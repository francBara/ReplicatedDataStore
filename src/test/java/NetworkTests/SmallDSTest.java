package NetworkTests;

import it.polimi.ds.DataStore.DataStoreNetwork;
import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.DataStore.Exceptions.FullDataStoreException;
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
                firstReplica.initiateDataStore(1, 1);
            } catch(IOException | QuorumNumberException e) {
                fail();
            }
        }).start();

        delay(1);


        for (int i = 0; i < 100; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    new DataStoreNetwork(7001 + finalI).joinDataStore("127.0.0.1", 7000);
                    fail();
                } catch(IOException | FullDataStoreException ignored) {}
            }).start();
        }


        assertEquals(0, firstReplica.getReplicasSize());
        for (DataStoreNetwork ds : replicas) {
            assertEquals(0, ds.getReplicasSize());
        }

        Client client = new Client();
        client.bind("127.0.0.1", 7000);

        try {
            assertTrue(client.write("Alen", "Kaja"));
            assertEquals("Kaja", client.read("Alen").getValue());
        } catch(Exception ignored) {
            System.out.println(ignored);
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
