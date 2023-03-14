import DataStore.DSElement;
import Message.ReadMessage;
import Message.WriteMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Quorum {
    /**
     *
     * @param message The original write message, sent from the client
     * @param replicas The other replicas in the datastore
     * @param quorum The number of replicas to contact
     * @return True if most replicas wrote successfully, false if not
     */
    public boolean initWriteQuorum(WriteMessage message, Replicas replicas, int quorum) {
        //TODO: Implement
        return false;
    }

    /**
     *
     * @param message The original read message, sent from the client
     * @param replicas The other replicas in the datastore
     * @param quorum The number of replicas to contact
     * @return The most recent element read from the replicas
     * @throws IOException
     */
    public DSElement initReadQuorum(ReadMessage message, Replicas replicas, int quorum) throws IOException {
        if (quorum > replicas.size()) {
            throw(new RuntimeException());
        }

        final Set<Socket> quorumReplicas = new HashSet<>();

        message.isQuorum = true;

        //TODO: Improve the choice of replicas to contact, remember to take this replica into account
        int i = 0;
        for (Replica replica : replicas.getReplicas()) {
            if (i == quorum) {
                break;
            }
            quorumReplicas.add(replica.sendMessage(message));
            i++;
        }

        Scanner scanner;
        Gson gson = new Gson();
        DSElement currentReadElement = null;
        for (Socket socket : quorumReplicas) {
            //TODO: Should implement timeouts
            scanner = new Scanner(socket.getInputStream());
            DSElement readElement = gson.fromJson(scanner.nextLine(), DSElement.class);
            if (currentReadElement == null || readElement.getTimestamp() > currentReadElement.getTimestamp()) {
                currentReadElement = readElement;
            }
        }

        return currentReadElement;
    }
}
