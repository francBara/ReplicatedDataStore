package it.polimi.ds.DataStore.Lock;

/**
 * Notifier given after a lock attempt on the Lock class, it's updated to its actual lock state
 */
public class LockNotifier {
    private boolean isLocked;
    private boolean isForceLocked = false;
    private final Lock lock;

    public LockNotifier(boolean isLocked, Lock lock) {
        this.isLocked = isLocked;
        this.lock = lock;
    }

    /**
     * Frees the lock and an eventual strong lock, the Lock object will now accept any following lock request
     */
    public void unlock() {
        this.isLocked = false;
    }

    /**
     * Attempts a strong lock, if successful the lock can not be bullied
     * @return True if the force lock was successful, false if not
     */
    public boolean forceLock() {
        synchronized (lock) {
            if (!isLocked) {
                return false;
            }
            isForceLocked = true;
            return true;
        }
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean isForceLocked() {
        return isForceLocked;
    }

    @Override
    public String toString() {
        return isLocked ? "locked" : "unlocked";
    }
}
