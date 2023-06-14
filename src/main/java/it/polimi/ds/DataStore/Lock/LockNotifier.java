package it.polimi.ds.DataStore.Lock;

public class LockNotifier {
    private boolean isLocked;

    public LockNotifier(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public void unlock() {
        this.isLocked = false;
    }

    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public String toString() {
        return isLocked ? "locked" : "unlocked";
    }
}
