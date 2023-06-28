package StructuralTests;

import it.polimi.ds.DataStore.DataStoreState.DSNullElement;
import it.polimi.ds.DataStore.DataStoreState.DSState;
import it.polimi.ds.DataStore.DataStoreState.DSStateException;
import junit.framework.TestCase;

public class DataStoreStateTest extends TestCase {
    public void testDataStoreState() {
        DSState state = new DSState();

        assertEquals(DSNullElement.class.getTypeName(), state.read("Alen").getClass().getTypeName());
        assertTrue(state.read("Alen").isNull());

        for (int i = 0; i < 100; i++) {
            state.write("Alen", "Distributed" + i, 0);
            state.write("Distributed", "Software" + i, 0);
            assertEquals("Distributed" + i, state.read("Alen").getValue());
            assertEquals("Software" + i, state.read("Distributed").getValue());
            assertEquals(i + 1, state.read("Alen").getVersionNumber());
            assertEquals(i + 1, state.read("Distributed").getVersionNumber());
        }
    }
}
