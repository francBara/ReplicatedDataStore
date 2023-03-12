package Message;

public class ReadMessage extends Message {
    public final String key;
    public boolean isQuorum;

    public ReadMessage(String key, int timestamp, boolean isQuorum) {
        this.key = key;
        this.timestamp = timestamp;
        this.isQuorum = isQuorum;
    }

    @Override
    public String toString() {
        if (isQuorum) {
            return "READ_Q " + key + " " + timestamp;
        }
        return "READ " + key + " " + timestamp;
    }
}
