import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.DataStore.Quorum;
import junit.framework.TestCase;

public class QuorumTest extends TestCase {
    public void testMaxReplicas() {
        try {
            Quorum quorum = new Quorum(10, 10);
            assertEquals(19, quorum.maxReplicas);

            quorum = new Quorum(113, 1);
            assertEquals(113, quorum.maxReplicas);

            quorum = new Quorum(13, 5);
            assertEquals(17, quorum.maxReplicas);

            quorum = new Quorum(1, 1);
            assertEquals(1, quorum.maxReplicas);

        } catch(QuorumNumberException e) {
            fail();
        }
    }

    public void testQuorumException() {
        try {
            new Quorum(0, 0);
            fail();
        } catch(QuorumNumberException ignored) {}

        try {
            new Quorum(1, 0);
            fail();
        } catch(QuorumNumberException ignored) {}

        try {
            new Quorum(0, 1);
            fail();
        } catch(QuorumNumberException ignored) {}

        try {
            new Quorum(-1, -1);
            fail();
        } catch(QuorumNumberException ignored) {}

        try {
            new Quorum(1, 1);
        } catch(QuorumNumberException ignored) {
            fail();
        }
    }
}
