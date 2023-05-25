package it.polimi.ds.Client;

import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String dataStoreAddress;
    private int dataStorePort;

    public void bind(String address, int port) {
        this.dataStoreAddress = address;
        this.dataStorePort = port;
    }

    public DSElement read(String key) throws IOException {
        final Socket socket = new Socket(dataStoreAddress, dataStorePort);
        final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        final Scanner scanner = new Scanner(socket.getInputStream());

        final Message message = new Message(MessageType.Read);
        message.setKey(key);

        writer.println(message.toJson());

        //TODO: Handle when reply is a KO message

        final DSElement readValue = new Gson().fromJson(scanner.nextLine(), DSElement.class);

        socket.close();

        return readValue;
    }

    public boolean write(String key, String value) throws IOException {
        final Socket socket = new Socket(dataStoreAddress, dataStorePort);
        final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        final Scanner scanner = new Scanner(socket.getInputStream());

        final Message message = new Message(MessageType.Write);
        message.setKey(key);
        message.setValue(value);

        writer.println(message.toJson());
        final Message reply = new Gson().fromJson(scanner.nextLine(), Message.class);

        socket.close();

        return reply.messageType == MessageType.OK;
    }
}
