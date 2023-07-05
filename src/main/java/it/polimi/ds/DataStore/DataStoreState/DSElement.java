package it.polimi.ds.DataStore.DataStoreState;

/**
 * The element contained in a data store, referenced by a key
 */
public class DSElement {
    private String value;
    private int versionNumber;
    protected boolean isNull = false;

    public DSElement(String value, int versionNumber) {
        this.value = value;
        this.versionNumber = versionNumber;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public boolean isNull() {
        return isNull;
    }

    /**
     * Sets a new version number for the element, while guaranteeing lamport clock coherence
     * @param versionNumber
     */
    synchronized public void setVersionNumber(int versionNumber) {
        if (versionNumber > this.versionNumber) {
            this.versionNumber = versionNumber + 1;
        }
        else {
            this.versionNumber++;
        }
    }

    @Override
    public String toString() {
        if (isNull) {
            return "null value, v: NA";
        }
        return value + ", v: " + versionNumber;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DSElement otherElement)) {
            return false;
        }
        return value.equals(otherElement.value) && versionNumber == otherElement.getVersionNumber() && isNull == otherElement.isNull();
    }
}
