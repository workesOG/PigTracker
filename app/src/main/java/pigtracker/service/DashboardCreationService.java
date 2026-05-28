// Theis Thomsen

package pigtracker.service;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

public final class DashboardCreationService {

    private static final int KPI_HISTORY_SIZE = 7;
    private static final int DISTRIBUTION_BIN_COUNT = 10;

    private DashboardCreationService() {}

    public static DashboardMetrics calculateDashboardMetrics(Integer groupId) throws SQLException {
        if (groupId == null)
            return null;

        List<Report> completed = ReportDAO.findCompletedByGroupId(groupId);
        if (completed.isEmpty())
            return null;

        List<Animal> latestAnimals = getAnimalsForReport(completed.get(0));
        List<Animal> previousAnimals = completed.size() > 1
                ? getAnimalsForReport(completed.get(1))
                : Collections.emptyList();

        KpiMetrics fcrKpi = buildKpi("Feed Conversion Rate (FCR)", "kg feed / kg gain", 1, "Mean",
                latestAnimals, previousAnimals, completed, MetricsUtil::fcrListMean);
        KpiMetrics weightKpi = buildKpi("Animal Weight", "kg gain / day", 2, "Mean",
                latestAnimals, previousAnimals, completed, MetricsUtil::dailyGainListMean);
        KpiMetrics feedKpi = buildKpi("Feed Consumption", "g feed / day", 0, "Mean",
                latestAnimals, previousAnimals, completed, a -> MetricsUtil.feedPerDayListMean(a) * 1000);
        KpiMetrics populationKpi = buildKpi("Animal Population", "pigs", 0, "Total",
                latestAnimals, previousAnimals, completed, a -> (double) MetricsUtil.animalCount(a));

        return new DashboardMetrics(weightKpi, feedKpi, fcrKpi, populationKpi, null, null);
    }

    public static PopulationDistributionGraphMetrics calculatePopulationDistributionMetrics(Integer groupId,
            String metric) throws SQLException {
        if (groupId == null)
            return null;

        List<Report> completed = ReportDAO.findCompletedByGroupId(groupId);
        if (completed.isEmpty())
            return null;

        List<Animal> animals = getAnimalsForReport(completed.get(0));
        List<Double> values = switch (metric) {
            case "FCR" -> MetricsUtil.fcrList(animals);
            case "Feed Cns." -> MetricsUtil.feedPerDayList(animals);
            case "Weight" -> MetricsUtil.weightList(animals);
            default -> Collections.emptyList();
        };

        if (values.isEmpty()) {
            return new PopulationDistributionGraphMetrics(
                    Collections.nCopies(DISTRIBUTION_BIN_COUNT, ""),
                    Collections.nCopies(DISTRIBUTION_BIN_COUNT, 0));
        }

        double min = Collections.min(values);
        double max = Collections.max(values);
        if (min == max)
            max = min + 1;
        double binWidth = (max - min) / DISTRIBUTION_BIN_COUNT;

        List<Integer> binCounts = new ArrayList<>(Collections.nCopies(DISTRIBUTION_BIN_COUNT, 0));
        for (double v : values) {
            int bin = Math.min((int) ((v - min) / binWidth), DISTRIBUTION_BIN_COUNT - 1);
            binCounts.set(bin, binCounts.get(bin) + 1);
        }

        List<String> labels = new ArrayList<>(DISTRIBUTION_BIN_COUNT);
        for (int i = 0; i < DISTRIBUTION_BIN_COUNT; i++) {
            double rangeStart = min + i * binWidth;
            double rangeEnd = rangeStart + binWidth;
            labels.add(String.format(DateFormattingUtil.APP_LOCALE, "%.2f–%.2f", rangeStart, rangeEnd));
        }

        return new PopulationDistributionGraphMetrics(labels, binCounts);
    }

