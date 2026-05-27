// Theis Thomsen

package pigtracker.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pigtracker.dao.ReportDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Animal;
import pigtracker.model.DashboardMetrics;
import pigtracker.model.HistoricalImportComparisonMetrics;
import pigtracker.model.KpiMetrics;
import pigtracker.model.PopulationDistributionGraphMetrics;
import pigtracker.model.Report;
import pigtracker.model.Visit;
import pigtracker.util.DateFormattingUtil;
import pigtracker.util.MathUtil;
import pigtracker.util.MetricsUtil;
import pigtracker.util.TimeUtil;

public class DashboardCreationService {
    private static final int KPI_HISTORY_SIZE = 7;

    private static List<Animal> getAnimalsForReport(Report report) throws SQLException {
        if (report == null)
            return Collections.emptyList();
        List<Visit> visits = VisitDAO.findByReportId(report.id());
        Map<Integer, List<Visit>> animalVisits = visits.stream().collect(Collectors.groupingBy(Visit::animalNumber));
        return buildAnimalsForPeriod(animalVisits);
    }

    private static List<Double> buildKpiHistory(List<Report> reports,
            java.util.function.Function<List<Animal>, Double> kpiFunc) throws SQLException {
        List<Double> history = new ArrayList<>();
        int steps = Math.min(reports.size(), KPI_HISTORY_SIZE);

        for (int i = 0; i < steps; i++) {
            Report report = reports.get(i);
            List<Animal> animals = getAnimalsForReport(report);
            history.add(kpiFunc.apply(animals));
        }
        return history;
    }

    private static KpiMetrics buildKpi(String title, double latest, double previous, String unit, int decimals,
            String description, List<Double> history) {
        double change = MathUtil.percentChange(previous, latest);
        return new KpiMetrics(title, latest, decimals, unit, change, description, history);
    }

    public static DashboardMetrics calculateDashboardMetrics(Integer groupId) throws SQLException {
        if (groupId == null) {
            return null;
        }

        List<Report> completed = ReportDAO.findCompletedByGroupId(groupId);
        if (completed.isEmpty())
            return null;

        Report latest = completed.get(0), previous = completed.size() > 1 ? completed.get(1) : null;

        List<Animal> latestAnimals = getAnimalsForReport(latest), previousAnimals = getAnimalsForReport(previous);

        // Compose histories (latest first)
        List<Double> fcrHistory = buildKpiHistory(completed, MetricsUtil::fcrListMean);
        List<Double> weightHistory = buildKpiHistory(completed, MetricsUtil::dailyGainListMean);
        List<Double> feedHistory = buildKpiHistory(completed, animals -> MetricsUtil.feedPerDayListMean(animals) * 1000 // grams
        );
        List<Double> populationHistory = buildKpiHistory(completed,
                animals -> (double)MetricsUtil.animalCount(animals));

        // FCR KPI
        double currentFcr = MathUtil.mean(MetricsUtil.fcrList(latestAnimals));
        double prevFcr = MathUtil.mean(MetricsUtil.fcrList(previousAnimals));
        KpiMetrics fcrKpi = buildKpi("Feed Conversion Rate (FCR)", currentFcr, prevFcr, "kg feed / kg gain", 1, "Mean",
                fcrHistory);

        // Weight KPI
        double currentWeight = MathUtil.mean(MetricsUtil.dailyGainList(latestAnimals));
        double prevWeight = MathUtil.mean(MetricsUtil.dailyGainList(previousAnimals));
        KpiMetrics weightKpi = buildKpi("Animal Weight", currentWeight, prevWeight, "kg gain / day", 2, "Mean",
                weightHistory);

        // Feed Consumption KPI
        double currentFeed = MathUtil.mean(MetricsUtil.feedPerDayList(latestAnimals)) * 1000; // to g
        double prevFeed = MathUtil.mean(MetricsUtil.feedPerDayList(previousAnimals)) * 1000;
        KpiMetrics feedKpi = buildKpi("Feed Consumption", currentFeed, prevFeed, "g feed / day", 0, "Mean",
                feedHistory);

        // Population KPI
        double currPop = MetricsUtil.animalCount(latestAnimals);
        double prevPop = MetricsUtil.animalCount(previousAnimals);
        KpiMetrics populationKpi = buildKpi("Animal Population", currPop, prevPop, "pigs", 0, "Total",
                populationHistory);

        // Graph models (PDG/HIC) would be added here later
        return new DashboardMetrics(weightKpi, feedKpi, fcrKpi, populationKpi, null, null);
    }

