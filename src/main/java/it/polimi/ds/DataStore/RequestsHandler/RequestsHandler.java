package it.polimi.ds.DataStore.RequestsHandler;

import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.DataStore.DataStoreState.DSState;
import it.polimi.ds.DataStore.DataStoreState.DSStateException;
import it.polimi.ds.DataStore.Quorum;
import it.polimi.ds.DataStore.Replica;
import it.polimi.ds.DataStore.Replicas;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public abstract class RequestsHandler {
    protected Replicas replicas;
    protected Quorum quorum;
    protected DSState dsState;
    protected int port;

    protected RequestsHandler(Replicas replicas, Quorum quorum, DSState dsState, int port) {
        this.replicas = replicas;
        this.quorum = quorum;
        this.dsState = dsState;
        this.port = port;
    }

    abstract public void ackCoordinator() throws IOException;

    abstract public void handleJoin(Socket clientSocket, PrintWriter writer, Scanner scanner, Message message);

    public void handleReplicasUpdate(Scanner scanner) {
        replicas.addReplica(new Gson().fromJson(scanner.nextLine(), Replica.class));
    }

    public void handleRead(PrintWriter writer, Message message) {
        try {
            //This replica starts a read quorum
            final DSElement dsElement = quorum.initReadQuorum(message, replicas);

            //This replica sends the most recent value to the client
            writer.println(new Gson().toJson(dsElement));
        } catch(IOException | QuorumNumberException e) {
            //TODO: Handle high quorum exception
            System.out.println("Read error: " + e);
            writer.println(new Message(MessageType.KO).toJson());
        }
    }

    public void handleReadQuorum(PrintWriter writer, Scanner scanner, Message message) {
        try {
            Gson gson = new Gson();

            //This replica replies to the read quorum with its version of the requested element
            DSElement dsElement = dsState.read(message.getKey());
            writer.println(gson.toJson(dsElement));

            //Read-repair in case of stale element
            MessageType elementIsRecent = MessageType.valueOf(scanner.nextLine());
            if (elementIsRecent == MessageType.KO) {
                DSElement recentElement = gson.fromJson(scanner.nextLine(), DSElement.class);
                dsState.write(message.getKey(), recentElement);
            }
        } catch(DSStateException e) {
            //TODO: Handle KO in the other side
            writer.println(new Message(MessageType.KO).toJson());
        }
    }

    public void handleWrite(PrintWriter writer, Message message) {
        try {
            //Reads the value to write, if it already exists, the version number is propagated
            final Message readMessage = new Message(MessageType.Read);
            readMessage.setKey(message.getKey());
            final DSElement dsElement = quorum.initReadQuorum(readMessage, replicas);

            if (!dsElement.isNull()) {
                message.setVersionNumber(dsElement.getVersionNumber());
            }

            //This replica starts a write quorum
            final boolean quorumApproved = quorum.initWriteQuorum(message, replicas);

            writer.println(new Message(quorumApproved ? MessageType.OK : MessageType.KO).toJson());
        } catch(IOException | QuorumNumberException e) {
            //TODO: Handle high quorum exception
            System.out.println(e);
            writer.println(new Message(MessageType.KO).toJson());
        }
    }

    public void handleWriteQuorum(PrintWriter writer, Message message) {
        try {
            dsState.write(message.getKey(), message.getValue(), message.getVersionNumber());
            writer.println(new Message(MessageType.OK).toJson());
        } catch(DSStateException e) {
            writer.println(new Message(MessageType.KO).toJson());
        }
    }
}
