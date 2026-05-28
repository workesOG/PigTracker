// Theis Thomsen
package pigtracker.service;

import pigtracker.dao.ReportDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Animal;
import pigtracker.model.MeanMedianMetric;
import pigtracker.model.Report;
import pigtracker.model.ReportMetrics;
import pigtracker.model.TopThreePigs;
import pigtracker.model.Visit;
import pigtracker.util.MetricsUtil;

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

        List<MeanMedianMetric> meanMedians = MetricsUtil.calculateMeanMedianMetrics(animals, visits, visitsByAnimal);
        List<TopThreePigs> topThree = MetricsUtil.calculateTopThreePigs(animals, visits, visitsByAnimal);
        List<Integer> activityByHour = MetricsUtil.calculateActivityByHour(visits);
        return new ReportMetrics(meanMedians, topThree, activityByHour);
    }

    public static int createReport(int groupId, LocalDateTime importStart, LocalDateTime importEnd, int rowCount,
            int pigCount, int createdBy) throws SQLException {
        Report report = new Report(0, groupId, importStart, importEnd, rowCount, pigCount, null, createdBy, null);
        Report created = ReportDAO.create(report);
        return created.id();
    }

    public static int createReport(int groupId, LocalDateTime importStart, LocalDateTime importEnd, int rowCount,
            int pigCount, int createdBy, LocalDateTime createdAt) throws SQLException {
        Report report = new Report(0, groupId, importStart, importEnd, rowCount, pigCount, null, createdBy, createdAt);
        Report created = ReportDAO.create(report);
        return created.id();
    }
}
