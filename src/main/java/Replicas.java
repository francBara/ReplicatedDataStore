import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps references to the other replicas in the data store
 */
public class Replicas {
    private final Map<String, Replica> replicas = new HashMap<>();

    public void addReplica(Replica replica) {
        replicas.put(replica.getAddress(), replica);
    }

    public HashSet<Replica> getReplicas() {
        return new HashSet<Replica>(replicas.values());
    }

    /**
     * Sends this replica references to other replicas in the data store
     */
    public void updateReplicas() {
        //TODO: Implement
    }

    public int size() {
        return replicas.size();
    }
}
