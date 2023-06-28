package StructuralTests;

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

        LockNotifier notifier3 = lock.lock(500);

        assertTrue(notifier3.isLocked());
        assertFalse(notifier2.isLocked());
        assertFalse(notifier.isLocked());
        assertFalse(notifier.isForceLocked());
        assertFalse(notifier2.isForceLocked());
        assertFalse(notifier3.isForceLocked());

        assertFalse(notifier.forceLock());
        assertFalse(notifier.isForceLocked());

        assertTrue(notifier3.forceLock());
        assertTrue(notifier3.isForceLocked());

        assertFalse(lock.lock(100000).isLocked());

        assertTrue(notifier3.isLocked());
        assertTrue(notifier3.isForceLocked());
        notifier3.unlock();

        assertFalse(notifier3.isLocked());
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

    public void testReadLock() {
        final Lock lock = new Lock();

        LockNotifier notifier = lock.lock(0);

        assertTrue(notifier.isLocked());

        assertFalse(lock.lockRead().isLocked());

        notifier.unlock();

        assertFalse(notifier.isLocked());

        assertTrue(lock.lockRead().isLocked());

        assertTrue(lock.lock(0).isLocked());

        assertFalse(lock.lockRead().isLocked());
    }
}
