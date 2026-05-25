// Af Nikolaj Jakobsen

package pigtracker.model;

import java.time.LocalDateTime;

public record Visit(int id, int animalNumber, String responder, int reportId, int location, LocalDateTime visitTime,
        int durationSec, Integer weightG, int feedIntakeG) {

    // Returns a copy of this visit with the given id (used after insert).
    public Visit withId(int newId) {
        return new Visit(newId, animalNumber, responder, reportId, location, visitTime, durationSec, weightG,
                feedIntakeG);
    }
}