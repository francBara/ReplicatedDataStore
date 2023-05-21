package DataStore.RequestsHandler;

import DataStore.DataStoreState.DSState;
import DataStore.Quorum;
import DataStore.Replica;
import DataStore.Replicas;
import Message.Message;
import Message.MessageType;
import com.google.gson.Gson;

import java.io.PrintWriter;
import java.net.Socket;

public class CoordinatorHandler extends RequestsHandler {
    public CoordinatorHandler(Replicas replicas, Quorum quorum, DSState dsState, int port) {
        super(replicas, quorum, dsState, port);
    }

    @Override
    public void handleJoin(Socket clientSocket, PrintWriter writer, Message message) {
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

            final Replica replica = new Replica(clientSocket.getInetAddress().toString().substring(1), message.getPort());

            //The new replica is sent to other replicas
            replicas.updateReplicas(replica);
            replicas.addReplica(replica);
        }
    }
}
