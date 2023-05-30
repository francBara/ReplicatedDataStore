package it.polimi.ds.DataStore.DataStoreState;

import java.util.HashMap;

/**
 * Manages the actual local datastore of a single replica
 */
public class DSState {
    //TODO: Read and write should probably use a file as permanent storage, and dataStore field for caching
    private HashMap<String, DSElement> dataStore = new HashMap<>();

    /**
     *
     * @param key
     * @return
     * @throws DSStateException If some internal error occurs during reading
     */
    public synchronized DSElement read(String key) throws DSStateException {
        //TODO: Should manage the case in which an element is not stored in the state, maybe with an exception
        final DSElement dsElement = dataStore.get(key);
        if (dsElement == null) {
            return new DSNullElement();
        }
        return dsElement;
    }

    /**
     *
     * @param key
     * @param value
     * @throws DSStateException If some internal error occurs during writing
     */
    public synchronized void write(String key, String value, int versionNumber) throws DSStateException {
        if (dataStore.containsKey(key)) {
            DSElement dsElement = dataStore.get(key);
            dsElement.setValue(value);
            dsElement.setVersionNumber(versionNumber);
        }
        else {
            dataStore.put(key, new DSElement(value, versionNumber + 1));
        }
    }

    public synchronized void write(String key, DSElement element) throws DSStateException {
        dataStore.put(key, element);
    }
}

