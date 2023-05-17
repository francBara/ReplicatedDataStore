import DataStoreState.DSElement;
import DataStoreState.DSState;
import DataStoreState.DSStateException;
import Exceptions.QuorumNumberException;
import Message.Message;
import Message.MessageType;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Keeps the main methods for the replica, handling initialization, settings and communication
 */
public class DSCommunication {
    private final int port;

    private Replicas replicas;
    private final DSState dsState = new DSState();
    private Quorum quorum;

    public DSCommunication(int port) {
        this.port = port;
    }

    /**
     * This replica starts a new data store
     */
    public void initiateDataStore(int writeQuorum, int readQuorum) throws IOException {
        quorum = new Quorum(writeQuorum, readQuorum);
        replicas = new Replicas();
        begin();
    }

    /**
     * This replicas joins a preexisting data store
     * @param address The address of a replica of the data store
     * @param dataStorePort The port of a replica of the data store
     */
    public void joinDataStore(String address, int dataStorePort) throws IOException {
        final Socket socket = new Socket(address, dataStorePort);
        final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        final Scanner scanner = new Scanner(socket.getInputStream());

        final Message message = new Message(MessageType.Join);
        message.setPort(this.port);

        writer.println(message.toJson());

        final Gson gson = new Gson();

        replicas = gson.fromJson(scanner.nextLine(), Replicas.class);
        replicas.addReplica(new Replica(socket.getInetAddress().toString().substring(1), scanner.nextInt()));
        scanner.nextLine();
        quorum = gson.fromJson(scanner.nextLine(), Quorum.class);

        try {
            socket.close();
        } catch(IOException ignored) {}

        begin();
    }

    /**
     * Starts the replica, making it listen for requests from clients and other replicas
     * @throws IOException
     */
    private void begin() throws IOException  {
        final ServerSocket serverSocket = new ServerSocket(port);
        final ExecutorService executor = Executors.newCachedThreadPool();

        while (true) {
            //For every open connection, a new thread starts
            final Socket clientSocket = serverSocket.accept();

            executor.submit(() -> {
                Scanner scanner;
                PrintWriter writer;
                try {
                    scanner = new Scanner(clientSocket.getInputStream());
                    writer = new PrintWriter(clientSocket.getOutputStream(), true);
                } catch(IOException e) {
                    try {
                        clientSocket.close();
                    } catch(Exception ignored) {}
                    return;
                }

                //The message is parsed
                final Message message = new Gson().fromJson(scanner.nextLine(), Message.class);

                if (message.messageType == MessageType.Join) {
                    synchronized (this) {
                        final Gson gson = new Gson();
                        writer.println(gson.toJson(replicas));
                        writer.println(port);
                        writer.println(gson.toJson(quorum));

                        final Replica replica = new Replica(clientSocket.getInetAddress().toString().substring(1), message.getPort());

                        //The new replica is sent to other replicas
                        replicas.updateReplicas(replica);
                        replicas.addReplica(replica);
                    }
                }
                else if (message.messageType == MessageType.ReplicasUpdate) {
                    replicas.addReplica(new Gson().fromJson(scanner.nextLine(), Replica.class));
                    //System.out.println(this + "Current replicas: " + replicas.size());
                }
                else if (message.messageType == MessageType.Read) {
                    try {
                        //This replica starts a read quorum
                        final DSElement dsElement = quorum.initReadQuorum(message, replicas);

                        //This replica sends the most recent value to the client
                        writer.println(dsElement.getValue());
                    } catch(IOException | QuorumNumberException e) {
                        //TODO: Handle high quorum exception
                        System.out.println("Read error: " + e);
                        writer.println(new Message(MessageType.KO).toJson());
                    }
                }
                else if (message.messageType == MessageType.ReadQuorum) {
                    try {
                        //This replica replies to the read quorum with its version of the requested element
                        DSElement dsElement = dsState.read(message.getKey());
                        writer.println(new Gson().toJson(dsElement));
                    } catch(DSStateException e) {
                        writer.println(new Message(MessageType.KO).toJson());
                    }
                }
                else if (message.messageType == MessageType.Write) {
                    try {
                        //This replica starts a write quorum
                        final boolean quorumApproved = quorum.initWriteQuorum(message, replicas);

                        writer.println(new Message(quorumApproved ? MessageType.OK : MessageType.KO).toJson());
                    } catch(IOException | QuorumNumberException e) {
                        //TODO: Handle high quorum exception
                        System.out.println("Write error: " + e);
                        writer.println(new Message(MessageType.KO).toJson());
                    }
                }
                else if (message.messageType == MessageType.WriteQuorum) {
                    try {
                        //TODO: Is this the right way to do it? should there be more checks?
                        dsState.write(message.getKey(), message.getValue());
                        writer.println(new Message(MessageType.OK).toJson());
                    } catch(DSStateException e) {
                        writer.println(new Message(MessageType.KO).toJson());
                    }
                }
                try {
                    clientSocket.close();
                } catch(IOException ignored) {}
            });
        }
    }

    public int getReplicasSize() {
        return replicas.size();
    }
}
