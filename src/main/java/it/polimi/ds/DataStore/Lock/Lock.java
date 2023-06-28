package it.polimi.ds.DataStore.Lock;

import java.util.Random;

public class Lock {
    private int lockNonce = 0;
    private LockNotifier lockedBy;


    private int readLocked = 0;

    public synchronized LockNotifier lock(int nonce) {
        if (readLocked > 0) {
            return new LockNotifier(false, this);
        }
        else if (lockedBy != null && lockedBy.isLocked()) {
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

    public synchronized boolean lockRead() {
        if (lockedBy != null && lockedBy.isLocked()) {
            return false;
        }
        readLocked += 1;
        return true;
    }

    public synchronized void unlockRead() {
        readLocked -= 1;
    }

    public int generateNonce() {
        return new Random().nextInt(10000);
    }
}
