package it.polimi.ds.DataStore.Lock;

import java.util.Random;

public class Lock {
    private int lockNonce = 0;
    private LockNotifier lockedBy;


    public synchronized LockNotifier lock(int nonce) {
        if (lockedBy != null && lockedBy.isLocked()) {
            if (!lockedBy.isForceLocked() && nonce > lockNonce) {
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

    public synchronized LockNotifier lockRead() {
        if (lockedBy != null && lockedBy.isLocked()) {
            return new LockNotifier(false, this);
        }
        lockNonce = -1;
        return new LockNotifier(true, this);
    }

    public int generateNonce() {
        return new Random().nextInt(10000);
    }
}
