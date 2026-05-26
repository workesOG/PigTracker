// Theis Thomsen
package pigtracker.service;

import pigtracker.dao.ReportDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Animal;
import pigtracker.model.DisplayType;
import pigtracker.model.MeanMedianMetric;
import pigtracker.model.Report;
import pigtracker.model.ReportMetrics;
import pigtracker.model.TopThreePigs;
import pigtracker.model.Visit;
import pigtracker.util.MathUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportImportService {
    public static ReportMetrics generateReportData(Report report) throws SQLException {
        List<Visit> visits = VisitDAO.findByReportId(report.id());

        Map<Integer, List<Visit>> visitsByAnimal = visits.stream().collect(Collectors.groupingBy(Visit::animalNumber));

        List<Animal> animals = new ArrayList<>();
        for (Map.Entry<Integer, List<Visit>> entry : visitsByAnimal.entrySet()) {
            Animal snapshot = AnimalSyncService.buildAnimalForPeriod(entry.getKey(), entry.getValue());
            if (snapshot != null) {
                animals.add(snapshot);
            }
        }

        List<MeanMedianMetric> meanMedians = calculateMeanMedianMetrics(animals, visits, visitsByAnimal);
        List<TopThreePigs> topThree = calculateTopThreePigs(animals, visits, visitsByAnimal);
        return new ReportMetrics(meanMedians, topThree);
    }

    public static int createReport(LocalDateTime importStart, LocalDateTime importEnd, int rowCount, int pigCount,
            int createdBy) throws SQLException {
        Report report = new Report(0, importStart, importEnd, rowCount, pigCount, null, createdBy, null);
        Report created = ReportDAO.create(report);
        return created.id();
    }

    private static List<MeanMedianMetric> calculateMeanMedianMetrics(List<Animal> animals, List<Visit> visits,
            Map<Integer, List<Visit>> visitsByAnimal) {
        List<MeanMedianMetric> meanMedians = new ArrayList<>();

        // 1. FCR
        List<Double> fcrList = animals.stream().map(Animal::fcr).filter(java.util.Objects::nonNull).toList();
        meanMedians.add(
                new MeanMedianMetric("FCR", MathUtil.mean(fcrList), MathUtil.median(fcrList), DisplayType.DECIMAL));

        // 2. Weight
        List<Double> weightList = animals.stream().map(Animal::latestWeightKg).filter(java.util.Objects::nonNull)
                .toList();
        meanMedians.add(new MeanMedianMetric("Weight", MathUtil.mean(weightList), MathUtil.median(weightList),
                DisplayType.DECIMAL));

        // 3. Feed per Visit
        List<Double> feedPerVisitList = visits.stream().map(v -> v.feedIntakeG() / 1000.0).toList();
        meanMedians.add(new MeanMedianMetric("Feed/Visit (kg)", MathUtil.mean(feedPerVisitList),
                MathUtil.median(feedPerVisitList), DisplayType.DECIMAL));

        // 4. Feed per Day per Pig
        List<Double> feedPerDayList = animals.stream()
                .filter(a -> a.totalFeedKg() != null && a.completedDays() != null && a.completedDays() > 0)
                .map(a -> a.totalFeedKg() / a.completedDays()).toList();
        meanMedians.add(new MeanMedianMetric("Feed/Day (kg)", MathUtil.mean(feedPerDayList),
                MathUtil.median(feedPerDayList), DisplayType.DECIMAL));

        // 5. Visits per Pig
        List<Double> visitsPerPigList = visitsByAnimal.values().stream().map(list -> (double)list.size()).toList();
        meanMedians.add(new MeanMedianMetric("Visits/Pig", MathUtil.mean(visitsPerPigList),
                MathUtil.median(visitsPerPigList), DisplayType.DECIMAL));

        // 6. Visit Duration (seconds)
        List<Double> visitDurationList = visits.stream().map(v -> (double)v.durationSec()).toList();
        meanMedians.add(new MeanMedianMetric("Visit Duration (s)", MathUtil.mean(visitDurationList),
                MathUtil.median(visitDurationList), DisplayType.TIME));

        return meanMedians;
    }

    private static List<TopThreePigs> calculateTopThreePigs(List<Animal> animals, List<Visit> visits,
            Map<Integer, List<Visit>> visitsByAnimal) {
        List<TopThreePigs> topThree = new ArrayList<>();

        // 1. FCR (best/lowest)
        List<Animal> topFCR = animals.stream().filter(a -> a.fcr() != null && a.fcr() > 0)
                .sorted(java.util.Comparator.comparingDouble(Animal::fcr)).limit(3).toList();
        topThree.add(new TopThreePigs("FCR", topFCR.stream().mapToInt(Animal::animalNumber).toArray(),
                topFCR.stream().mapToDouble(Animal::fcr).toArray(), DisplayType.DECIMAL));

        // 2. Weight (heaviest)
        List<Animal> topWeight = animals.stream().filter(a -> a.latestWeightKg() != null)
                .sorted(java.util.Comparator.comparingDouble(Animal::latestWeightKg).reversed()).limit(3).toList();
        topThree.add(new TopThreePigs("Weight", topWeight.stream().mapToInt(Animal::animalNumber).toArray(),
                topWeight.stream().mapToDouble(Animal::latestWeightKg).toArray(), DisplayType.DECIMAL));

        // 3. Feed intake (most)
        List<Animal> topFeed = animals.stream().filter(a -> a.totalFeedKg() != null)
                .sorted(java.util.Comparator.comparingDouble(Animal::totalFeedKg).reversed()).limit(3).toList();
        topThree.add(new TopThreePigs("Feed Intake", topFeed.stream().mapToInt(Animal::animalNumber).toArray(),
                topFeed.stream().mapToDouble(Animal::totalFeedKg).toArray(), DisplayType.DECIMAL));

        // 4. Longest single visit
        List<Visit> topLongestVisits = visits.stream()
                .sorted(java.util.Comparator.comparingInt(Visit::durationSec).reversed()).limit(3).toList();
        topThree.add(new TopThreePigs("Longest Visit",
                topLongestVisits.stream().mapToInt(Visit::animalNumber).toArray(),
                topLongestVisits.stream().mapToDouble(v -> (double)v.durationSec()).toArray(), DisplayType.TIME));

        // 5. Most visits
        List<java.util.Map.Entry<Integer, List<Visit>>> topMostVisits = visitsByAnimal.entrySet().stream()
                .sorted(java.util.Comparator.comparingInt(e -> -e.getValue().size())).limit(3).toList();
        topThree.add(new TopThreePigs("Most Visits",
                topMostVisits.stream().mapToInt(java.util.Map.Entry::getKey).toArray(),
                topMostVisits.stream().mapToDouble(e -> (double)e.getValue().size()).toArray(), DisplayType.INT));

        return topThree;
    }
}