    public static PopulationDistributionGraphMetrics calculatePopulationDistributionMetrics(Integer groupId,
            String metric) throws SQLException {
        if (groupId == null) {
            return null;
        }

        List<Report> completed = ReportDAO.findCompletedByGroupId(groupId);
        if (completed.isEmpty())
            return null;

        Report latest = completed.get(0);

        List<Animal> animals = getAnimalsForReport(latest);

        List<Double> values = switch (metric) {

        case "FCR" -> MetricsUtil.fcrList(animals);
        case "Feed Cns." -> MetricsUtil.feedPerDayList(animals);
        case "Weight" -> MetricsUtil.weightList(animals);
        default -> Collections.emptyList();
        };

        if (values.isEmpty()) {
            List<String> labels = Collections.nCopies(10, "");
            List<Integer> bins = Collections.nCopies(10, 0);
            return new PopulationDistributionGraphMetrics(labels, bins);
        }

        double min = Collections.min(values);
        double max = Collections.max(values);
        if (min == max)
            max = min + 1;

        double binWidth = (max - min) / 10.0;
        List<Integer> binCounts = new ArrayList<>(Collections.nCopies(10, 0));

        for (double v : values) {
            int bin = (int)((v - min) / binWidth);
            if (bin == 10)
                bin = 9;
            binCounts.set(bin, binCounts.get(bin) + 1);
        }

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            double rangeStart = min + i * binWidth;
            double rangeEnd = rangeStart + binWidth;
            labels.add(String.format("%.2f–%.2f", rangeStart, rangeEnd));
        }

        return new PopulationDistributionGraphMetrics(labels, binCounts);
    }

    public static HistoricalImportComparisonMetrics calculateHistoricalImportComparisonMetrics(Integer groupId,
            String metric, String period) throws SQLException {
        if (groupId == null) {
            return null;
        }

        List<Report> completed = ReportDAO.findCompletedByGroupId(groupId);
        if (completed.isEmpty())
            return new HistoricalImportComparisonMetrics(List.of(), List.of());

        completed.sort(Comparator.comparing(Report::createdAt));

        LocalDateTime latest = completed.get(completed.size() - 1).createdAt();
        LocalDateTime lowerBound = TimeUtil.minusPeriod(latest, period);
        List<Report> relevant = completed.stream().filter(r -> !r.createdAt().isBefore(lowerBound)).toList();

        if (relevant.isEmpty())
            return new HistoricalImportComparisonMetrics(List.of(), List.of());

        List<Double> means = new ArrayList<>();
        for (Report report : relevant) {
            List<Animal> animals = getAnimalsForReport(report);
            double mean = switch (metric) {
            case "FCR" -> MathUtil.mean(MetricsUtil.fcrList(animals));
            case "Feed Cns." -> MathUtil.mean(MetricsUtil.feedPerDayList(animals));
            case "Weight" -> MathUtil.mean(MetricsUtil.weightList(animals));
            default -> 0.0;
            };
            means.add(mean);
        }
        if (means.isEmpty())
            return new HistoricalImportComparisonMetrics(List.of(), List.of());

        double baseline = means.get(0);
        List<Double> percentChanges = means.stream().map(v -> MathUtil.percentChange(baseline, v)).toList();

        List<String> labels = relevant.stream().map(r -> {
            if (r.importStart() != null && r.importEnd() != null) {
                long midpointSeconds = java.time.Duration.between(r.importStart(), r.importEnd()).getSeconds() / 2;
                LocalDateTime avg = r.importStart().plusSeconds(midpointSeconds);
                return avg.format(DateFormattingUtil.dateFormatter);
            } else {
                return r.createdAt().format(DateFormattingUtil.dateFormatter);
            }
        }).toList();

        return new HistoricalImportComparisonMetrics(labels, percentChanges);
    }

    private static List<Animal> buildAnimalsForPeriod(Map<Integer, List<Visit>> visitsByAnimal) throws SQLException {
        List<Animal> animals = new ArrayList<>();
        for (Map.Entry<Integer, List<Visit>> entry : visitsByAnimal.entrySet()) {
            Animal a = AnimalSyncService.buildAnimalForPeriod(entry.getKey(), entry.getValue());
            if (a != null)
                animals.add(a);
        }
        return animals;
    }
}