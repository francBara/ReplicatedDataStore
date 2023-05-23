package DataStore.RequestsHandler;

import DataStore.DataStoreState.DSElement;
import DataStore.DataStoreState.DSState;
import DataStore.DataStoreState.DSStateException;
import DataStore.Quorum;
import DataStore.Replica;
import DataStore.Replicas;
import DataStore.Exceptions.QuorumNumberException;
import Message.Message;
import Message.MessageType;
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

    abstract public void handleJoin(Socket clientSocket, PrintWriter writer, Message message);

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

    public void handleReadQuorum(PrintWriter writer, Message message) {
        try {
            //This replica replies to the read quorum with its version of the requested element
            DSElement dsElement = dsState.read(message.getKey());
            writer.println(new Gson().toJson(dsElement));
        } catch(DSStateException e) {
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
