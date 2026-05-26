// Af Nikolaj Jakobsen

package pigtracker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Animal(int id, int animalNumber, String responder, int location, Status status, String stoppedReason,
        LocalDateTime stoppedAt, Double fcr, Double startWeightKg, Double totalFeedKg, Double weightGainKg,
        Double latestWeightKg, Integer completedDays, LocalDate startDay, LocalDateTime createdAt) {

    public enum Status {
        ACTIVE, STOPPED
    }

    // Returns a copy of this animal with the given id (used after insert).
    public Animal withId(int newId) {
        return new Animal(newId, animalNumber, responder, location, status, stoppedReason, stoppedAt, fcr,
                startWeightKg, totalFeedKg, weightGainKg, latestWeightKg, completedDays, startDay, createdAt);
    }

    // Returns true if the animal is still being registered.
    public boolean isActive() {
        return status == Status.ACTIVE;
    }
}