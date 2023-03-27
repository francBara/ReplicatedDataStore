package Exceptions;

public class QuorumNumberException extends Exception {
    private final int expectedQuorum;
    private final int actualQuorum;

    public QuorumNumberException(int expectedQuorum, int actualQuorum) {
        this.expectedQuorum = expectedQuorum;
        this.actualQuorum = actualQuorum;
    }

    @Override
    public String toString() {
        return "QuorumNumberException\nExpected Quorum: " + expectedQuorum + "\nActual Quorum: " + actualQuorum;
    }
}
