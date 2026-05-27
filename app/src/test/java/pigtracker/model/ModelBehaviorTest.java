package pigtracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelBehaviorTest {
    @Test
    void animalWithIdChangesOnlyId() {
        Animal animal = animal(12, Animal.Status.ACTIVE);

        Animal withId = animal.withId(99);

        assertEquals(99, withId.id());
        assertEquals(animal.animalNumber(), withId.animalNumber());
        assertEquals(animal.responder(), withId.responder());
        assertEquals(animal.location(), withId.location());
        assertEquals(animal.status(), withId.status());
        assertEquals(animal.stoppedReason(), withId.stoppedReason());
        assertEquals(animal.stoppedAt(), withId.stoppedAt());
        assertEquals(animal.fcr(), withId.fcr());
        assertEquals(animal.startWeightKg(), withId.startWeightKg());
        assertEquals(animal.totalFeedKg(), withId.totalFeedKg());
        assertEquals(animal.weightGainKg(), withId.weightGainKg());
        assertEquals(animal.latestWeightKg(), withId.latestWeightKg());
        assertEquals(animal.completedDays(), withId.completedDays());
        assertEquals(animal.startDay(), withId.startDay());
        assertEquals(animal.createdAt(), withId.createdAt());
    }

    @Test
    void animalIsActiveReflectsStatus() {
        assertTrue(animal(1, Animal.Status.ACTIVE).isActive());
        assertFalse(animal(2, Animal.Status.STOPPED).isActive());
    }

    @Test
    void visitWithIdChangesOnlyId() {
        Visit visit = visit(14, 3);

        Visit withId = visit.withId(88);

        assertEquals(88, withId.id());
        assertEquals(visit.animalNumber(), withId.animalNumber());
        assertEquals(visit.responder(), withId.responder());
        assertEquals(visit.reportId(), withId.reportId());
        assertEquals(visit.location(), withId.location());
        assertEquals(visit.visitTime(), withId.visitTime());
        assertEquals(visit.durationSec(), withId.durationSec());
        assertEquals(visit.weightG(), withId.weightG());
        assertEquals(visit.feedIntakeG(), withId.feedIntakeG());
    }

    @Test
    void visitWithReportIdChangesOnlyReportId() {
        Visit visit = visit(14, 3);

        Visit withReportId = visit.withReportId(77);

        assertEquals(visit.id(), withReportId.id());
        assertEquals(visit.animalNumber(), withReportId.animalNumber());
        assertEquals(visit.responder(), withReportId.responder());
        assertEquals(77, withReportId.reportId());
        assertEquals(visit.location(), withReportId.location());
        assertEquals(visit.visitTime(), withReportId.visitTime());
        assertEquals(visit.durationSec(), withReportId.durationSec());
        assertEquals(visit.weightG(), withReportId.weightG());
        assertEquals(visit.feedIntakeG(), withReportId.feedIntakeG());
    }

    @Test
    void userIsAdminReflectsPermission() {
        assertTrue(new User(1, "admin", "pw", User.Permission.ADMIN).isAdmin());
        assertFalse(new User(2, "user", "pw", User.Permission.DEFAULT).isAdmin());
    }

    @Test
    void reportErrorReportHasExpectedDisplayText() {
        Report report = Report.getReportListErrorReport();

        assertEquals(-1, report.id());
        assertEquals("Error fetching reports", report.toString());
    }

    @Test
    void reportToStringFormatsCreatedAt() {
        Report report = new Report(42, 2, LocalDateTime.of(2026, 5, 1, 8, 0), LocalDateTime.of(2026, 5, 2, 8, 0), 100,
                12, Report.Status.COMPLETE, 7, LocalDateTime.of(2026, 5, 26, 9, 7, 45));

        assertEquals("Report #42 - 2026-05-26 09:07", report.toString());
    }

    @Test
    void meanMedianMetricFormatsDecimalIntAndTimeValues() {
        MeanMedianMetric decimal = new MeanMedianMetric("fcr", 1.234, 5.678, DisplayType.DECIMAL);
        MeanMedianMetric integer = new MeanMedianMetric("feed", 1.49, 1.5, DisplayType.INT);
        MeanMedianMetric time = new MeanMedianMetric("duration", 61.4, 61.5, DisplayType.TIME);

        assertEquals(String.format("%.2f", 1.234), decimal.getMeanDisplayString());
        assertEquals(String.format("%.2f", 5.678), decimal.getMedianDisplayString());
        assertEquals("1", integer.getMeanDisplayString());
        assertEquals("2", integer.getMedianDisplayString());
        assertEquals("01:01", time.getMeanDisplayString());
        assertEquals("01:02", time.getMedianDisplayString());
    }

    @Test
    void topThreePigsFormatsDecimalIntAndTimeValues() {
        int[] pigNumbers = { 101, 102, 103 };
        TopThreePigs decimal = new TopThreePigs("fcr", pigNumbers, new double[] { 1.234, 5.678 }, DisplayType.DECIMAL);
        TopThreePigs integer = new TopThreePigs("feed", pigNumbers, new double[] { 1.49, 1.5 }, DisplayType.INT);
        TopThreePigs time = new TopThreePigs("duration", pigNumbers, new double[] { 61.4, 61.5 }, DisplayType.TIME);

        assertArrayEquals(new String[] { String.format("%.2f", 1.234), String.format("%.2f", 5.678) },
                decimal.getDisplayStrings());
        assertArrayEquals(new String[] { "1", "2" }, integer.getDisplayStrings());
        assertArrayEquals(new String[] { "01:01", "01:02" }, time.getDisplayStrings());
    }

    private static Animal animal(int id, Animal.Status status) {
        return new Animal(id, 101, "RFID-101", 2, 7, status, "done", LocalDateTime.of(2026, 5, 26, 10, 0), 2.5, 50.0,
                12.0, 4.8, 54.8, 2, LocalDate.of(2026, 5, 25), LocalDateTime.of(2026, 5, 24, 9, 0));
    }

    private static Visit visit(int id, int reportId) {
        return new Visit(id, 101, "RFID-101", reportId, 7, LocalDateTime.of(2026, 5, 26, 9, 7), 123, 51000, 1500);
    }
}
