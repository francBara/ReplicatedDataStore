package it.polimi.ds.DataStore;

import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.DataStore.DataStoreState.DSNullElement;
import it.polimi.ds.DataStore.DataStoreState.DSState;
import it.polimi.ds.DataStore.DataStoreState.DSStateException;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;


public class Quorum {
    public final int writeQuorum;
    public final int readQuorum;
    public final int maxReplicas;

    final DSState dsState;

    //Enforcing a max number of replicas could imply concurrency problems
    //public final int maxNumberOfReplicas;
    

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
     *
     * @param message The original write message, sent from the client
     * @param replicas The other replicas in the datastore
     * @return True if most replicas wrote successfully, false if not
     */
    public boolean initWriteQuorum(Message message, Replicas replicas) throws IOException, QuorumNumberException {
        if (message.messageType != MessageType.Write) {
            throw(new RuntimeException());
        }

        message.setQuorum();

        final Set<Socket> quorumReplicas = replicas.sendMessageToBatch(message, writeQuorum - 1);

        Scanner scanner;
        final Gson gson = new Gson();
        int successfulReplicas = 0;

        for (Socket socket : quorumReplicas) {
            scanner = new Scanner(socket.getInputStream());
            Message writeAck = gson.fromJson(scanner.nextLine(), Message.class);
            if (writeAck.messageType == MessageType.OK) {
                successfulReplicas++;
            }
            socket.close();
        }

        //Attempts to write in the local replica
        try {
            dsState.write(message.getKey(), message.getValue(), message.getVersionNumber());
            successfulReplicas++;
        } catch(DSStateException ignored) {}

        return successfulReplicas > writeQuorum / 2;
    }

    /**
     *
     * @param message The original read message, sent from the client
     * @param replicas The other replicas in the datastore
     * @return The most recent element read from the replicas
     * @throws IOException
     */
    public DSElement initReadQuorum(Message message, Replicas replicas) throws IOException, QuorumNumberException {
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
        try {
            DSElement localElement = dsState.read(message.getKey());
            if (!localElement.isNull() && (currentReadElement.isNull() || localElement.getVersionNumber() > currentReadElement.getVersionNumber())) {
                currentReadElement = localElement;
            }
            else if (!currentReadElement.isNull() && (localElement.isNull() || localElement.getVersionNumber() < currentReadElement.getVersionNumber())) {
                //Read-repair on local replica
                dsState.write(message.getKey(), currentReadElement);
            }
        } catch(DSStateException ignored) {}

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
