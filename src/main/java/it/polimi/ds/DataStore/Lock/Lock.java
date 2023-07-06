package it.polimi.ds.DataStore.Lock;

import java.util.Random;

/**
 * Distributed lock manager, for write and read-repair locks
 */
public class Lock {
    private int lockNonce = 0;
    private LockNotifier lockedBy;


    /**
     * Requests a write lock, the lock could be granted or not, depending on the previous locks and the given nonce
     * @param nonce If greater than the current lock nonce, the lock will be granted
     * @return A LockNotifier, which tells if the lock was granted or not
     */
    public synchronized LockNotifier lock(int nonce, boolean bully) {
        if (lockedBy != null && lockedBy.isLocked()) {
            if (bully && !lockedBy.isForceLocked() && nonce > lockNonce) {
                lockedBy.unlock();
            }
            else {
                return new LockNotifier(false, this);
            }
        }
        lockNonce = nonce;
        lockedBy = new LockNotifier(true, this);
        return lockedBy;
    }

    /**
     * Requests a read lock, the lock could be granted or not, depending on the previous locks. A read lock always gets bullied by a write lock
     * @return A LockNotifier, which tells if the lock was granted or not
     */
    public synchronized LockNotifier lockRead() {
        if (lockedBy != null && lockedBy.isLocked()) {
            return new LockNotifier(false, this);
        }
        lockNonce = -1;
        return new LockNotifier(true, this);
    }

    /**
     * Utility method to generate a random nonce
     * @return
     */
    public int generateNonce() {
        return new Random().nextInt(10000);
    }
}
