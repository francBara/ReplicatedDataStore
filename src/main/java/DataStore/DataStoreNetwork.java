package DataStore;
import DataStore.DataStoreState.DSState;
import DataStore.RequestsHandler.CoordinatorHandler;
import DataStore.RequestsHandler.PeerHandler;
import DataStore.RequestsHandler.RequestsHandler;
import DataStore.Exceptions.FullDataStoreException;
import DataStore.Exceptions.QuorumNumberException;
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
public class DataStoreNetwork {
    private final int port;

    private Replicas replicas;
    private final DSState dsState = new DSState();
    private Quorum quorum;

    private RequestsHandler requestsHandler;

    public DataStoreNetwork(int port) {
        this.port = port;
    }

    /**
     * This replica starts a new data store
     */
    public void initiateDataStore(int writeQuorum, int readQuorum) throws IOException, QuorumNumberException {
        quorum = new Quorum(writeQuorum, readQuorum);
        replicas = new Replicas();
        requestsHandler = new CoordinatorHandler(replicas, quorum, dsState, port);
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

        //TODO: Handle IllegalArgumentException
        final MessageType joinApproved = MessageType.valueOf(scanner.nextLine());

        if (joinApproved == MessageType.KO) {
            //TODO: Display error
            /*
            This error is thrown when there are too many replicas in the data store,
            or when the contacted replica is not the coordinator.
            */
            throw(new FullDataStoreException());
        }

        final Gson gson = new Gson();

        replicas = gson.fromJson(scanner.nextLine(), Replicas.class);
        replicas.addReplica(new Replica(socket.getInetAddress().toString().substring(1), scanner.nextInt()));
        scanner.nextLine();
        quorum = gson.fromJson(scanner.nextLine(), Quorum.class);

        requestsHandler = new PeerHandler(replicas, quorum, dsState, port);

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
                    requestsHandler.handleJoin(clientSocket, writer, message);
                }
                else if (message.messageType == MessageType.ReplicasUpdate) {
                    requestsHandler.handleReplicasUpdate(scanner);
                }
                else if (message.messageType == MessageType.Read) {
                    requestsHandler.handleRead(writer, message);
                }
                else if (message.messageType == MessageType.ReadQuorum) {
                    requestsHandler.handleReadQuorum(writer, message);
                }
                else if (message.messageType == MessageType.Write) {
                    requestsHandler.handleWrite(writer, message);
                }
                else if (message.messageType == MessageType.WriteQuorum) {
                    requestsHandler.handleWriteQuorum(writer, message);
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

    public Quorum getQuorum() {
        return quorum;
    }
}
