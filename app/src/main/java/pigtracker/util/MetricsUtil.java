// Theis Thomsen

package pigtracker.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import pigtracker.model.Animal;
import pigtracker.model.DisplayType;
import pigtracker.model.MeanMedianMetric;
import pigtracker.model.TopThreePigs;
import pigtracker.model.Visit;

public final class MetricsUtil {

    private MetricsUtil() {}

    public static List<MeanMedianMetric> calculateMeanMedianMetrics(List<Animal> animals, List<Visit> visits,
            Map<Integer, List<Visit>> visitsByAnimal) {
        List<Double> feedPerVisitKg = visits.stream().map(v -> v.feedIntakeG() / 1000.0).toList();
        List<Double> visitDurations = visits.stream().map(v -> (double) v.durationSec()).toList();

        return List.of(
                meanMedianOf("FCR", fcrList(animals), DisplayType.DECIMAL),
                meanMedianOf("Weight", weightList(animals), DisplayType.DECIMAL),
                meanMedianOf("Feed/Visit (kg)", feedPerVisitKg, DisplayType.DECIMAL),
                meanMedianOf("Feed/Day (kg)", feedPerDayList(animals), DisplayType.DECIMAL),
                meanMedianOf("Visits/Pig/Day", visitsPerPigPerDayList(visitsByAnimal), DisplayType.DECIMAL),
                meanMedianOf("Visit Duration (s)", visitDurations, DisplayType.TIME));
    }

    public static List<TopThreePigs> calculateTopThreePigs(List<Animal> animals, List<Visit> visits,
            Map<Integer, List<Visit>> visitsByAnimal) {
        List<Animal> withFcr = animals.stream().filter(a -> a.fcr() != null && a.fcr() > 0).toList();
        List<Animal> withWeight = animals.stream().filter(a -> a.latestWeightKg() != null).toList();
        List<Animal> withFeed = animals.stream().filter(a -> a.totalFeedKg() != null).toList();

        TopThreePigs mostVisits = mostVisitsTop3(visitsByAnimal);

        return List.of(
                topThreeOf("FCR", withFcr, Comparator.comparingDouble(Animal::fcr),
                        Animal::animalNumber, Animal::fcr, DisplayType.DECIMAL),
                topThreeOf("Weight", withWeight, Comparator.comparingDouble(Animal::latestWeightKg).reversed(),
                        Animal::animalNumber, Animal::latestWeightKg, DisplayType.DECIMAL),
                topThreeOf("Feed Intake", withFeed, Comparator.comparingDouble(Animal::totalFeedKg).reversed(),
                        Animal::animalNumber, Animal::totalFeedKg, DisplayType.DECIMAL),
                topThreeOf("Longest Visit", visits, Comparator.comparingInt(Visit::durationSec).reversed(),
                        Visit::animalNumber, v -> (double) v.durationSec(), DisplayType.TIME),
                mostVisits);
    }

    public static List<Integer> calculateActivityByHour(List<Visit> visits) {
        List<Integer> activityByHour = new ArrayList<>(Collections.nCopies(24, 0));
        for (Visit visit : visits) {
            int hour = visit.visitTime().getHour();
            activityByHour.set(hour, activityByHour.get(hour) + 1);
        }
        return activityByHour;
    }

    public static List<Double> fcrList(List<Animal> animals) {
        return animals.stream().map(Animal::fcr).filter(Objects::nonNull).filter(fcr -> fcr > 0).toList();
    }

    public static List<Double> weightList(List<Animal> animals) {
        return animals.stream().map(Animal::latestWeightKg).filter(Objects::nonNull).toList();
    }

    public static List<Double> dailyGainList(List<Animal> animals) {
        return animals.stream()
                .filter(a -> a.weightGainKg() != null && a.completedDays() != null && a.completedDays() > 0)
                .map(a -> a.weightGainKg() / a.completedDays())
                .toList();
    }

    public static List<Double> feedPerDayList(List<Animal> animals) {
        return animals.stream()
                .filter(a -> a.totalFeedKg() != null && a.completedDays() != null && a.completedDays() > 0)
                .map(a -> a.totalFeedKg() / a.completedDays())
                .toList();
    }

    public static int animalCount(List<Animal> animals) {
        return animals.size();
    }

    public static List<Double> visitsPerPigPerDayList(Map<Integer, List<Visit>> visitsByAnimal) {
        return visitsByAnimal.values().stream()
                .filter(list -> !list.isEmpty())
                .map(MetricsUtil::visitsPerDay)
                .toList();
    }

    public static Double fcrListMean(List<Animal> animals) {
        return MathUtil.mean(fcrList(animals));
    }

    public static Double dailyGainListMean(List<Animal> animals) {
        return MathUtil.mean(dailyGainList(animals));
    }

    public static Double feedPerDayListMean(List<Animal> animals) {
        return MathUtil.mean(feedPerDayList(animals));
    }

    private static MeanMedianMetric meanMedianOf(String name, List<Double> values, DisplayType displayType) {
        return new MeanMedianMetric(name, MathUtil.mean(values), MathUtil.median(values), displayType);
    }

    private static <T> TopThreePigs topThreeOf(String name, List<T> rows, Comparator<T> order,
            ToIntFunction<T> animalNumberOf, ToDoubleFunction<T> valueOf, DisplayType displayType) {
        List<T> top = rows.stream().sorted(order).limit(3).toList();
        return new TopThreePigs(name,
                top.stream().mapToInt(animalNumberOf).toArray(),
                top.stream().mapToDouble(valueOf).toArray(),
                displayType);
    }

    private static TopThreePigs mostVisitsTop3(Map<Integer, List<Visit>> visitsByAnimal) {
        List<Map.Entry<Integer, List<Visit>>> top = visitsByAnimal.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<Integer, List<Visit>> e) -> e.getValue().size()).reversed())
                .limit(3)
                .toList();
        return new TopThreePigs("Most Visits",
                top.stream().mapToInt(Map.Entry::getKey).toArray(),
                top.stream().mapToDouble(e -> e.getValue().size()).toArray(),
                DisplayType.INT);
    }

    private static double visitsPerDay(List<Visit> visits) {
        LocalDateTime first = visits.stream().map(Visit::visitTime).min(LocalDateTime::compareTo).orElseThrow();
        LocalDateTime last = visits.stream().map(Visit::visitTime).max(LocalDateTime::compareTo).orElseThrow();
        long daysActive = Duration.between(first.toLocalDate().atStartOfDay(), last.toLocalDate().atStartOfDay())
                .toDays() + 1;
        return visits.size() / (double) daysActive;
    }
}
