import DataStore.DataStoreNetwork;
import DataStore.DataStoreState.DSElement;
import DataStore.Exceptions.QuorumNumberException;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class ConcurrencyTest extends TestCase {
    public void testConcurrency() {
        DataStoreNetwork firstReplica = new DataStoreNetwork(6000);
        HashSet<DataStoreNetwork> replicas = new HashSet<>();

        new Thread(() -> {
            try {
                firstReplica.initiateDataStore(11, 11);
            } catch(IOException | QuorumNumberException e) {
                fail();
            }
        }).start();

        delay(1);

        for (int i = 0; i < 20; i++) {
            replicas.add(new DataStoreNetwork(6001 + i));
        }

        //All replicas join concurrently
        for (DataStoreNetwork ds : replicas) {
            new Thread(() -> {
                try {
                    ds.joinDataStore("127.0.0.1", 6000);
                } catch(IOException e) {
                    fail();
                }
            }).start();
        }

        delay(1);

        assertEquals(20, firstReplica.getReplicasSize());
        for (DataStoreNetwork ds : replicas) {
            assertEquals(20, ds.getReplicasSize());
        }

        ArrayList<Client> clients = new ArrayList();
        for (int i = 0; i < 10; i++) {
            final Client client = new Client();
            client.bind("127.0.0.1", 6000 + i * 2);
            clients.add(client);
        }

        //All clients write concurrently
        int i = 0;
        for (Client client: clients) {
            int finalI = i;
            new Thread(() -> {
                try {
                    client.write("Key", "Value" + finalI);
                } catch(IOException e) {
                    fail();
                }
            }).start();
            i++;
        }

        delay(2);

        //All clients read the same value concurrently
        try {
            DSElement readValue = clients.get(0).read("Key");

            HashSet<DSElement> readValues = new HashSet<>();

            for (Client client : clients) {
                new Thread(() -> {
                    try {
                        readValues.add(client.read("Key"));
                    } catch(IOException e) {fail();}
                }).start();
            }

            for (DSElement element : readValues) {
                assertEquals(readValue, element);
            }
        } catch(IOException e) {
            fail();
        }
    }

    private void delay(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch(Exception e) {
            fail();
        }
    }
}
