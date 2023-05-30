import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.DataStore.DataStoreState.DSState;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.DataStore.Quorum;
import junit.framework.TestCase;

public class QuorumTest extends TestCase {
    public void testMaxReplicas() {
        try {
            DSState dsState = new DSState();

            Quorum quorum = new Quorum(10, 10, dsState);
            assertEquals(19, quorum.maxReplicas);

            quorum = new Quorum(113, 1, dsState);
            assertEquals(113, quorum.maxReplicas);

            quorum = new Quorum(13, 5, dsState);
            assertEquals(17, quorum.maxReplicas);

            quorum = new Quorum(1, 1, dsState);
            assertEquals(1, quorum.maxReplicas);

            quorum = new Quorum(5, 200, dsState);
            assertEquals(9, quorum.maxReplicas);

        } catch(QuorumNumberException e) {
            fail();
        }
    }

    public void testQuorumException() {
        DSState dsState = new DSState();

        try {
            new Quorum(0, 0, dsState);
            fail();
        } catch(QuorumNumberException ignored) {}

        try {
            new Quorum(1, 0, dsState);
            fail();
        } catch(QuorumNumberException ignored) {}

        try {
            new Quorum(0, 1, dsState);
            fail();
        } catch(QuorumNumberException ignored) {}

        try {
            new Quorum(-1, -1, dsState);
            fail();
        } catch(QuorumNumberException ignored) {}

        try {
            new Quorum(1, 1, dsState);
        } catch(QuorumNumberException ignored) {
            fail();
        }
    }
}
