package it.polimi.ds.DataStore;

import it.polimi.ds.Message.Message;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Replica {
    private final String address;
    private final int port;

    public Replica(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public Socket sendMessage(Message message) throws IOException {
        Socket socket = new Socket(address, port);
        final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println(message.toJson());
        return socket;
    }

    public String getAddress() {
        return new String(address);
    }

    public int getPort() {
        return port;
    }
}
