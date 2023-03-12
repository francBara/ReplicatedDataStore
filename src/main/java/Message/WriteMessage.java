package Message;

public class WriteMessage extends Message {
    public final String key;
    public final String item;
    public boolean isQuorum;

    public WriteMessage(String key, String item, int timestamp, boolean isQuorum) {
        this.key = key;
        this.item = item;
        this.timestamp = timestamp;
        this.isQuorum = isQuorum;
    }

    @Override
    public String toString() {
        if (isQuorum) {
            return "WRITE_Q " + key + " " + item + " " + timestamp;
        }
        return "WRITE " + key + " " + item + " " + timestamp;
    }
}
