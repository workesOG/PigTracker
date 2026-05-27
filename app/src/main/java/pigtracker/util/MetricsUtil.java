// Theis Thomsen

package pigtracker.util;

import pigtracker.model.Animal;
import pigtracker.model.MeanMedianMetric;
import pigtracker.model.TopThreePigs;
import pigtracker.model.Visit;
import pigtracker.model.DisplayType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MetricsUtil {
    private MetricsUtil() {} // Utility class

    // Calculate mean/median for common metrics
    public static List<MeanMedianMetric> calculateMeanMedianMetrics(List<Animal> animals, List<Visit> visits,
            Map<Integer, List<Visit>> visitsByAnimal) {
        List<MeanMedianMetric> meanMedians = new ArrayList<>();
        // FCR
        List<Double> fcrList = fcrList(animals);
        meanMedians.add(
                new MeanMedianMetric("FCR", MathUtil.mean(fcrList), MathUtil.median(fcrList), DisplayType.DECIMAL));
        // Weight
        List<Double> weightList = weightList(animals);
        meanMedians.add(new MeanMedianMetric("Weight", MathUtil.mean(weightList), MathUtil.median(weightList),
                DisplayType.DECIMAL));
        // Feed per visit (kg)
        List<Double> feedPerVisitList = visits.stream().map(v -> v.feedIntakeG() / 1000.0).toList();
        meanMedians.add(new MeanMedianMetric("Feed/Visit (kg)", MathUtil.mean(feedPerVisitList),
                MathUtil.median(feedPerVisitList), DisplayType.DECIMAL));
        // Feed per day per pig
        List<Double> feedPerDayList = feedPerDayList(animals);
        meanMedians.add(new MeanMedianMetric("Feed/Day (kg)", MathUtil.mean(feedPerDayList),
                MathUtil.median(feedPerDayList), DisplayType.DECIMAL));
        // Visits per pig per day
        List<Double> visitsPerPigPerDayList = visitsPerPigPerDayList(visitsByAnimal);
        meanMedians.add(new MeanMedianMetric("Visits/Pig/Day", MathUtil.mean(visitsPerPigPerDayList),
                MathUtil.median(visitsPerPigPerDayList), DisplayType.DECIMAL));
        // Visit Duration (seconds)
        List<Double> visitDurationList = visits.stream().map(v -> (double)v.durationSec()).toList();
        meanMedians.add(new MeanMedianMetric("Visit Duration (s)", MathUtil.mean(visitDurationList),
                MathUtil.median(visitDurationList), DisplayType.TIME));

        return meanMedians;
    }

    // Calculate "top three" for each metric
    public static List<TopThreePigs> calculateTopThreePigs(List<Animal> animals, List<Visit> visits,
            Map<Integer, List<Visit>> visitsByAnimal) {
        List<TopThreePigs> topThree = new ArrayList<>();
        // 1. FCR (best/lowest)
        List<Animal> topFCR = animals.stream().filter(a -> a.fcr() != null && a.fcr() > 0)
                .sorted(Comparator.comparingDouble(Animal::fcr)).limit(3).toList();
        topThree.add(new TopThreePigs("FCR", topFCR.stream().mapToInt(Animal::animalNumber).toArray(),
                topFCR.stream().mapToDouble(Animal::fcr).toArray(), DisplayType.DECIMAL));
        // 2. Weight (heaviest)
        List<Animal> topWeight = animals.stream().filter(a -> a.latestWeightKg() != null)
                .sorted(Comparator.comparingDouble(Animal::latestWeightKg).reversed()).limit(3).toList();
        topThree.add(new TopThreePigs("Weight", topWeight.stream().mapToInt(Animal::animalNumber).toArray(),
                topWeight.stream().mapToDouble(Animal::latestWeightKg).toArray(), DisplayType.DECIMAL));
        // 3. Feed intake (most)
        List<Animal> topFeed = animals.stream().filter(a -> a.totalFeedKg() != null)
                .sorted(Comparator.comparingDouble(Animal::totalFeedKg).reversed()).limit(3).toList();
        topThree.add(new TopThreePigs("Feed Intake", topFeed.stream().mapToInt(Animal::animalNumber).toArray(),
                topFeed.stream().mapToDouble(Animal::totalFeedKg).toArray(), DisplayType.DECIMAL));
        // 4. Longest single visit
        List<Visit> topLongestVisits = visits.stream().sorted(Comparator.comparingInt(Visit::durationSec).reversed())
                .limit(3).toList();
        topThree.add(new TopThreePigs("Longest Visit",
                topLongestVisits.stream().mapToInt(Visit::animalNumber).toArray(),
                topLongestVisits.stream().mapToDouble(v -> (double)v.durationSec()).toArray(), DisplayType.TIME));
        // 5. Most visits
        List<Map.Entry<Integer, List<Visit>>> topMostVisits = visitsByAnimal.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> -e.getValue().size())).limit(3).toList();
        topThree.add(new TopThreePigs("Most Visits", topMostVisits.stream().mapToInt(Map.Entry::getKey).toArray(),
                topMostVisits.stream().mapToDouble(e -> (double)e.getValue().size()).toArray(), DisplayType.INT));
        return topThree;
    }

    // Simple activity by hour breakdown
    public static List<Integer> calculateActivityByHour(List<Visit> visits) {
        List<Integer> activityByHour = new ArrayList<>(Collections.nCopies(24, 0));
        for (Visit visit : visits) {
            int hour = visit.visitTime().getHour(); // get hour (0-23)
            activityByHour.set(hour, activityByHour.get(hour) + 1);
        }
        return activityByHour;
    }

    public static List<Double> fcrList(List<Animal> animals) {
        return animals.stream().map(Animal::fcr).filter(Objects::nonNull).filter(fcr -> fcr > 0)
                .collect(Collectors.toList());
    }

    public static List<Double> weightList(List<Animal> animals) {
        return animals.stream().map(Animal::latestWeightKg).filter(Objects::nonNull).toList();
    }

    public static List<Double> dailyGainList(List<Animal> animals) {
        // Mean daily gain: weightGainKg/completedDays or avg per-animal daily gain
        // metric
        return animals.stream()
                .filter(a -> a.weightGainKg() != null && a.completedDays() != null && a.completedDays() > 0)
                .map(a -> a.weightGainKg() / a.completedDays()).collect(Collectors.toList());
    }

    public static List<Double> feedPerDayList(List<Animal> animals) {
        // Feed per day per animal
        return animals.stream()
                .filter(a -> a.totalFeedKg() != null && a.completedDays() != null && a.completedDays() > 0)
                .map(a -> a.totalFeedKg() / a.completedDays()).collect(Collectors.toList());
    }

    public static int animalCount(List<Animal> animals) {
        return animals.size();
    }

    public static List<Double> visitsPerPigPerDayList(Map<Integer, List<Visit>> visitsByAnimal) {
        return visitsByAnimal.values().stream().filter(list -> !list.isEmpty()).map(list -> {
            int visitCount = list.size();
            LocalDateTime firstVisit = list.stream().map(Visit::visitTime).min(LocalDateTime::compareTo).get();
            LocalDateTime lastVisit = list.stream().map(Visit::visitTime).max(LocalDateTime::compareTo).get();
            long daysActive = java.time.Duration
                    .between(firstVisit.toLocalDate().atStartOfDay(), lastVisit.toLocalDate().atStartOfDay()).toDays()
                    + 1;
            if (daysActive <= 0)
                daysActive = 1;
            return visitCount / (double)daysActive;
        }).toList();
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
}