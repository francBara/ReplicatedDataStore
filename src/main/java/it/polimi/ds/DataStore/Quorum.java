package it.polimi.ds.DataStore;

import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.DataStore.DataStoreState.DSNullElement;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;


public class Quorum {
    public final int writeQuorum;
    public final int readQuorum;
    public final int maxReplicas;

    //Enforcing a max number of replicas could imply concurrency problems
    //public final int maxNumberOfReplicas;
    

    public Quorum(int writeQuorum, int readQuorum) throws QuorumNumberException {
        if (writeQuorum <= 0 || readQuorum <= 0) {
            throw(new QuorumNumberException());
        }

        final int maxReplicas = inferMaxReplicas();

        if (maxReplicas <= 0) {
            throw(new QuorumNumberException());
        }

        this.writeQuorum = writeQuorum;
        this.readQuorum = readQuorum;
        this.maxReplicas = maxReplicas;
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

        final Set<Socket> quorumReplicas = replicas.sendMessageToBatch(message, writeQuorum);

        Scanner scanner;
        final Gson gson = new Gson();
        int successfulReplicas = 0;

        for (Socket socket : quorumReplicas) {
            //TODO: Should implement timeouts
            scanner = new Scanner(socket.getInputStream());
            Message writeAck = gson.fromJson(scanner.nextLine(), Message.class);
            if (writeAck.messageType == MessageType.OK) {
                successfulReplicas++;
            }
            socket.close();
        }

        return successfulReplicas > replicas.size() / 2;
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

        final Set<Socket> quorumReplicas = replicas.sendMessageToBatch(message, readQuorum);

        Scanner scanner;
        final Gson gson = new Gson();
        DSElement currentReadElement = new DSNullElement();

        for (Socket socket : quorumReplicas) {
            //TODO: Should implement timeouts
            scanner = new Scanner(socket.getInputStream());

            DSElement readElement = gson.fromJson(scanner.nextLine(), DSElement.class);
            //System.out.println(readElement);

            if (!readElement.isNull() && (currentReadElement.isNull() || readElement.getVersionNumber() > currentReadElement.getVersionNumber())) {
                currentReadElement = readElement;
            }

            socket.close();
        }

        System.out.println("\n");

        return currentReadElement;
    }

    private int inferMaxReplicas() {
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