    public static HistoricalImportComparisonMetrics calculateHistoricalImportComparisonMetrics(Integer groupId,
            String metric, String period) throws SQLException {
        if (groupId == null)
            return null;

        List<Report> completed = ReportDAO.findCompletedByGroupId(groupId);
        if (completed.isEmpty())
            return empty();

        completed.sort(Comparator.comparing(Report::createdAt));
        LocalDateTime latest = completed.get(completed.size() - 1).createdAt();
        LocalDateTime lowerBound = TimeUtil.minusPeriod(latest, period);
        List<Report> relevant = completed.stream().filter(r -> !r.createdAt().isBefore(lowerBound)).toList();

        if (relevant.isEmpty())
            return empty();

        Function<List<Animal>, Double> metricFn = switch (metric) {
            case "FCR" -> a -> MathUtil.mean(MetricsUtil.fcrList(a));
            case "Feed Cns." -> a -> MathUtil.mean(MetricsUtil.feedPerDayList(a));
            case "Weight" -> a -> MathUtil.mean(MetricsUtil.weightList(a));
            default -> a -> 0.0;
        };

        List<Double> means = new ArrayList<>(relevant.size());
        for (Report report : relevant) {
            means.add(metricFn.apply(getAnimalsForReport(report)));
        }

        double baseline = means.get(0);
        List<Double> percentChanges = means.stream().map(v -> MathUtil.percentChange(baseline, v)).toList();
        List<String> labels = relevant.stream().map(DashboardCreationService::labelFor).toList();

        return new HistoricalImportComparisonMetrics(labels, percentChanges);
    }

    private static HistoricalImportComparisonMetrics empty() {
        return new HistoricalImportComparisonMetrics(List.of(), List.of());
    }

    private static String labelFor(Report r) {
        if (r.importStart() != null && r.importEnd() != null) {
            long midpointSeconds = Duration.between(r.importStart(), r.importEnd()).getSeconds() / 2;
            return r.importStart().plusSeconds(midpointSeconds).format(DateFormattingUtil.dateFormatter);
        }
        return r.createdAt().format(DateFormattingUtil.dateFormatter);
    }

    private static KpiMetrics buildKpi(String title, String unit, int decimals, String description,
            List<Animal> latestAnimals, List<Animal> previousAnimals, List<Report> completed,
            Function<List<Animal>, Double> metric) throws SQLException {
        double current = metric.apply(latestAnimals);
        double previous = previousAnimals.isEmpty() ? 0 : metric.apply(previousAnimals);
        double change = MathUtil.percentChange(previous, current);
        List<Double> history = buildKpiHistory(completed, metric);
        return new KpiMetrics(title, current, decimals, unit, change, description, history);
    }

    private static List<Double> buildKpiHistory(List<Report> reports, Function<List<Animal>, Double> kpi)
            throws SQLException {
        int steps = Math.min(reports.size(), KPI_HISTORY_SIZE);
        List<Double> history = new ArrayList<>(steps);
        for (int i = 0; i < steps; i++) {
            history.add(kpi.apply(getAnimalsForReport(reports.get(i))));
        }
        return history;
    }

    private static List<Animal> getAnimalsForReport(Report report) throws SQLException {
        if (report == null)
            return Collections.emptyList();

        List<Visit> visits = VisitDAO.findByReportId(report.id());
        Map<Integer, List<Visit>> visitsByAnimal = visits.stream()
                .collect(Collectors.groupingBy(Visit::animalNumber));
        return buildAnimalsForPeriod(visitsByAnimal);
    }

    private static List<Animal> buildAnimalsForPeriod(Map<Integer, List<Visit>> visitsByAnimal) throws SQLException {
        List<Animal> animals = new ArrayList<>();
        for (Map.Entry<Integer, List<Visit>> entry : visitsByAnimal.entrySet()) {
            Animal a = AnimalSyncService.buildAnimalForPeriod(entry.getKey(), entry.getValue());
            if (a != null) {
                animals.add(a);
            }
        }
        return animals;
    }
}
