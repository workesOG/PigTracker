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
            List<Visit> importedForAnimal = entry.getValue();
            try {
                // 1. Get all existing visits for this animalNumber from DB
                List<Visit> dbVisits = VisitDAO.findByAnimalNumber(animalNumber);
                // 2. Combine with current import
                List<Visit> allVisits = new ArrayList<>(dbVisits);
                allVisits.addAll(importedForAnimal);
                allVisits.sort(Comparator.comparing(Visit::visitTime));

                // 3. Get or create Animal
                Animal animal = findOrCreateAnimal(animalNumber, allVisits);

                // 4. Calculate updated properties from allVisits, keeping creation data if
                // present
                Animal updated = buildUpdatedAnimal(animal, allVisits);

                if (animal.id() > 0) {
                    AnimalDAO.update(updated);
                } else {
                    AnimalDAO.create(updated);
                }
            } catch (Exception e) {
                System.err.println("Error syncing animal_number " + animalNumber + ": " + e.getMessage());
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
        Double startWeightKg = base.startWeightKg() != null ? base.startWeightKg() : getEarliestWeightKg(allVisits);
        Double latestWeightKg = getLatestWeightKg(allVisits);
        LocalDate startDay = base.startDay() != null ? base.startDay() : getStartDay(allVisits);
        LocalDate latestDay = getLatestDay(allVisits);
        Integer completedDays = getCompletedDays(startDay, latestDay);
        double totalFeedKg = getTotalFeedKg(allVisits);
        Double weightGainKg = getWeightGainKg(startWeightKg, latestWeightKg);
        Double fcr = getFCR(totalFeedKg, weightGainKg);

        // Re-use other fields from base (status, stopped, etc)
        return new Animal(base.id(), base.animalNumber(), base.responder(), base.location(), base.status(),
                base.stoppedReason(), base.stoppedAt(), fcr, startWeightKg, totalFeedKg, weightGainKg, latestWeightKg,
                completedDays, startDay, base.createdAt());
    }

    // For reports and dashboard: create animal representing an animal's state at
    // the end of a
    // specific period (using only visitsInPeriod)
    public static Animal buildAnimalForPeriod(int animalNumber, List<Visit> visitsInPeriod) {
        if (visitsInPeriod == null || visitsInPeriod.isEmpty())
            return null;
        String responder = visitsInPeriod.get(0).responder();
        int location = visitsInPeriod.get(0).location();
        // Assume ACTIVE (report doesn't care about true/active/stopped)
        Animal.Status status = Animal.Status.ACTIVE;
        LocalDate startDay = getStartDay(visitsInPeriod);
        LocalDate latestDay = getLatestDay(visitsInPeriod);
        Integer completedDays = getCompletedDays(startDay, latestDay);
        Double startWeightKg = getEarliestWeightKg(visitsInPeriod);
        Double latestWeightKg = getLatestWeightKg(visitsInPeriod);
        double totalFeedKg = getTotalFeedKg(visitsInPeriod);
        Double weightGainKg = getWeightGainKg(startWeightKg, latestWeightKg);
        Double fcr = getFCR(totalFeedKg, weightGainKg);
        return new Animal(0, animalNumber, responder, location, status, null, null, fcr, startWeightKg, totalFeedKg,
                weightGainKg, latestWeightKg, completedDays, startDay, null);
    }

    // Utility: earliest weight (in KG)
    private static Double getEarliestWeightKg(List<Visit> visits) {
        return visits.stream().filter(v -> v.weightG() != null && v.weightG() > 0)
                .min(Comparator.comparing(Visit::visitTime)).map(v -> v.weightG() / 1000.0).orElse(null);
    }

    // Utility: latest weight (in KG)
    private static Double getLatestWeightKg(List<Visit> visits) {
        return visits.stream().filter(v -> v.weightG() != null && v.weightG() > 0)
                .max(Comparator.comparing(Visit::visitTime)).map(v -> v.weightG() / 1000.0).orElse(null);
    }

    // Utility: first visit date
    private static LocalDate getStartDay(List<Visit> visits) {
        return visits.stream().map(v -> v.visitTime().toLocalDate()).min(LocalDate::compareTo).orElse(null);
    }

    // Utility: latest visit date
    private static LocalDate getLatestDay(List<Visit> visits) {
        return visits.stream().map(v -> v.visitTime().toLocalDate()).max(LocalDate::compareTo).orElse(null);
    }

    // Utility: completed days
    private static Integer getCompletedDays(LocalDate startDay, LocalDate latestDay) {
        return (startDay != null && latestDay != null) ? (int)(latestDay.toEpochDay() - startDay.toEpochDay() + 1)
                : null;
    }

    // Utility: total feed kg
    private static double getTotalFeedKg(List<Visit> visits) {
        return visits.stream().mapToDouble(Visit::feedIntakeG).sum() / 1000.0;
    }

    // Utility: weight gain kg
    private static Double getWeightGainKg(Double startWeightKg, Double latestWeightKg) {
        return (latestWeightKg != null && startWeightKg != null) ? latestWeightKg - startWeightKg : null;
    }

    // Utility: FCR
    private static Double getFCR(double totalFeedKg, Double weightGainKg) {
        return (weightGainKg != null && weightGainKg > 0) ? totalFeedKg / weightGainKg : null;
    }
}
