// Theis Thomsen

package pigtracker.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pigtracker.dao.ReportDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Animal;
import pigtracker.model.Report;
import pigtracker.model.ReportMetrics;
import pigtracker.model.Visit;
import pigtracker.util.MetricsUtil;

public final class ReportImportService {

    private ReportImportService() {}

    public static ReportMetrics generateReportData(Report report) throws SQLException {
        List<Visit> visits = VisitDAO.findByReportId(report.id());
        Map<Integer, List<Visit>> visitsByAnimal = visits.stream()
                .collect(Collectors.groupingBy(Visit::animalNumber));

        List<Animal> animals = new ArrayList<>();
        for (Map.Entry<Integer, List<Visit>> entry : visitsByAnimal.entrySet()) {
            Animal snapshot = AnimalSyncService.buildAnimalForPeriod(entry.getKey(), entry.getValue());
            if (snapshot != null) {
                animals.add(snapshot);
            }
        }

        return new ReportMetrics(
                MetricsUtil.calculateMeanMedianMetrics(animals, visits, visitsByAnimal),
                MetricsUtil.calculateTopThreePigs(animals, visits, visitsByAnimal),
                MetricsUtil.calculateActivityByHour(visits));
    }

    public static int createReport(int groupId, LocalDateTime importStart, LocalDateTime importEnd, int rowCount,
            int pigCount, int createdBy) throws SQLException {
        return createReport(groupId, importStart, importEnd, rowCount, pigCount, createdBy, null);
    }

    public static int createReport(int groupId, LocalDateTime importStart, LocalDateTime importEnd, int rowCount,
            int pigCount, int createdBy, LocalDateTime createdAt) throws SQLException {
        Report report = new Report(0, groupId, importStart, importEnd, rowCount, pigCount, null, createdBy, createdAt);
        return ReportDAO.create(report).id();
    }
}
