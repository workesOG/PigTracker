// Theis Thomsen

package pigtracker.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import pigtracker.dao.AnimalDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Animal;
import pigtracker.model.Animal.Status;
import pigtracker.model.Visit;

public final class AnimalSyncService {

    private AnimalSyncService() {}

    public static void syncAnimalData(List<Visit> importedVisits, int groupId) {
        Map<Integer, List<Visit>> visitsByAnimal = importedVisits.stream()
                .collect(Collectors.groupingBy(Visit::animalNumber));

        for (Map.Entry<Integer, List<Visit>> entry : visitsByAnimal.entrySet()) {
            int animalNumber = entry.getKey();
            try {
                List<Visit> allVisits = new ArrayList<>(VisitDAO.findByAnimalNumber(animalNumber));
                allVisits.addAll(entry.getValue());
                allVisits.sort(Comparator.comparing(Visit::visitTime));

                Animal base = findOrCreateAnimal(animalNumber, allVisits, groupId);
                Animal updated = applyDerivedMetrics(base, allVisits);

                if (base.id() > 0) {
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

    // Builds a snapshot Animal representing this animal at the end of visitsInPeriod. Used by reports/dashboard.
    public static Animal buildAnimalForPeriod(int animalNumber, List<Visit> visitsInPeriod) throws SQLException {
        if (visitsInPeriod == null || visitsInPeriod.isEmpty())
            return null;

        Integer groupId = AnimalDAO.getGroupIdByAnimalNumber(animalNumber).orElse(null);
        if (groupId == null) {
            throw new NullPointerException(
                    String.format("Animal with animal number: %d has no group", animalNumber));
        }

        Visit first = visitsInPeriod.get(0);
        DerivedMetrics m = DerivedMetrics.from(visitsInPeriod, null, null);
        return new Animal(0, animalNumber, first.responder(), groupId, first.location(), Status.ACTIVE,
                null, null, m.fcr(), m.startWeightKg(), m.totalFeedKg(), m.weightGainKg(),
                m.latestWeightKg(), m.completedDays(), m.startDay(), null);
    }

    private static Animal findOrCreateAnimal(int animalNumber, List<Visit> allVisits, int groupId) throws SQLException {
        Optional<Animal> existing = AnimalDAO.findByAnimalNumber(animalNumber);
        if (existing.isPresent())
            return existing.get();

        Visit first = allVisits.get(0);
        return new Animal(0, animalNumber, first.responder(), groupId, first.location(), Status.ACTIVE,
                null, null, null, earliestWeightKg(allVisits), 0.0, 0.0, null, null,
                first.visitTime().toLocalDate(), LocalDateTime.now());
    }

    // Recomputes derived fields on base from allVisits, preserving id/status/stopped/createdAt.
    private static Animal applyDerivedMetrics(Animal base, List<Visit> allVisits) {
        DerivedMetrics m = DerivedMetrics.from(allVisits, base.startWeightKg(), base.startDay());
        return new Animal(base.id(), base.animalNumber(), base.responder(), base.groupId(), base.location(),
                base.status(), base.stoppedReason(), base.stoppedAt(),
                m.fcr(), m.startWeightKg(), m.totalFeedKg(), m.weightGainKg(), m.latestWeightKg(),
                m.completedDays(), m.startDay(), base.createdAt());
    }

    private record DerivedMetrics(Double startWeightKg, Double latestWeightKg, LocalDate startDay, LocalDate latestDay,
            Integer completedDays, double totalFeedKg, Double weightGainKg, Double fcr) {

        static DerivedMetrics from(List<Visit> visits, Double startWeightOverride, LocalDate startDayOverride) {
            Double startWeight = startWeightOverride != null
                    ? startWeightOverride
                    : AnimalSyncService.earliestWeightKg(visits);
            Double latestWeight = AnimalSyncService.latestWeightKg(visits);
            LocalDate start = startDayOverride != null ? startDayOverride : AnimalSyncService.firstDay(visits);
            LocalDate latest = AnimalSyncService.lastDay(visits);
            Integer days = (start != null && latest != null)
                    ? (int) (latest.toEpochDay() - start.toEpochDay() + 1)
                    : null;
            double feed = AnimalSyncService.totalFeedKg(visits);
            Double gain = (latestWeight != null && startWeight != null) ? latestWeight - startWeight : null;
            Double fcr = (gain != null && gain > 0) ? feed / gain : null;
            return new DerivedMetrics(startWeight, latestWeight, start, latest, days, feed, gain, fcr);
        }
    }

    private static Double earliestWeightKg(List<Visit> visits) {
        return visits.stream()
                .filter(v -> v.weightG() != null && v.weightG() > 0)
                .min(Comparator.comparing(Visit::visitTime))
                .map(v -> v.weightG() / 1000.0)
                .orElse(null);
    }

    private static Double latestWeightKg(List<Visit> visits) {
        return visits.stream()
                .filter(v -> v.weightG() != null && v.weightG() > 0)
                .max(Comparator.comparing(Visit::visitTime))
                .map(v -> v.weightG() / 1000.0)
                .orElse(null);
    }

    private static LocalDate firstDay(List<Visit> visits) {
        return visits.stream().map(v -> v.visitTime().toLocalDate()).min(LocalDate::compareTo).orElse(null);
    }

    private static LocalDate lastDay(List<Visit> visits) {
        return visits.stream().map(v -> v.visitTime().toLocalDate()).max(LocalDate::compareTo).orElse(null);
    }

    private static double totalFeedKg(List<Visit> visits) {
        return visits.stream().mapToDouble(Visit::feedIntakeG).sum() / 1000.0;
    }
}
