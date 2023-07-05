package it.polimi.ds.DataStore;

import it.polimi.ds.Message.Message;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * The local proxy for a data store replica
 */
public class Replica {
    private final String address;
    private final int port;

    /**
     *
     * @param address
     * @param port The port where the replica hosts the data store process
     */
    public Replica(String address, int port) {
        this.address = address;
        this.port = port;
    }

    /**
     * Sends a custom message to the replica, opening a new socket connection
     * @param message
     * @return The socket of the connection
     * @throws IOException If communication errors arise
     */
    public Socket sendMessage(Message message) throws IOException {
        Socket socket;
        try {
            socket = new Socket(address, port);
        } catch(IOException e) {
            //System.out.println("Error on message: " + port);
            //System.out.println(e.getMessage());
            throw(e);
        }
        final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println(message.toJson());
        return socket;
    }
}
