package DataStoreState;

/**
 * The element contained in a data store, referenced through a key
 */
public class DSElement {
    private String value;
    private int versionNumber;

    public DSElement(String value) {
        this.value = value;
        this.versionNumber = 0;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    synchronized public void incrementVersionNumber() {
        versionNumber++;
    }
}
