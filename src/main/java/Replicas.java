import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Replicas {
    private final Map<String, Replica> replicas = new HashMap<>();

    public void addReplica(Replica replica) {
        replicas.put(replica.getAddress(), replica);
    }

    public HashSet<Replica> getReplicas() {
        return new HashSet<Replica>(replicas.values());
    }

    //TODO: Updates all replicas with replicas references
    public void updateReplicas() {

    }

    public int size() {
        return replicas.size();
    }
}
