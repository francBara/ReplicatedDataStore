import it.polimi.ds.DataStore.DataStoreNetwork;
import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.Client.Client;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class SmallDSTest extends TestCase {
    public void testConcurrency() {
        DataStoreNetwork firstReplica = new DataStoreNetwork(7000);
        HashSet<DataStoreNetwork> replicas = new HashSet<>();

        new Thread(() -> {
            try {
                firstReplica.initiateDataStore(100, 100);
            } catch(IOException | QuorumNumberException e) {
                fail();
            }
        }).start();

        delay(1);

        for (int i = 0; i < 20; i++) {
            replicas.add(new DataStoreNetwork(7001 + i));
        }

        //All replicas join concurrently
        for (DataStoreNetwork ds : replicas) {
            new Thread(() -> {
                try {
                    ds.joinDataStore("127.0.0.1", 7000);
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

        Client client = new Client();
        client.bind("127.0.0.1", 7000);

        try {
            client.write("Alen", "Kaja");
            assertEquals("Kaja", client.read("Alen").getValue());
        } catch(Exception ignored) {
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
