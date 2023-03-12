import DataStore.DSElement;
import Message.ReadMessage;
import Message.WriteMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Quorum {
    public boolean initWriteQuorum(WriteMessage message, Replicas replicas, int quorum) {
        //TODO: Implement method
        if (quorum > replicas.size()) {
            throw(new RuntimeException());
        }
        return false;
    }

    public DSElement initReadQuorum(ReadMessage message, Replicas replicas, int quorum) throws IOException {
        if (quorum > replicas.size()) {
            throw(new RuntimeException());
        }

        final Set<Socket> quorumReplicas = new HashSet<>();

        message.isQuorum = true;

        int i = 0;
        for (Replica replica : replicas.getReplicas()) {
            if (i == quorum) {
                break;
            }
            quorumReplicas.add(replica.sendMessage(message));
            i++;
        }

        Scanner scanner;
        Gson gson = new Gson();
        DSElement currentReadElement = null;
        for (Socket socket : quorumReplicas) {
            //TODO: Should implement timeouts
            scanner = new Scanner(socket.getInputStream());
            DSElement readElement = gson.fromJson(scanner.nextLine(), DSElement.class);
            if (currentReadElement == null || readElement.getTimestamp() > currentReadElement.getTimestamp()) {
                currentReadElement = readElement;
            }
        }

        return currentReadElement;
    }
}
