package it.polimi.ds.DataStore.RequestsHandler;

import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.DataStore.DataStoreState.DSNullElement;
import it.polimi.ds.DataStore.DataStoreState.DSState;
import it.polimi.ds.DataStore.Lock.Lock;
import it.polimi.ds.DataStore.Lock.LockNotifier;
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
    private final Lock lock = new Lock();

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
            final LockNotifier lockNotifier = lock.lockRead();
            //This replica starts a read quorum
            final DSElement dsElement = quorum.initReadQuorum(message, replicas, lockNotifier);

            //This replica sends the most recent value to the client
            writer.println(MessageType.OK);
            writer.println(new Gson().toJson(dsElement));

            lockNotifier.unlock();
        } catch(IOException e) {
            writer.println(MessageType.KO);
        }
    }

    public void handleReadQuorum(PrintWriter writer, Scanner scanner, Message message) {
        final Gson gson = new Gson();
        final LockNotifier lockNotifier = lock.lockRead();

        //This replica replies to the read quorum with its version of the requested element
        DSElement dsElement = dsState.read(message.getKey());
        writer.println(gson.toJson(dsElement));

        //Read-repair in case of stale element
        MessageType elementIsRecent = MessageType.valueOf(scanner.nextLine());
        if (elementIsRecent == MessageType.KO && lockNotifier.forceLock()) {
            DSElement recentElement = gson.fromJson(scanner.nextLine(), DSElement.class);
            dsState.write(message.getKey(), recentElement);
        }
        lockNotifier.unlock();
    }

    public void handleWrite(PrintWriter writer, Message message) {
        try {
            final int nonce = lock.generateNonce();
            final LockNotifier lockNotifier = lock.lock(nonce);
            if (lockNotifier.isLocked()) {
                message.setNonce(nonce);
                //This replica starts a write quorum
                final boolean quorumApproved = quorum.initWriteQuorum(message, replicas, lockNotifier);

                writer.println(quorumApproved ? MessageType.OK : MessageType.KO);
                lockNotifier.unlock();
            }
            else {
                writer.println(MessageType.KO);
            }
        } catch(IOException | QuorumNumberException e) {
            //TODO: Handle high quorum exception
            writer.println(MessageType.KO);
        }
    }

    public void handleWriteQuorum(Message message, PrintWriter writer, Scanner scanner) {
        final LockNotifier lockNotifier = lock.lock(message.getNonce());
        if (lockNotifier.isLocked()) {
            writer.println(MessageType.OK);
            final Gson gson = new Gson();
            writer.println(gson.toJson(dsState.read(message.getKey())));
            final Message writeMessage = new Gson().fromJson(scanner.nextLine(), Message.class);

            if (!lockNotifier.forceLock()) {
                writer.println(MessageType.KO);
                return;
            }

            writer.println(MessageType.OK);

            if (MessageType.valueOf(scanner.nextLine()) == MessageType.OK) {
                dsState.write(writeMessage.getKey(), writeMessage.getValue(), writeMessage.getVersionNumber());
            }
            lockNotifier.unlock();
        }
        else {
            writer.println(MessageType.KO);
        }
    }
}
