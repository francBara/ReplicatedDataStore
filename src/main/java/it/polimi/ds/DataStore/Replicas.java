package it.polimi.ds.DataStore;

import it.polimi.ds.Message.Message;
import it.polimi.ds.Message.MessageType;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;

/**
 * Keeps references to the other replicas in the data store
 */
public class Replicas {
    private final HashSet<Replica> replicas = new HashSet<>();

    public synchronized void addReplica(Replica newReplica) {
        replicas.add(newReplica);
    }

    public synchronized void updateReplicas(Replica newReplica) {
        final Message message = new Message(MessageType.ReplicasUpdate);
        final String replicaJson = new Gson().toJson(newReplica);

        for (Replica replica : replicas) {
            //System.out.println("Connecting to " + replica.getAddress() + " " + replica.getPort());
            try {
                Socket socket = replica.sendMessage(message);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(replicaJson);
                socket.close();
            } catch(IOException e) {
                //System.out.println("Kaja Error on " + replica.getAddress() + " " + replica.getPort() + ": " + e);
            }
        }
    }

    /**
     * Sends a message to a given number of replicas, chosen indeterminately between available replicas
     * @param replicasNumber The number of replicas to contact
     */
    public synchronized HashSet<Socket> sendMessageToBatch(Message message, int replicasNumber) throws IOException {
        final HashSet<Socket> replicasSockets = new HashSet<>();

        int i = 0;
        for (Replica replica : replicas) {
            if (i == replicasNumber) {
                break;
            }

            replicasSockets.add(replica.sendMessage(message));
            i++;
        }

        return replicasSockets;
    }

    public int size() {
        return replicas.size();
    }
}
