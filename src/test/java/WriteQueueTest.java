import it.polimi.ds.Client.Client;
import it.polimi.ds.DataStore.DataStoreNetwork;
import it.polimi.ds.DataStore.LocalReplicasFactory;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.HashSet;

public class WriteQueueTest extends TestCase {
    public void testWriteQueue() {
        final DataStoreNetwork firstReplica = new DataStoreNetwork(16000);

        new Thread(() -> {
            try {
                firstReplica.initiateDataStore(7, 7);
            } catch (Exception e) {
                fail();
            }
        }).start();

        delay(1000);

       final HashSet<DataStoreNetwork> replicas = new LocalReplicasFactory().getReplicas("127.0.0.1", 16000, 16001, 12);

       delay(3000);

       final Client client = new Client();

       client.bind("127.0.0.1", 16005);

       for (int i = 0; i < 20; i++) {
           new Thread(() -> {
               try {
                   client.write("Alen", "Kaja");
                   //assertTrue(client.write("Alen", "Kaja"));
               } catch(Exception e) {
                   System.out.println(e);
                   fail();
               }
           }).start();
       }

       delay(3000);

       try {
           assertEquals("Kaja", client.read("Alen").getValue());
       } catch(Exception e) {
           fail();
       }
    }

    private void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(Exception e) {fail();}
    }
}
