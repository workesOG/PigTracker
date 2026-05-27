// Af Nikolaj Jakobsen

package pigtracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimalTest {

    private static Animal sampleAnimal(int id, Animal.Status status) {
        return new Animal(id, 211863, "984000010111863", 1, 2, status, null, null, 2.4, 30.0, 120.0, 50.0, 80.0, 45,
                LocalDate.of(2026, 4, 8), LocalDateTime.of(2026, 4, 8, 9, 14));
    }

    @Test
    void isActiveIsTrueWhenStatusIsActive() {
        assertTrue(sampleAnimal(1, Animal.Status.ACTIVE).isActive());
    }

    @Test
    void isActiveIsFalseWhenStatusIsStopped() {
        assertFalse(sampleAnimal(1, Animal.Status.STOPPED).isActive());
    }

    @Test
    void withIdReplacesOnlyTheId() {
        Animal copy = sampleAnimal(0, Animal.Status.ACTIVE).withId(99);

        assertEquals(99, copy.id());
        // The rest of the snapshot is carried over unchanged.
        assertEquals(211863, copy.animalNumber());
        assertEquals(Animal.Status.ACTIVE, copy.status());
        assertEquals(2.4, copy.fcr());
        assertEquals(80.0, copy.latestWeightKg());
        assertEquals(LocalDate.of(2026, 4, 8), copy.startDay());
    }
}
