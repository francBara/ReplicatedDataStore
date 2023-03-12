import Message.Message;
import Message.MessageFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

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
        writer.println(message.toString());
        return socket;
    }

    public String getAddress() {
        return new String(address);
    }
}
