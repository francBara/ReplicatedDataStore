package it.polimi.ds.DataStore.RequestsHandler;

import it.polimi.ds.Message.Message;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class WriteQueue {
    private final ArrayList<QueueElement> queue = new ArrayList<>();
    private boolean isQueueProcessing = false;

    public synchronized boolean add(Socket socket, PrintWriter writer, Message message) {
        if (queue.size() >= 10000) {
            return false;
        }
        queue.add(new QueueElement(socket, writer, message));
        return true;
    }

    public synchronized QueueElement get() {
        QueueElement element = queue.get(0);
        queue.remove(0);
        return element;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void setProcessing(boolean isQueueProcessing) {
        this.isQueueProcessing = isQueueProcessing;
    }

    public synchronized boolean isProcessing() {
        return isQueueProcessing;
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