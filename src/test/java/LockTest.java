import it.polimi.ds.DataStore.Lock.Lock;
import it.polimi.ds.DataStore.Lock.LockNotifier;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

public class LockTest extends TestCase {
    public void testLock() {
        final Lock lock = new Lock();

        LockNotifier notifier = lock.lock(100);

        assertTrue(notifier.isLocked());

        assertFalse(lock.lock(0).isLocked());

        assertTrue(notifier.isLocked());

        notifier.unlock();

        notifier = lock.lock(0);

        assertTrue(notifier.isLocked());

        LockNotifier notifier2 = lock.lock(200);

        assertTrue(notifier2.isLocked());

        assertFalse(notifier.isLocked());
    }

    public void testConcurrency() {
        final Lock lock = new Lock();

        HashMap<Integer, LockNotifier> notifiers = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            new Thread(() -> {
                notifiers.put(finalI, lock.lock(finalI));
            }).start();
        }

        try {
            Thread.sleep(200);
        } catch(InterruptedException ignored) {fail();}

        for (int i = 0; i < 99; i++) {
            assertFalse(notifiers.get(i).isLocked());
        }
        assertTrue(notifiers.get(99).isLocked());
    }
}
