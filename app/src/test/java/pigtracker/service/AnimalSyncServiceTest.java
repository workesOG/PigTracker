package pigtracker.service;

import org.junit.jupiter.api.Test;
import pigtracker.model.Animal;
import pigtracker.model.Visit;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AnimalSyncServiceTest {
    @Test
    void buildAnimalForPeriodReturnsNullForNullVisits() throws SQLException {
        assertNull(AnimalSyncService.buildAnimalForPeriod(101, null));
    }

    @Test
    void buildAnimalForPeriodReturnsNullForEmptyVisits() throws SQLException {
        assertNull(AnimalSyncService.buildAnimalForPeriod(101, List.of()));
    }

    @Test
    void buildAnimalForPeriodCalculatesDerivedAnimalMetrics() throws SQLException {
        List<Visit> visits = List.of(visit(LocalDateTime.of(2026, 5, 26, 14, 30), 54000, 3000),
                visit(LocalDateTime.of(2026, 5, 25, 8, 15), 50000, 2000),
                visit(LocalDateTime.of(2026, 5, 26, 9, 0), 52000, 2500));

        Animal animal = AnimalSyncService.buildAnimalForPeriod(101, visits);

        assertEquals(0, animal.id());
        assertEquals(101, animal.animalNumber());
        assertEquals("RFID-101", animal.responder());
        assertEquals(7, animal.location());
        assertEquals(Animal.Status.ACTIVE, animal.status());
        assertEquals(LocalDate.of(2026, 5, 25), animal.startDay());
        assertEquals(2, animal.completedDays());
        assertEquals(50.0, animal.startWeightKg(), 1e-9);
        assertEquals(54.0, animal.latestWeightKg(), 1e-9);
        assertEquals(7.5, animal.totalFeedKg(), 1e-9);
        assertEquals(4.0, animal.weightGainKg(), 1e-9);
        assertEquals(1.875, animal.fcr(), 1e-9);
        assertNull(animal.stoppedReason());
        assertNull(animal.stoppedAt());
        assertNull(animal.createdAt());
    }

    @Test
    void buildAnimalForPeriodIgnoresZeroWeights() throws SQLException {
        List<Visit> visits = List.of(visit(LocalDateTime.of(2026, 5, 25, 8, 15), 0, 2000),
                visit(LocalDateTime.of(2026, 5, 25, 12, 0), 50000, 1000),
                visit(LocalDateTime.of(2026, 5, 26, 14, 30), 0, 3000),
                visit(LocalDateTime.of(2026, 5, 26, 9, 0), 53000, 2500));

        Animal animal = AnimalSyncService.buildAnimalForPeriod(101, visits);

        assertEquals(50.0, animal.startWeightKg(), 1e-9);
        assertEquals(53.0, animal.latestWeightKg(), 1e-9);
        assertEquals(3.0, animal.weightGainKg(), 1e-9);
        assertEquals(8.5, animal.totalFeedKg(), 1e-9);
        assertEquals(8.5 / 3.0, animal.fcr(), 1e-9);
    }

    @Test
    void buildAnimalForPeriodLeavesFcrNullWhenWeightGainIsMissing() throws SQLException {
        List<Visit> visits = List.of(visit(LocalDateTime.of(2026, 5, 25, 8, 15), 0, 2000),
                visit(LocalDateTime.of(2026, 5, 26, 9, 0), 0, 2500));

        Animal animal = AnimalSyncService.buildAnimalForPeriod(101, visits);

        assertNull(animal.startWeightKg());
        assertNull(animal.latestWeightKg());
        assertNull(animal.weightGainKg());
        assertNull(animal.fcr());
        assertEquals(4.5, animal.totalFeedKg(), 1e-9);
    }

    @Test
    void buildAnimalForPeriodLeavesFcrNullWhenWeightGainIsZeroOrNegative() throws SQLException {
        Animal zeroGain = AnimalSyncService.buildAnimalForPeriod(101,
                List.of(visit(LocalDateTime.of(2026, 5, 25, 8, 15), 50000, 2000),
                        visit(LocalDateTime.of(2026, 5, 26, 9, 0), 50000, 2500)));
        Animal negativeGain = AnimalSyncService.buildAnimalForPeriod(101,
                List.of(visit(LocalDateTime.of(2026, 5, 25, 8, 15), 54000, 2000),
                        visit(LocalDateTime.of(2026, 5, 26, 9, 0), 50000, 2500)));

        assertEquals(0.0, zeroGain.weightGainKg(), 1e-9);
        assertNull(zeroGain.fcr());
        assertEquals(-4.0, negativeGain.weightGainKg(), 1e-9);
        assertNull(negativeGain.fcr());
    }

    private static Visit visit(LocalDateTime visitTime, Integer weightG, int feedIntakeG) {
        return new Visit(0, 101, "RFID-101", 0, 7, visitTime, 120, weightG, feedIntakeG);
    }
}
