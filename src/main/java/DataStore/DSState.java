package DataStore;

import java.util.HashMap;

public class DSState {
    private HashMap<String, DSElement> dataStore = new HashMap<>();

    public synchronized DSElement read(String key) {
        return dataStore.get(key);
    }

    public synchronized void write(String key, DSElement element) {
        dataStore.put(key, element);
    }
}

