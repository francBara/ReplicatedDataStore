package Message;

import com.google.gson.Gson;

public class Message {
    private String key;
    private String value;
    private int port;
    public MessageType messageType;

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MessageType getType() {
        return messageType;
    }

    public void setQuorum() {
        if (messageType == MessageType.Read) {
            messageType = MessageType.ReadQuorum;
        }
        else if (messageType == MessageType.Write) {
            messageType = MessageType.WriteQuorum;
        }
    }
}
