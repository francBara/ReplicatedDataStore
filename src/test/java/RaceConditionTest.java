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

        for (int i = 0; i < 18; i++) {
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
            int finalI = i;
            int finalI1 = i;
            new Thread(() -> {
                try {
                    client.bind("127.0.0.1", 6000 + finalI1);
                    client.write("Luca", "Andrulli" + finalI);
                } catch(IOException e) {
                    fail();
                }
            }).start();
        }

        try {
            Thread.sleep(4000);
        } catch(Exception ignored) {fail();}

        HashMap<Integer, ArrayList<DSElement>> result = new HashMap<>();

        final int readers = 10;
        final int reads = 10;

        for (int i = 0; i < readers; i++) {
            Client client = new Client();

            result.put(i, new ArrayList<>());

            client.bind("127.0.0.1", 6000 + i);

            for (int j = 0; j < reads; j++) {
                int finalI = i;
                new Thread(() -> {
                    try {
                        result.get(finalI).add(client.read("Luca"));
                    } catch(ReadException | IOException e) {
                        fail();
                    }
                }).start();
            }
        }

        try {
            Thread.sleep(3000);
        } catch(Exception ignored) {fail();}

        for (int i = 0; i < readers; i++) {
            System.out.println(result.get(i));

            int lastVersion = 0;

            //Checks that every read in a sequential read is monotonically increasing
            for (int j = 0; j < reads; j++) {
                assertTrue(result.get(i).get(j).getVersionNumber() >= lastVersion);
                lastVersion = result.get(i).get(j).getVersionNumber();
            }


            for (int j = i; j < readers; j++) {
                //All readers must have same values for same version numbers, and vice-versa
                for (int k = 0; k < reads; k++) {
                    for (int h = k; h < reads; h++) {
                        if (result.get(i).get(k).getVersionNumber() == result.get(j).get(h).getVersionNumber()) {
                            assertEquals(result.get(i).get(k).getValue(), result.get(j).get(h).getValue());
                        }
                        if (result.get(i).get(k).getValue().equals(result.get(j).get(h).getValue())) {
                            assertEquals(result.get(i).get(k).getVersionNumber(), result.get(j).get(h).getVersionNumber());
                        }
                    }
                }
            }
        }

    }
}
