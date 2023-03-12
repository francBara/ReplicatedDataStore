package Message;

public class JoinMessage extends Message {
    public final int port;

    public JoinMessage(int port, int timestamp) {
        this.port = port;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "JOIN " + port + " " + timestamp;
    }
}
