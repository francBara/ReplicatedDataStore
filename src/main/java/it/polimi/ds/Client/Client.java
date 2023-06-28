package it.polimi.ds.Client;

import it.polimi.ds.Client.Exceptions.ReadException;
import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    private String dataStoreAddress;
    private int dataStorePort;

    public void bind(String address, int port) {
        this.dataStoreAddress = address;
        this.dataStorePort = port;
    }

    public DSElement read(String key) throws IOException, ReadException {
        final Socket socket = new Socket(dataStoreAddress, dataStorePort);
        final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        final Scanner scanner = new Scanner(socket.getInputStream());

        final Message message = new Message(MessageType.Read);
        message.setKey(key);

        writer.println(message.toJson());

        final MessageType readSuccessful = MessageType.valueOf(scanner.nextLine());

        if (readSuccessful == MessageType.KO) {
            socket.close();
            throw(new ReadException());
        }

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

        final MessageType reply = MessageType.valueOf(scanner.nextLine());

        socket.close();

        return reply == MessageType.OK;
    }

    public ArrayList<DSElement> readMany(int count, String key) {
        final ArrayList<DSElement> elements = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            try {
                try {
                    elements.add(read(key));
                } catch (Exception ignored) {}
            } catch(Exception ignored) {}
        }

        return elements;
    }

    /**
     * Writes the value sequentially
     * @param count
     * @param key
     * @param value
     */
    public void writeMany(int count, String key, String value) {
        for (int i = 0; i < count; i++) {
            try {
                try {
                    write(key, value + i);
                } catch (Exception ignored) {}
            } catch(Exception ignored) {}
        }
    }
}
