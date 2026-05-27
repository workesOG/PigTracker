// Af Nikolaj Jakobsen

package pigtracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupTest {

    @Test
    void withIdReplacesIdAndKeepsNameAndStatus() {
        Group original = new Group(0, "Adults", Group.CreationStatus.IN_PROGRESS);

        Group copy = original.withId(5);

        assertEquals(5, copy.id());
        assertEquals("Adults", copy.name());
        assertEquals(Group.CreationStatus.IN_PROGRESS, copy.creationStatus());
    }
}
