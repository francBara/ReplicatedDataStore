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

/**
 * The client main class, to execute writes and reads
 */
public class Client {
    private String dataStoreAddress;
    private int dataStorePort;

    /**
     * Locally sets the datastore to communicate to, no network communication is started
     * @param address The IP address of a datastore replica
     * @param port The port of a datastore replica
     */
    public void bind(String address, int port) {
        this.dataStoreAddress = address;
        this.dataStorePort = port;
    }

    /**
     *
     * @param key The key of the element to read
     * @return The DSElement corresponding to the key, with field isNull == True if no element was found
     * @throws IOException If there are errors in communication
     * @throws ReadException If there is an internal error during the reading process
     */
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

    /**
     *
     * @param key The key to write the element to
     * @param value The value to write
     * @return True if write was successful, false if not
     * @throws IOException If there are errors in communication
     */
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

    /**
     * Executes count sequential reads
     * @param key The key to read from
     * @param count The number of reads to execute
     * @return All read elements
     */
    public ArrayList<DSElement> readMany(String key, int count) {
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
     * Executes count sequential writes, the value written will not be fixed, as it will change at every write request
     * @param key The key to write to
     * @param value The value to write
     * @param count The number of writes to execute
     */
    public void writeMany(String key, String value, int count) {
        for (int i = 0; i < count; i++) {
            try {
                write(key, value + i);
            } catch (Exception ignored) {}
        }
    }
}
