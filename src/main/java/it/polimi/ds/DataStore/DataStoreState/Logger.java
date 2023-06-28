package it.polimi.ds.DataStore.DataStoreState;

public class Logger {
    static public int counter = 0;

    static synchronized public void addCounter() {
        counter++;
    }
}
