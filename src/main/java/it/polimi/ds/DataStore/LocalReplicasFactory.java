package it.polimi.ds.DataStore;
import java.util.HashSet;


public class LocalReplicasFactory {
    /**
     * Deploys a group of replicas, making them join a preexisting data store
     * @param coordinatorAddress
     * @param coordinatorPort
     * @param localPort The port to host the data store process
     * @param count The number of replicas to deploy
     * @return
     */
    public HashSet<DataStoreNetwork> getReplicas(String coordinatorAddress, int coordinatorPort, int localPort, int count) {
        HashSet<DataStoreNetwork> replicas = new HashSet<>();

        for (int i = 0; i < count; i++) {
            replicas.add(new DataStoreNetwork(localPort + i));
        }

        for (DataStoreNetwork dsn : replicas) {
            new Thread(() -> {
                try {
                    dsn.joinDataStore(coordinatorAddress, coordinatorPort);
                } catch (Exception ignored) {
                    //System.out.println(ignored);
                }
            }).start();
        }

        return replicas;
    }
}
