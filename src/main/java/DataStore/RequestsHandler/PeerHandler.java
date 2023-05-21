package DataStore.RequestsHandler;

import DataStore.DataStoreState.DSState;
import DataStore.Quorum;
import DataStore.Replicas;
import Message.Message;
import Message.MessageType;
import java.io.PrintWriter;
import java.net.Socket;

public class PeerHandler extends RequestsHandler {

    public PeerHandler(Replicas replicas, Quorum quorum, DSState dsState, int port) {
        super(replicas, quorum, dsState, port);
    }

    @Override
    public void handleJoin(Socket clientSocket, PrintWriter writer, Message message) {
        writer.println(MessageType.KO);
    }

}
