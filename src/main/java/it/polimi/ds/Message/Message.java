package it.polimi.ds.Message;
import com.google.gson.Gson;


public class Message {
    private String key;
    private String value;
    private int versionNumber;

    private int nonce;

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

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
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
