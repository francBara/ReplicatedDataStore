package it.polimi.ds.DataStore.Lock;

import java.util.Random;

public class Lock {
    private int lockNonce = 0;
    private LockNotifier lockedBy;

    public synchronized LockNotifier lock(int nonce) {
        if (lockedBy != null && lockedBy.isLocked()) {
            if (nonce > lockNonce) {
                lockedBy.unlock();
            }
            else {
                return new LockNotifier(false);
            }
        }
        lockNonce = nonce;
        lockedBy = new LockNotifier(true);
        return lockedBy;
    }

    public int generateNonce() {
        return new Random().nextInt(10000);
    }
}
