package it.polimi.ds.DataStore;
import it.polimi.ds.DataStore.DataStoreState.DSState;
import it.polimi.ds.DataStore.RequestsHandler.CoordinatorHandler;
import it.polimi.ds.DataStore.RequestsHandler.PeerHandler;
import it.polimi.ds.DataStore.RequestsHandler.RequestsHandler;
import it.polimi.ds.DataStore.Exceptions.FullDataStoreException;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Keeps the main methods for a replica, handling initialization, settings and communication
 */
public class DataStoreNetwork {
    private int port;

    private Replicas replicas;
    private final DSState dsState = new DSState();
    private Quorum quorum;

    private RequestsHandler requestsHandler;

    private ServerSocket serverSocket;

    private boolean isRunning = true;

    public DataStoreNetwork(int port) {
        this.port = port;
    }
    public DataStoreNetwork() {}

    /**
     * The replica starts a new data store
     * @param writeQuorum
     * @param readQuorum
     * @throws IOException If communication errors arise
     * @throws QuorumNumberException If writeQuorum and readQuorum are not consistent
     */
    public void initiateDataStore(int writeQuorum, int readQuorum) throws IOException, QuorumNumberException {
        quorum = new Quorum(writeQuorum, readQuorum, dsState);
        replicas = new Replicas();
        requestsHandler = new CoordinatorHandler(replicas, quorum, dsState, port);
        System.out.println("Maximum number of replicas: " + quorum.maxReplicas);
        begin();
    }

    /**
     * The replica joins a preexisting data store
     * @param address The address of the datastore initializer
     * @param dataStorePort The port of the datastore initializer
     * @throws IOException If communication errors arise
     * @throws FullDataStoreException If the data store can't accept new replicas, or if the contacted replica is not an initializer
     */
    public void joinDataStore(String address, int dataStorePort) throws IOException, FullDataStoreException {
        final Socket socket = new Socket(address, dataStorePort);
        final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        final Scanner scanner = new Scanner(socket.getInputStream());

        final Message message = new Message(MessageType.Join);
        message.setPort(this.port);

        writer.println(message.toJson());


        final MessageType joinApproved = MessageType.valueOf(scanner.nextLine());

        if (joinApproved == MessageType.KO) {
            throw(new FullDataStoreException());
        }

        final Gson gson = new Gson();

        replicas = gson.fromJson(scanner.nextLine(), Replicas.class);

        replicas.addReplica(new Replica(socket.getInetAddress().toString().substring(1), scanner.nextInt()));
        scanner.nextLine();

        quorum = gson.fromJson(scanner.nextLine(), Quorum.class);

        requestsHandler = new PeerHandler(replicas, quorum, dsState, port, socket);

        begin();
    }

    /**
     * Starts the replica, making it listen for requests from clients and other replicas
     * @throws IOException
     */
    private void begin() throws IOException  {
        serverSocket = new ServerSocket(port);
        final ExecutorService executor = Executors.newCachedThreadPool();

        //If a peer, notifies the coordinator to be ready to receive requests
        requestsHandler.ackCoordinator();

        System.out.println("\nReplica ready on port " + port);

        while (isRunning) {
            try {
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

                    boolean closeSocket = true;

                    //The message is parsed
                    final Message message = new Gson().fromJson(scanner.nextLine(), Message.class);

                    if (message.messageType == MessageType.Join) {
                        requestsHandler.handleJoin(clientSocket, writer, scanner, message);
                    }
                    else if (message.messageType == MessageType.ReplicasUpdate) {
                        requestsHandler.handleReplicasUpdate(scanner);
                    }
                    else if (message.messageType == MessageType.Read) {
                        requestsHandler.handleRead(writer, message);
                    }
                    else if (message.messageType == MessageType.ReadQuorum) {
                        requestsHandler.handleReadQuorum(writer, scanner, message);
                    }
                    else if (message.messageType == MessageType.Write) {
                        closeSocket = false;
                        requestsHandler.handleWrite(clientSocket, writer, message, false);
                    }
                    else if (message.messageType == MessageType.WriteQuorum) {
                        requestsHandler.handleWriteQuorum(message, writer, scanner);
                    }
                    if (closeSocket) {
                        try {
                            clientSocket.close();
                        } catch(IOException ignored) {}
                    }
                });
            } catch(SocketException e) {
                return;
            }
        }
    }

    /**
     * Close the connection
     * @throws IOException
     */
    public void close() throws IOException {
        isRunning = false;
        serverSocket.close();
    }

    /**
     * @param port The port where to start the datastore process
     */
    public void setPort(int port) {
        this.port = port;
    }

    public Quorum getQuorum() {
        return quorum;
    }

    public int getReplicasSize() {
        return replicas.size();
    }
}
