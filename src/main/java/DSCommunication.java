import DataStore.DSElement;
import DataStore.DSState;
import DataStore.DSStateException;
import Message.Message;
import Message.MessageFactory;
import Message.JoinMessage;
import Message.ReadMessage;
import Message.WriteMessage;
import Message.ErrorMessage;
import com.google.gson.Gson;

import javax.xml.crypto.Data;
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
    private final int writeQuorum;
    private final int readQuorum;
    private final int port;

    private final Replicas replicas = new Replicas();
    private final DSState dsState = new DSState();

    public DSCommunication(int writeQuorum, int readQuorum, int port) {
        this.writeQuorum = writeQuorum;
        this.readQuorum = readQuorum;
        this.port = port;
    }

    /**
     * This replica starts a new data store
     * @param port The port in which to listen for requests, both from clients and other replicas
     */
    public void initiateDataStore(int port) {

    }

    /**
     * This replicas joins a preexisting data store
     * @param address The address of a replica of the data store
     * @param port The port of a replica of the data store
     */
    public void joinDataStore(String address, int port) {

    }

    /**
     * Starts the replica, making it listen for requests from clients and other replicas
     * @throws IOException
     */
    private void begin() throws IOException  {
        final ServerSocket serverSocket = new ServerSocket(port);
        final ExecutorService executor = Executors.newCachedThreadPool();
        final MessageFactory messageFactory = new MessageFactory();

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
                    return;
                }

                //The message is parsed
                final Message message = messageFactory.buildMessage(scanner.nextLine());

                if (message instanceof JoinMessage) {
                    //The new replica is added to the replicas
                    replicas.addReplica(new Replica(clientSocket.getInetAddress().toString(), ((JoinMessage) message).port));
                    //New replica information is sent to other replicas
                    replicas.updateReplicas();
                }
                else if (message instanceof final ReadMessage readMessage) {
                    if (readMessage.isQuorum) {
                        try {
                            //This replica replies to the read quorum with its version of the requested element
                            DSElement dsElement = dsState.read(readMessage.key);
                            writer.println(new Gson().toJson(dsElement));
                        } catch(DSStateException e) {
                            writer.println("KO");
                        }
                    }
                    else {
                        try {
                            //This replica starts a read quorum
                            final DSElement dsElement = new Quorum().initReadQuorum(readMessage, replicas, readQuorum);
                            //This replica sends the most recent value to the client
                            writer.println(new Gson().toJson(dsElement));
                        } catch(IOException e) {
                            writer.println("KO");
                        }
                    }
                }
                else if (message instanceof final WriteMessage writeMessage) {
                    if (writeMessage.isQuorum) {
                        try {
                            //This replica compares the requested value timestamp with the write message
                            DSElement dsElement = dsState.read(writeMessage.key);
                            if (dsElement.getTimestamp() > writeMessage.getTimestamp()) {
                                //TODO: Replace OK and KO messages with proper enums
                                writer.println("KO");
                            }
                            else {
                                //The element is written
                                dsState.write(writeMessage.key, new DSElement(writeMessage.item, writeMessage.getTimestamp()));
                                writer.println("OK");
                            }
                        } catch(DSStateException e) {
                            writer.println("KO");
                        }
                    }
                    else {
                        //This replica starts a write quorum
                        final boolean quorumApproved = new Quorum().initWriteQuorum(writeMessage, replicas, writeQuorum);
                        writer.println(quorumApproved ? "OK" : "KO");
                    }
                }
            });
        }
    }
}
