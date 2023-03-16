import Message.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps references to the other replicas in the data store
 */
public class Replicas {
    private final Map<String, Replica> replicas = new HashMap<>();

    public void addReplica(Replica newReplica) {
        for (Replica replica : replicas.values()) {
            //TODO: Send new replica data to each replica
        }
        replicas.put(newReplica.getAddress(), newReplica);
    }

    /**
     * Sends a message to a given number of replicas, chosen indeterminately between available replicas
     * @param replicasNumber The number of replicas to contact
     */
    public HashSet<Socket> sendMessageToBatch(Message message, int replicasNumber) throws IOException {
        final HashSet<Socket> replicasSockets = new HashSet<>();
        //TODO: Improve the choice of replicas to contact, remember to take this replica into account
        int i = 0;
        for (Replica replica : replicas.values()) {
            if (i == replicasNumber) {
                break;
            }
            replicasSockets.add(replica.sendMessage(message));
            i++;
        }
        return replicasSockets;
    }

    public HashSet<Replica> getReplicas() {
        return new HashSet<Replica>(replicas.values());
    }


    public int size() {
        return replicas.size();
    }
}
