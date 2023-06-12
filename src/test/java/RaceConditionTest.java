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
        DataStoreNetwork coordinator = new DataStoreNetwork(6000);
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
                DataStoreNetwork replica = new DataStoreNetwork(6001 + finalI);
                replicas.add(replica);
                try {
                    replica.joinDataStore("127.0.0.1", 6000);
                } catch(Exception e) {

                }
            }).start();
        }

        try {
            Thread.sleep(3000);
        } catch(Exception ignored) {fail();}


        assertEquals(18, coordinator.getReplicasSize());


        for (int i = 0; i < 10; i++) {
            Client client = new Client();
            client.bind("127.0.0.1", 6000 + i);
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

        for (int i = 0; i < 15; i++) {
            Client client = new Client();
            client.bind("127.0.0.1", 6000 + i);

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

        try {
            Thread.sleep(3000);
        } catch(Exception ignored) {fail();}

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                System.out.println(result.get(i));
                System.out.println(result.get(j));

                for (int k = 0; k < 10; k++) {
                    for (int h = 0; h < 10; h++) {
                        if (result.get(i).get(k).getVersionNumber() == result.get(j).get(h).getVersionNumber()) {
                            assertEquals(result.get(i).get(k).getValue(), result.get(j).get(h).getValue());
                        }
                    }
                }


            }
        }

    }
}
