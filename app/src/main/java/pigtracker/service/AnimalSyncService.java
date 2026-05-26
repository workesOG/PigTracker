// Theis Thomsen
package pigtracker.service;

import pigtracker.dao.AnimalDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Animal;
import pigtracker.model.Animal.Status;
import pigtracker.model.Visit;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class AnimalSyncService {
    public static void syncAnimalData(List<Visit> importedVisits) {
        Map<Integer, List<Visit>> visitsByAnimal = importedVisits.stream()
                .collect(Collectors.groupingBy(Visit::animalNumber));

        for (Map.Entry<Integer, List<Visit>> entry : visitsByAnimal.entrySet()) {
            int animalNumber = entry.getKey();
            try {
                // 1. Get all visits for this animalNumber (now all present in DB after import)
                List<Visit> allVisits = VisitDAO.findByAnimalNumber(animalNumber);
                allVisits.sort(Comparator.comparing(Visit::visitTime));

                // 2. Get or create Animal
                Animal animal = findOrCreateAnimal(animalNumber, allVisits);

                // 3. Calculate updated properties from allVisits, keeping creation data if
                // present
                Animal updated = buildUpdatedAnimal(animal, allVisits);

                if (animal.id() > 0) {
                    AnimalDAO.update(updated);
                } else {
                    AnimalDAO.create(updated);
                }
            } catch (Exception e) {
                System.err.println(
                        "Error syncing animal_number " + animalNumber + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Find animal in DB, or create a fresh Animal with blank ID
    private static Animal findOrCreateAnimal(int animalNumber, List<Visit> allVisits) throws SQLException {
        Optional<Animal> existing = AnimalDAO.findByAnimalNumber(animalNumber);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            Visit first = allVisits.get(0);
            Double startWeightKg = getEarliestWeightKg(allVisits);
            LocalDate startDay = first.visitTime().toLocalDate();
            return new Animal(0, animalNumber, first.responder(), first.location(), Status.ACTIVE, null, null, null,
                    startWeightKg, 0.0, 0.0, null, null, startDay, LocalDateTime.now());
        }
    }

    // Calculates derived animal fields from visits
    private static Animal buildUpdatedAnimal(Animal base, List<Visit> allVisits) {
        /*
         * I am not sure I need this part anymore? List<Visit> visitsWithWeight =
         * allVisits.stream().filter(v -> v.weightG() != null && v.weightG() > 0)
         * .sorted(Comparator.comparing(Visit::visitTime)).toList();
         */

        // startWeightKg only ever set if not yet set, otherwise keep base
        Double startWeightKg = base.startWeightKg() != null ? base.startWeightKg() : getEarliestWeightKg(allVisits);
        Double latestWeightKg = getLatestWeightKg(allVisits);

        // startDay: never changes, keep base or derive from earliest visit
        LocalDate startDay = base.startDay() != null ? base.startDay()
                : allVisits.stream().map(v -> v.visitTime().toLocalDate()).min(LocalDate::compareTo).orElse(null);

        // completedDays: difference in days (inclusive) between startDay and latest
        // visit
        LocalDate latestDay = allVisits.stream().map(v -> v.visitTime().toLocalDate()).max(LocalDate::compareTo)
                .orElse(startDay);
        Integer completedDays = (startDay != null && latestDay != null)
                ? (int)(latestDay.toEpochDay() - startDay.toEpochDay() + 1)
                : null;

        // totalFeedKg: sum of feedIntakeG from all visits
        double totalFeedKg = allVisits.stream().mapToDouble(Visit::feedIntakeG).sum() / 1000.0;

        // weightGainKg: difference between latestWeightKg and startWeightKg, if both
        // present
        Double weightGainKg = (latestWeightKg != null && startWeightKg != null) ? latestWeightKg - startWeightKg : null;

        // FCR: feed conversion ratio
        Double fcr = (weightGainKg != null && weightGainKg > 0) ? totalFeedKg / weightGainKg : null;

        // Re-use other fields from base (status, stopped, etc)
        return new Animal(base.id(), base.animalNumber(), base.responder(), base.location(), base.status(),
                base.stoppedReason(), base.stoppedAt(), fcr, startWeightKg, totalFeedKg, weightGainKg, latestWeightKg,
                completedDays, startDay, base.createdAt());
    }

    // Utility: earliest weight (in KG)
    private static Double getEarliestWeightKg(List<Visit> allVisits) {
        return allVisits.stream().filter(v -> v.weightG() != null && v.weightG() > 0)
                .min(Comparator.comparing(Visit::visitTime)).map(v -> v.weightG() / 1000.0).orElse(null);
    }

    // Utility: latest weight (in KG)
    private static Double getLatestWeightKg(List<Visit> allVisits) {
        return allVisits.stream().filter(v -> v.weightG() != null && v.weightG() > 0)
                .max(Comparator.comparing(Visit::visitTime)).map(v -> v.weightG() / 1000.0).orElse(null);
    }
}
