package it.polimi.ds.DataStore.RequestsHandler;

import it.polimi.ds.DataStore.DataStoreState.DSState;
import it.polimi.ds.DataStore.Quorum;
import it.polimi.ds.DataStore.Replica;
import it.polimi.ds.DataStore.Replicas;
import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class CoordinatorHandler extends RequestsHandler {
    public CoordinatorHandler(Replicas replicas, Quorum quorum, DSState dsState, int port) {
        super(replicas, quorum, dsState, port);
    }

    @Override
    public void ackCoordinator() throws IOException {

    }

    @Override
    public void handleJoin(Socket clientSocket, PrintWriter writer, Scanner scanner, Message message) {
        synchronized (this) {

            if (replicas.size() >= quorum.maxReplicas - 1) {
                writer.println(MessageType.KO);
                return;
            }

            writer.println(MessageType.OK);
            final Gson gson = new Gson();
            writer.println(gson.toJson(replicas));
            writer.println(port);
            writer.println(gson.toJson(quorum));

            if (!MessageType.valueOf(scanner.nextLine()).equals(MessageType.OK)) {
                return;
            }

            final Replica replica = new Replica(clientSocket.getInetAddress().toString().substring(1), message.getPort());

            //The new replica is sent to other replicas
            replicas.updateReplicas(replica);
            replicas.addReplica(replica);
        }
    }
}
