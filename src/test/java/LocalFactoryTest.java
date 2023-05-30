import it.polimi.ds.Client.Client;
import it.polimi.ds.DataStore.DataStoreNetwork;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.DataStore.LocalReplicasFactory;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.HashSet;

public class LocalFactoryTest extends TestCase {
    public void testLocalFactory() {
        LocalReplicasFactory factory = new LocalReplicasFactory();

        DataStoreNetwork coordinator = new DataStoreNetwork(8000);

        new Thread(() -> {
            try {
                coordinator.initiateDataStore(113, 10);
            } catch (Exception ignored) {}
        }).start();

        delay(1);

        HashSet<DataStoreNetwork> ds = new HashSet<>();

        try {
            ds = factory.getReplicas("127.0.0.1", 8000, 8001, 100);
        } catch(Exception ignored) {
            fail();
        }

        delay(4);

        assertEquals(100, coordinator.getReplicasSize());

        Client client = new Client();
        client.bind("127.0.0.1", 8070);

        try {
            assertTrue(client.write("Alen", "Kaja"));
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
