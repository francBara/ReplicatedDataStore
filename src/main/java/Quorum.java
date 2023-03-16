import DataStore.DSElement;
import Message.Message;
import Message.MessageType;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Quorum {
    private final int writeQuorum;
    private final int readQuorum;

    public Quorum(int writeQuorum, int readQuorum) {
        this.writeQuorum = writeQuorum;
        this.readQuorum = readQuorum;
    }

    /**
     *
     * @param message The original write message, sent from the client
     * @param replicas The other replicas in the datastore
     * @return True if most replicas wrote successfully, false if not
     */
    public boolean initWriteQuorum(Message message, Replicas replicas) {
        //TODO: Implement
        return false;
    }

    /**
     *
     * @param message The original read message, sent from the client
     * @param replicas The other replicas in the datastore
     * @return The most recent element read from the replicas
     * @throws IOException
     */
    public DSElement initReadQuorum(Message message, Replicas replicas) throws IOException {
        if (readQuorum > replicas.size() || message.messageType != MessageType.Read) {
            throw(new RuntimeException());
        }

        message.setQuorum();

        final Set<Socket> quorumReplicas = replicas.sendMessageToBatch(message, readQuorum);

        Scanner scanner;
        Gson gson = new Gson();
        DSElement currentReadElement = null;
        for (Socket socket : quorumReplicas) {
            //TODO: Should implement timeouts
            scanner = new Scanner(socket.getInputStream());
            DSElement readElement = gson.fromJson(scanner.nextLine(), DSElement.class);
            if (currentReadElement == null || readElement.getVersionNumber() > currentReadElement.getVersionNumber()) {
                currentReadElement = readElement;
            }
        }

        return currentReadElement;
    }
}
