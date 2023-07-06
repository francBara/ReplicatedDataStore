package it.polimi.ds.DataStore;

import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.DataStore.DataStoreState.DSNullElement;
import it.polimi.ds.DataStore.DataStoreState.DSState;
import it.polimi.ds.DataStore.DataStoreState.DSStateException;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.DataStore.Lock.LockNotifier;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

/**
 * Manages write quorums, read quorums and quorum coherence
 */
public class Quorum {
    public final int writeQuorum;
    public final int readQuorum;
    public final int maxReplicas;

    private final DSState dsState;

    /**
     * @param writeQuorum
     * @param readQuorum
     * @param dsState
     * @throws QuorumNumberException If writeQuorum or readQuorum are not coherent
     */
    public Quorum(int writeQuorum, int readQuorum, DSState dsState) throws QuorumNumberException {
        if (writeQuorum <= 0 || readQuorum <= 0) {
            throw(new QuorumNumberException());
        }

        final int maxReplicas = inferMaxReplicas(writeQuorum, readQuorum);

        if (maxReplicas <= 0) {
            throw(new QuorumNumberException());
        }

        this.writeQuorum = writeQuorum;
        this.readQuorum = readQuorum;
        this.maxReplicas = maxReplicas;
        this.dsState = dsState;
    }

    /**
     * Initiates a write quorum to the data store, effectively writing if successful. The write quorum could be aborted if the lock gets bullied.
     * @param message The original write message, sent from the client
     * @param replicas The other replicas in the datastore
     * @param lockNotifier The lock notifier, retaining lock status
     * @return True if the required number of replicas wrote successfully, false if not
     * @throws IOException If communication errors arise
     */
    public boolean initWriteQuorum(Message message, Replicas replicas, LockNotifier lockNotifier) throws IOException {
        if (message.messageType != MessageType.Write) {
            throw(new RuntimeException());
        }

        final Gson gson = new Gson();

        message.setQuorum();

        //Contacts writeQuorum - 1 replicas for a write quorum
        final Set<Socket> quorumReplicas = replicas.sendMessageToBatch(message, writeQuorum - 1);

        int availableReplicas = 1;

        //Counts how many of the contacted replicas are available, and checks the most recent element they have
        DSElement mostRecentElement = new DSNullElement();
        for (Socket socket : quorumReplicas) {
            final Scanner scanner = new Scanner(socket.getInputStream());
            if (MessageType.valueOf(scanner.nextLine()) == MessageType.OK) {
                availableReplicas++;
            }
            else {
                continue;
            }
            DSElement lastElement = gson.fromJson(scanner.nextLine(), DSElement.class);
            if (!lastElement.isNull() && (mostRecentElement.isNull() || lastElement.getVersionNumber() > mostRecentElement.getVersionNumber())) {
                mostRecentElement = lastElement;
            }
        }

        //Sets the version number as the highest version number between the ones received, if any
        if (!mostRecentElement.isNull()) {
            message.setVersionNumber(mostRecentElement.getVersionNumber());
        }

        //If enough replicas are available, it sends the final version number and waits for a final OK message
        if (availableReplicas >= writeQuorum) {
            availableReplicas = 1;
            for (Socket socket : quorumReplicas) {
                try {
                    final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println(MessageType.OK);
                    writer.println(gson.toJson(message));
                    if (MessageType.valueOf(new Scanner(socket.getInputStream()).nextLine()) == MessageType.OK) {
                        availableReplicas++;
                    }
                } catch(Exception ignored) {}
            }

            /*
            If enough replicas are ready and the lock has not been replaced
            the final commit message is sent and the value is written locally
             */
            if (availableReplicas >= writeQuorum && lockNotifier.forceLock()) {
                for (Socket socket : quorumReplicas) {
                    try {
                        new PrintWriter(socket.getOutputStream(), true).println(MessageType.OK);
                        socket.close();
                    } catch(Exception ignored) {}
                }
                //Writes in the local replica
                dsState.write(message.getKey(), message.getValue(), message.getVersionNumber());
                return true;
            }
        }

        for (Socket socket : quorumReplicas) {
            try {
                new PrintWriter(socket.getOutputStream(), true).println(MessageType.KO);
                socket.close();
            } catch(Exception ignored) {}
        }
        return false;
    }

    /**
     * Initiates a read quorum and, in case it's not bullied by other writes, performs read-repair
     * @param message The original read message, sent from the client
     * @param replicas The other replicas in the datastore
     * @param lockNotifier The lock notifier, retaining lock status
     * @return The most recent element read from the replicas
     * @throws IOException If communication errors arise
     */
    public DSElement initReadQuorum(Message message, Replicas replicas, LockNotifier lockNotifier) throws IOException {
        if (message.getType() != MessageType.Read) {
            throw(new RuntimeException());
        }

        message.setQuorum();

        //Pairs every replica with its queried element
        final HashMap<Socket, DSElement> quorumValues = new HashMap<>();

        replicas.sendMessageToBatch(message, readQuorum - 1).forEach((Socket socket) -> quorumValues.put(socket, new DSNullElement()));

        Scanner scanner;
        final Gson gson = new Gson();
        DSElement currentReadElement = new DSNullElement();

        for (Socket socket : quorumValues.keySet()) {
            scanner = new Scanner(socket.getInputStream());

            DSElement readElement = gson.fromJson(scanner.nextLine(), DSElement.class);

            quorumValues.put(socket, readElement);

            //The most recent element is chosen
            if (!readElement.isNull() && (currentReadElement.isNull() || readElement.getVersionNumber() > currentReadElement.getVersionNumber())) {
                currentReadElement = readElement;
            }
        }

        //Reads local value
        DSElement localElement = dsState.read(message.getKey());
        if (!localElement.isNull() && (currentReadElement.isNull() || localElement.getVersionNumber() > currentReadElement.getVersionNumber())) {
            currentReadElement = localElement;
        }
        else if (!currentReadElement.isNull() && (localElement.isNull() || localElement.getVersionNumber() < currentReadElement.getVersionNumber())) {
            //Read-repair on local replica
            if (lockNotifier.forceLock()) {
                dsState.write(message.getKey(), currentReadElement);
            }
        }

        PrintWriter writer;
        for (Socket socket : quorumValues.keySet()) {
            writer = new PrintWriter(socket.getOutputStream(), true);
            //Read-repair, in case of stale values in replicas, an update is propagated
            if (!currentReadElement.isNull() && (quorumValues.get(socket).isNull() || quorumValues.get(socket).getVersionNumber() < currentReadElement.getVersionNumber())) {
                writer.println(MessageType.KO);
                writer.println(gson.toJson(currentReadElement));
            }
            else {
                writer.println(MessageType.OK);
            }
            socket.close();
        }

        return currentReadElement;
    }

    /**
     * Calculates the max number of replicas allowed for the given writeQuorum and readQuorum
     * @param writeQuorum
     * @param readQuorum
     * @return The max number of replicas
     */
    private int inferMaxReplicas(int writeQuorum, int readQuorum) {
        //NW > N / 2
        int writeWriteConflicts = 2 * writeQuorum;
        //NR + NW > N
        int readWriteConflicts = writeQuorum + readQuorum;

        if (writeWriteConflicts < readWriteConflicts) {
            return writeWriteConflicts - 1;
        }
        else {
            return readWriteConflicts - 1;
        }
    }
}
