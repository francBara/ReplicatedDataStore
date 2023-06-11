package it.polimi.ds.DataStore;

import it.polimi.ds.DataStore.Exceptions.FullDataStoreException;

import java.io.IOException;
import java.util.HashSet;

public class LocalReplicasFactory {
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
