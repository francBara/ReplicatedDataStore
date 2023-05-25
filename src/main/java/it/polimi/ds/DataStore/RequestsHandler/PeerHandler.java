package it.polimi.ds.DataStore.RequestsHandler;

import it.polimi.ds.DataStore.DataStoreState.DSState;
import it.polimi.ds.DataStore.Quorum;
import it.polimi.ds.DataStore.Replicas;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class PeerHandler extends RequestsHandler {
    final Socket coordinatorSocket;

    public PeerHandler(Replicas replicas, Quorum quorum, DSState dsState, int port, Socket coordinatorSocket) {
        super(replicas, quorum, dsState, port);
        this.coordinatorSocket = coordinatorSocket;
    }

    @Override
    public void ackCoordinator() throws IOException  {
        new PrintWriter(coordinatorSocket.getOutputStream(), true).println(MessageType.OK);
        coordinatorSocket.close();
    }

    @Override
    public void handleJoin(Socket clientSocket, PrintWriter writer, Scanner scanner, Message message) {
        writer.println(MessageType.KO);
    }
}
