package StructuralTests;

import it.polimi.ds.DataStore.RequestsHandler.WriteQueue;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import junit.framework.TestCase;

import java.net.Socket;

public class BasicQueueTest extends TestCase {
    public void testWriteQueue() {
        final WriteQueue writeQueue = new WriteQueue();

        assertTrue(writeQueue.isEmpty());

        Socket socket = null;

        try {
            writeQueue.add(socket, null, new Message(MessageType.KO));

            assertFalse(writeQueue.isEmpty());

            for (int i = 0; i < 10; i++) {
                writeQueue.add(socket, null, new Message(MessageType.OK));
            }

            assertFalse(writeQueue.isEmpty());

            for (int i = 0; i < 10; i++) {
                writeQueue.get();
            }

            assertFalse(writeQueue.isEmpty());

            writeQueue.get();

            assertTrue(writeQueue.isEmpty());

        } catch (Exception e) {
            System.out.println(e);
            fail();
        }
    }
}
