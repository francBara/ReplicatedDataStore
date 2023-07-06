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
        final int startingPort = 12000;

        DataStoreNetwork coordinator = new DataStoreNetwork(startingPort);
        HashSet<DataStoreNetwork> replicas = new HashSet<>();

        new Thread(() -> {
            try {
                coordinator.initiateDataStore(10, 10);
            } catch(Exception e) {
                System.out.println(e);
                fail();
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch(InterruptedException ignored) {}

        for (int i = 0; i < 18; i++) {
            int finalI = i;
            new Thread(() -> {
                DataStoreNetwork replica = new DataStoreNetwork(startingPort + 1 + finalI);
                replicas.add(replica);
                try {
                    replica.joinDataStore("127.0.0.1", startingPort);
                } catch(Exception e) {
                    fail();
                }
            }).start();
        }

        try {
            Thread.sleep(3000);
        } catch(Exception ignored) {fail();}


        assertEquals(18, coordinator.getReplicasSize());

        final HashSet<Thread> writeThreads = new HashSet<>();

        final AtomicBoolean timerActive = new AtomicBoolean(true);

        writeThreads.add(new Thread(() -> {
            try {
                Thread.sleep(2000);
                timerActive.set(false);
            } catch(InterruptedException e) {fail();}
        }));

        for (int i = 0; i < 10; i++) {
            Client client = new Client();
            int finalI = i;
            int finalI1 = i;
            writeThreads.add(new Thread(() -> {
                try {
                    client.bind("127.0.0.1", startingPort + finalI1);

                    int counter = 0;
                    while (timerActive.get()) {
                        client.write("Luca", counter + "Andrulli" + finalI);
                        counter++;
                    }
                } catch(IOException e) {
                    System.out.println("Error: " + e);
                    //fail();
                }
            }));
        }

        HashMap<Integer, ArrayList<DSElement>> result = new HashMap<>();

        final HashMap<Integer, ArrayList<Thread>> readThreads = new HashMap<>();

        final int readers = 10;
        final int reads = 1;

        for (int i = 0; i < readers; i++) {
            Client client = new Client();

            result.put(i, new ArrayList<>());

            client.bind("127.0.0.1", startingPort + (i % 18));

            int finalI = i;
            readThreads.put(i, new ArrayList<>());

            for (int j = 0; j < reads; j++) {
                readThreads.get(finalI).add(new Thread(() -> {
                    try {
                        while (timerActive.get()) {
                            result.get(finalI).add(client.read("Luca"));
                        }
                    } catch(ReadException | IOException e) {
                        System.out.println("Error: " + e);
                        //fail();
                    }
                }));
            }
        }

        for (Thread thread : writeThreads) {
            thread.start();
        }
        for (int reader : readThreads.keySet()) {
            new Thread(() -> {
                for (Thread thread : readThreads.get(reader)) {
                    thread.start();
                    //delay(100);
                }
            }).start();
        }

        delay(3000);

        boolean atLeastOneNonNull = false;

        for (int i = 0; i < readers; i++) {

        }

        for (int i = 0; i < readers; i++) {
            printSequence(result.get(i));

            int lastVersion = 0;

            //Checks that every read in a sequential read is monotonically increasing
            for (int j = 0; j < result.get(i).size(); j++) {
                assertTrue(result.get(i).get(j).getVersionNumber() >= lastVersion);
                lastVersion = result.get(i).get(j).getVersionNumber();
            }


            for (int j = i; j < readers; j++) {
                //All readers must have same values for same version numbers, and vice-versa
                for (int k = 0; k < result.get(i).size(); k++) {
                    for (int h = k; h < result.get(j).size(); h++) {
                        if (result.get(i).get(k).getVersionNumber() == result.get(j).get(h).getVersionNumber()) {
                            assertEquals(result.get(i).get(k).getValue(), result.get(j).get(h).getValue());
                        }
                    }
                    if (!result.get(i).get(k).isNull()) {
                        atLeastOneNonNull = true;
                    }
                }
            }
        }

        assertTrue(atLeastOneNonNull);

    }

    private void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException e) {
            fail();
        }
    }

    private void printSequence(ArrayList<DSElement> sequence) {
        for (DSElement element : sequence) {
            System.out.println(element);
        }
        System.out.println();
    }
}
