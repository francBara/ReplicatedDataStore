import it.polimi.ds.Client.Client;
import it.polimi.ds.Client.Exceptions.ReadException;
import it.polimi.ds.DataStore.DataStoreNetwork;
import it.polimi.ds.DataStore.DataStoreState.DSElement;
import junit.framework.TestCase;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RaceConditionTest extends TestCase {
    public void testRaceCondition() {
        DataStoreNetwork coordinator = new DataStoreNetwork(5000);
        HashSet<DataStoreNetwork> replicas = new HashSet<>();

        new Thread(() -> {
            try {
                coordinator.initiateDataStore(10, 10);
            } catch(Exception e) {
                fail();
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch(InterruptedException ignored) {}

        for (int i = 0; i < 20; i++) {
            int finalI = i;
            new Thread(() -> {
                DataStoreNetwork replica = new DataStoreNetwork(5001 + finalI);
                replicas.add(replica);
                try {
                    replica.joinDataStore("127.0.0.1", 5000);
                } catch(Exception e) {

                }
            }).start();
        }

        try {
            Thread.sleep(3000);
        } catch(Exception ignored) {fail();}


        assertEquals(19, coordinator.getReplicasSize());


        for (int i = 0; i < 10; i++) {
            Client client = new Client();
            client.bind("127.0.0.1", 5000 + i);
            int finalI = i;
            new Thread(() -> {
                try {
                    client.write("Luca", "Andrulli" + finalI);
                } catch(IOException ignored) {
                    fail();
                }
            }).start();
        }

        HashMap<Integer, ArrayList<DSElement>> result = new HashMap<>();

        for (int i = 0; i < 20; i++) {
            Client client = new Client();
            client.bind("127.0.0.1", 5000 + i);

            result.put(i, new ArrayList<>());

            for (int j = 0; j < 10; j++) {
                int finalI = i;
                new Thread(() -> {
                    try {
                        result.get(finalI).add(client.read("Luca"));
                    } catch(Exception ignored) {
                        System.out.println(ignored);
                        fail();
                    }
                }).start();
            }
        }

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                assertEquals(result.get(i), result.get(j));
            }
        }

    }
}
