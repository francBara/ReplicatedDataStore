import DataStore.DataStoreState.DSNullElement;
import DataStore.DataStoreState.DSState;
import DataStore.DataStoreState.DSStateException;
import junit.framework.TestCase;

public class DataStoreStateTest extends TestCase {
    public void testDataStoreState() {
        DSState state = new DSState();

        try {
            assertEquals(DSNullElement.class.getTypeName(), state.read("Alen").getClass().getTypeName());
            assertTrue(state.read("Alen").isNull());

            for (int i = 0; i < 100; i++) {
                state.write("Alen", "Distributed" + i, 0);
                state.write("Distributed", "Software" + i, 0);
                assertEquals("Distributed" + i, state.read("Alen").getValue());
                assertEquals("Software" + i, state.read("Distributed").getValue());
                assertEquals(i, state.read("Alen").getVersionNumber());
                assertEquals(i, state.read("Distributed").getVersionNumber());
            }

        } catch(DSStateException e) {
            fail();
        }
    }
}
