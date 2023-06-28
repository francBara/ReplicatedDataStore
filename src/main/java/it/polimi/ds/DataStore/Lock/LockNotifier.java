package it.polimi.ds.DataStore.Lock;

public class LockNotifier {
    private boolean isLocked;
    private boolean isForceLocked = false;
    private final Lock lock;

    public LockNotifier(boolean isLocked, Lock lock) {
        this.isLocked = isLocked;
        this.lock = lock;
    }

    public void unlock() {
        this.isLocked = false;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean forceLock() {
        synchronized (lock) {
            if (!isLocked) {
                return false;
            }
            isForceLocked = true;
            return true;
        }
    }

    public boolean isForceLocked() {
        return isForceLocked;
    }

    @Override
    public String toString() {
        return isLocked ? "locked" : "unlocked";
    }
}
