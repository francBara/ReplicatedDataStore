package it.polimi.ds.DataStore.DataStoreState;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

/**
 * Manages the local storage of values
 */
public class DSState {
    private final HashMap<String, DSElement> dataStore = new HashMap<>();

    /**
     *
     * @param key The key of the value to read
     * @return The element corresponding to the key
     */
    public synchronized DSElement read(String key) {
        final DSElement dsElement = dataStore.get(key);
        if (dsElement == null) {
            return new DSNullElement();
        }
        return dsElement;
    }

    /**
     *
     * @param key The key to write to
     * @param value The value to write
     * @param versionNumber The received lamport clock
     */
    public synchronized void write(String key, String value, int versionNumber) {
        if (dataStore.containsKey(key)) {
            DSElement dsElement = dataStore.get(key);
            dsElement.setValue(value);
            dsElement.setVersionNumber(versionNumber);
        }
        else {
            dataStore.put(key, new DSElement(value, versionNumber + 1));
        }
        writeInFile();
    }

    /**
     *
     * @param key The key to write to
     * @param element The element to write
     */
    public synchronized void write(String key, DSElement element) {
        dataStore.put(key, element);
        writeInFile();
    }

    private void writeInFile() {
        //TODO: File write disabled
        return;
        /*
        new Thread(() -> {
            synchronized (dataStore) {
                try {
                    File stateDirectory = new File("." + File.separator + "state");

                    if (!(stateDirectory).exists()) {
                        stateDirectory.mkdir();
                    }
                    FileWriter fileWriter = new FileWriter("." + File.separator + "state" + File.separator + "data" + this.hashCode() + ".json");
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    fileWriter.write(gson.toJson(dataStore));
                    fileWriter.flush();
                    fileWriter.close();
                } catch(Exception ignored) {}
            }
        }).start();
         */
    }
}

