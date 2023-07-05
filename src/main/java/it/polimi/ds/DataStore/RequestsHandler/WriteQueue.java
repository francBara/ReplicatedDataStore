package it.polimi.ds.DataStore.RequestsHandler;

import it.polimi.ds.Message.Message;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * ADT representing a queue of client write requests
 */
public class WriteQueue {
    private final ArrayList<QueueElement> queue = new ArrayList<>();
    private boolean isQueueProcessing = false;

    /**
     * Adds a write request to the queue
     * @param socket
     * @param writer
     * @param message
     * @return True if the request was successfully added, false if not
     */
    public synchronized boolean add(Socket socket, PrintWriter writer, Message message) {
        if (queue.size() >= 10000) {
            return false;
        }
        queue.add(new QueueElement(socket, writer, message));
        return true;
    }

    /**
     * Returns the head of the queue, while removing it
     * @return The head of the queue
     */
    public synchronized QueueElement get() {
        if (queue.isEmpty()) {
            return null;
        }
        QueueElement element = queue.get(0);
        queue.remove(0);
        return element;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}

class QueueElement {
    public final Socket socket;
    public final PrintWriter writer;
    public final Message message;

    public QueueElement(Socket socket, PrintWriter writer, Message message) {
        this.socket = socket;
        this.writer = writer;
        this.message = message;
    }
}