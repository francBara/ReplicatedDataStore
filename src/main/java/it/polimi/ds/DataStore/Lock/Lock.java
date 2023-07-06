package it.polimi.ds.DataStore.Lock;

import java.util.HashMap;
import java.util.Random;

/**
 * Distributed lock manager, for write and read-repair locks
 */
public class Lock {
    //private LockNotifier lockedBy;
    private final HashMap<String, LockNotifier> lockedBy = new HashMap<>();


    /**
     * Requests a write lock, the lock could be granted or not, depending on the previous locks and the given nonce
     * @param nonce If greater than the current lock nonce, the lock will be granted
     * @return A LockNotifier, which tells if the lock was granted or not
     */
    public synchronized LockNotifier lock(int nonce, String key, boolean bully) {
        if (lockedBy.containsKey(key) && lockedBy.get(key).isLocked()) {
            if (bully && !lockedBy.get(key).isForceLocked() && nonce > lockedBy.get(key).nonce) {
                lockedBy.get(key).unlock();
            }
            else {
                return new LockNotifier(false, nonce, this);
            }
        }
        final LockNotifier lockNotifier = new LockNotifier(true, nonce, this);
        lockedBy.put(key, lockNotifier);
        return lockNotifier;
    }

    /**
     * Requests a read lock, the lock could be granted or not, depending on the previous locks. A read lock always gets bullied by a write lock
     * @return A LockNotifier, which tells if the lock was granted or not
     */
    public synchronized LockNotifier lockRead(String key) {
        if (lockedBy.containsKey(key) && lockedBy.get(key).isLocked()) {
            return new LockNotifier(false, -1, this);
        }
        return new LockNotifier(true, -1, this);
    }

    /**
     * Utility method to generate a random nonce
     * @return
     */
    public int generateNonce() {
        return new Random().nextInt(10000);
    }
}
