import it.polimi.ds.Client.Client;
import it.polimi.ds.Client.Exceptions.ReadException;
import it.polimi.ds.DataStore.DataStoreNetwork;
import it.polimi.ds.DataStore.DataStoreState.DSElement;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class SequentialConsistencyTest extends TestCase {
    public void testRaceCondition() {
        DataStoreNetwork coordinator = new DataStoreNetwork(10000);
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
                DataStoreNetwork replica = new DataStoreNetwork(10001 + finalI);
                replicas.add(replica);
                try {
                    replica.joinDataStore("127.0.0.1", 10000);
                } catch(Exception e) {

                }
            }).start();
        }

        try {
            Thread.sleep(3000);
        } catch(Exception ignored) {fail();}


        assertEquals(18, coordinator.getReplicasSize());

        final HashSet<Thread> threads = new HashSet<>();

        AtomicBoolean timerActive = new AtomicBoolean(true);

        threads.add(new Thread(() -> {
            try {
                Thread.sleep(2000);
                timerActive.set(false);
            } catch(InterruptedException e) {fail();}
        }));

        for (int i = 0; i < 10; i++) {
            Client client = new Client();
            int finalI = i;
            int finalI1 = i;
            threads.add(new Thread(() -> {
                try {
                    client.bind("127.0.0.1", 10000 + finalI1);

                    int counter = 0;
                    while (timerActive.get()) {
                        client.write("Luca", counter + "Andrulli" + finalI);
                        counter++;
                    }
                } catch(IOException e) {
                    fail();
                }
            }));
        }

        HashMap<Integer, ArrayList<DSElement>> result = new HashMap<>();

        final int readers = 10;
        final int reads = 10;

        for (int i = 0; i < readers; i++) {
            Client client = new Client();

            result.put(i, new ArrayList<>());

            client.bind("127.0.0.1", 10000 + i);

            for (int j = 0; j < reads; j++) {
                int finalI = i;
                threads.add(new Thread(() -> {
                    try {
                        while (timerActive.get()) {
                            result.get(finalI).add(client.read("Luca"));
                        }
                    } catch(ReadException | IOException e) {
                        fail();
                    }
                }));
            }
        }

        for (Thread thread : threads) {
            thread.start();
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
                    }
                }
            }
        }

    }
}
