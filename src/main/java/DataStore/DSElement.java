package DataStore;

/**
 * The element contained in a data store, referenced through a key
 */
public class DSElement {
    private final String value;
    private final int timestamp;

    public DSElement(String value, int timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public int getTimestamp() {
        return timestamp;
    }
}
