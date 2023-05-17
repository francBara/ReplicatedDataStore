import DataStoreState.DSElement;
import Exceptions.QuorumNumberException;
import Message.Message;
import Message.MessageType;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;


public class Quorum {
    public final int writeQuorum;
    public final int readQuorum;

    //Enforcing a max number of replicas could imply concurrency problems
    //public final int maxNumberOfReplicas;
    

    public Quorum(int writeQuorum, int readQuorum) throws QuorumNumberException {
        if (writeQuorum <= 0 || readQuorum <= 0) {
            throw(new QuorumNumberException());
        }

        this.writeQuorum = writeQuorum;
        this.readQuorum = readQuorum;
        //this.maxNumberOfReplicas = inferMaxReplicas();
    }

    /**
     *
     * @param message The original write message, sent from the client
     * @param replicas The other replicas in the datastore
     * @return True if most replicas wrote successfully, false if not
     */
    public boolean initWriteQuorum(Message message, Replicas replicas) throws IOException, QuorumNumberException {
        if (writeQuorum > replicas.size()) {
            throw(new QuorumNumberException(writeQuorum, replicas.size()));
        }
        else if (message.messageType != MessageType.Write) {
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
        if (readQuorum > replicas.size()) {
            throw(new QuorumNumberException(readQuorum, replicas.size()));
        }
        else if (message.messageType != MessageType.Read) {
            throw(new RuntimeException());
        }

        message.setQuorum();

        final Set<Socket> quorumReplicas = replicas.sendMessageToBatch(message, readQuorum);

        Scanner scanner;
        final Gson gson = new Gson();
        DSElement currentReadElement = null;

        for (Socket socket : quorumReplicas) {
            //TODO: Should implement timeouts
            scanner = new Scanner(socket.getInputStream());

            DSElement readElement = gson.fromJson(scanner.nextLine(), DSElement.class);

            if (readElement != null && (currentReadElement == null || readElement.getVersionNumber() > currentReadElement.getVersionNumber())) {
                currentReadElement = readElement;
            }

            socket.close();
        }

        return currentReadElement;
    }

    private int inferMaxReplicas() {
        int writeWriteConflicts = 2 * writeQuorum;
        int readWriteConflicts = writeQuorum + readQuorum;

        if (writeWriteConflicts < readWriteConflicts) {
            return writeWriteConflicts - 1;
        }
        else {
            return readWriteConflicts - 1;
        }
    }
}
