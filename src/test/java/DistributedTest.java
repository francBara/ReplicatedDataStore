import DataStore.DataStoreNetwork;
import DataStore.Exceptions.FullDataStoreException;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.HashSet;

public class DistributedTest extends TestCase {

    public void testInitAndRead() {
        final DataStoreNetwork firstReplica = new DataStoreNetwork(5000);
        final HashSet<DataStoreNetwork> replicasControllers = new HashSet<>();

        new Thread(() -> {
            try {
                firstReplica.initiateDataStore(6, 6);
            } catch(Exception ignored) {
                fail();
            }
        }).start();

        try {
            Thread.sleep(500);
        } catch(Exception ignored) {fail();}

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    final DataStoreNetwork newReplica = new DataStoreNetwork(5001 + finalI);
                    replicasControllers.add(newReplica);
                    newReplica.joinDataStore("127.0.0.1", 5000);
                } catch(Exception e) {
                    System.out.println(e);
                    fail();
                }
            }).start();
            try {
                Thread.sleep(400);
            } catch(Exception ignored) {fail();}
        }

        for (int i = 10; i < 30; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    new DataStoreNetwork(5001 + finalI).joinDataStore("127.0.0.1", 5000);
                    fail();
                } catch(FullDataStoreException ignored) {} catch(IOException ignored) {fail();}
            }).start();
        }

        try {
            Thread.sleep(3000);
        } catch(Exception ignored) {fail();}


        //Every replica should retain 10 references to other replicas
        assertEquals(10, firstReplica.getReplicasSize());
        for (DataStoreNetwork replicaController : replicasControllers) {
            assertEquals(10, replicaController.getReplicasSize());
        }

        final Client client = new Client();
        client.bind("127.0.0.1", 5000);

        try {
            assertTrue(client.write("Alen", "Kaja"));

            for (int i = 0; i < 11; i++) {
                client.bind("127.0.0.1", 5000 + i);
                assertEquals("Kaja", client.read("Alen"));
            }

            client.bind("127.0.0.1", 5001);

            assertTrue(client.write("Kaja", "Alen"));
            assertTrue(client.write("Distributed", "Systems"));

            for (int i = 0; i < 11; i++) {
                client.bind("127.0.0.1", 5000 + i);
                assertEquals("Alen", client.read("Kaja"));
                assertEquals("Kaja", client.read("Alen"));
                assertEquals("Systems", client.read("Distributed"));
            }

            client.bind("127.0.0.1", 5007);

            assertTrue(client.write("Distributed", "Software"));
            assertTrue(client.write("Alen", "Luca"));
            assertTrue(client.write("Kaja", "Arjel"));

            for (int i = 0; i < 11; i++) {
                client.bind("127.0.0.1", 5000 + i);
                assertEquals("Luca", client.read("Alen"));
                assertEquals("Arjel", client.read("Kaja"));
                assertEquals("Software", client.read("Distributed"));
            }

        } catch(Exception e) {
            System.out.println(e);
            fail();
        }
    }
}
