import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashSet;

public class DistributedTest extends TestCase {

    public void testInitAndRead() {
        final DSCommunication firstReplica = new DSCommunication(5000);
        final HashSet<DSCommunication> replicasControllers = new HashSet<>();

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
                    final DSCommunication newReplica = new DSCommunication(5001 + finalI);
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

        try {
            Thread.sleep(3000);
        } catch(Exception ignored) {fail();}


        //Every replica should retain 10 references to other replicas
        assertEquals(10, firstReplica.getReplicasSize());
        for (DSCommunication replicaController : replicasControllers) {
            assertEquals(10, replicaController.getReplicasSize());
        }

        final Client client = new Client();
        client.bind("127.0.0.1", 5000);

        try {
            boolean writeSuccessful = client.write("Alen", "Kaja");

            assertTrue(writeSuccessful);

            assertEquals("Kaja", client.read("Alen"));

            client.bind("127.0.0.1", 5001);

            assertEquals("Kaja", client.read("Alen"));

            assertTrue(client.write("Kaja", "Alen"));

            assertEquals("Alen", client.read("Kaja"));

            client.bind("127.0.0.1", 5002);

            assertEquals("Kaja", client.read("Alen"));
            assertEquals("Alen", client.read("Kaja"));

            assertTrue(client.write("Distributed", "Systems"));

            assertEquals("Systems", client.read("Distributed"));

        } catch(Exception e) {
            System.out.println(e);
            fail();
        }
    }
}
