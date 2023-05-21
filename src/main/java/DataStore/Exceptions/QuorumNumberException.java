package DataStore.Exceptions;

public class QuorumNumberException extends Exception {
    private int expectedQuorum;
    private int actualQuorum;

    public QuorumNumberException(int expectedQuorum, int actualQuorum) {
        this.expectedQuorum = expectedQuorum;
        this.actualQuorum = actualQuorum;
    }
    public QuorumNumberException() {}

    @Override
    public String toString() {
        return "QuorumNumberException\nExpected DataStore.Quorum: " + expectedQuorum + "\nActual DataStore.Quorum: " + actualQuorum;
    }
}
