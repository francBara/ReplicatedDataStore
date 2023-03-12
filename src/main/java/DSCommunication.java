import DataStore.DSElement;
import DataStore.DSState;
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

    private void begin() throws IOException  {
        final ServerSocket serverSocket = new ServerSocket(port);
        final ExecutorService executor = Executors.newCachedThreadPool();
        final MessageFactory messageFactory = new MessageFactory();

        while (true) {
            final Socket clientSocket = serverSocket.accept();
            executor.submit(() -> {
                try {
                    final Scanner scanner = new Scanner(clientSocket.getInputStream());
                    final PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    final Message message = messageFactory.buildMessage(scanner.nextLine());

                    if (message instanceof JoinMessage) {
                        replicas.addReplica(new Replica(clientSocket.getInetAddress().toString(), ((JoinMessage) message).port));
                        replicas.updateReplicas();
                    }
                    else if (message instanceof final ReadMessage readMessage) {
                        if (readMessage.isQuorum) {
                            DSElement dsElement = dsState.read(readMessage.key);
                            writer.println(new Gson().toJson(dsElement));
                        }
                        else {
                            final Quorum quorum = new Quorum();
                            final DSElement dsElement = quorum.initReadQuorum(readMessage, replicas, readQuorum);
                            writer.println(dsElement);
                        }
                    }
                    else if (message instanceof final WriteMessage writeMessage) {
                        if (writeMessage.isQuorum) {
                            DSElement dsElement = dsState.read(writeMessage.key);
                            if (dsElement.getTimestamp() > writeMessage.getTimestamp()) {
                                //TODO: Replace OK and KO messages with proper enums
                                writer.println("KO");
                            }
                            else {
                                dsState.write(writeMessage.key, new DSElement(writeMessage.item, writeMessage.getTimestamp()));
                                writer.println("OK");
                            }
                        }
                        else {
                            final Quorum quorum = new Quorum();
                            final boolean quorumApproved = quorum.initWriteQuorum(writeMessage, replicas, writeQuorum);
                            writer.println(quorumApproved ? "OK" : "KO");
                        }
                    }

                } catch(Exception e) {

                }
            });
        }
    }
}
