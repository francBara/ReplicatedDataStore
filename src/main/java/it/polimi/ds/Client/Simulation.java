package it.polimi.ds.Client;

import java.io.IOException;

public class Simulation {
    final Client client;

    public Simulation(Client client) {
        this.client = client;
    }

    public void writeMany(int count, String key) {
        writeMany(count, key, "");
    }

    public void writeMany(int count, String key, String value) {
        for (int i = 0; i < count; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    client.write(key, value + finalI);
                } catch (IOException ignored) {}
            }).start();
        }
    }
}
