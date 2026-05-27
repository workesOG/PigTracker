// Theis Thomsen
package pigtracker.service;

import pigtracker.dao.GroupDAO;
import pigtracker.dao.ReportDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Visit;
import pigtracker.model.Report;
import pigtracker.model.Group;
import pigtracker.util.AppContext;
import pigtracker.util.Session;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ImportService {

    // CSV header columns (animal_number; responder; location; visit_time; duration;
    // "weight" or "weight (g)""; feed_intake)

    public static int importFromCSV(File file, String groupName) throws IOException {
        List<Visit> visits = new ArrayList<>();
        Set<Integer> pigNumbers = new HashSet<>();
        LocalDateTime minVisit = null, maxVisit = null;
        int rowCount = 0;

        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm", Locale.ENGLISH);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            String[] headers = headerLine.split(";");

            int idxAnimalNumber = -1;
            int idxResponder = -1;
            int idxLocation = -1;
            int idxVisitTime = -1;
            int idxDuration = -1;
            int idxWeight = -1;
            int idxFeedIntake = -1;

            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].toLowerCase(Locale.ENGLISH).trim().replaceAll("[\uFEFF]", ""); // Remove BOM
                                                                                                          // if present
                switch (header) {
                case "animal_number" -> idxAnimalNumber = i;
                case "responder" -> idxResponder = i;
                case "location" -> idxLocation = i;
                case "visit_time" -> idxVisitTime = i;
                case "duration" -> idxDuration = i;
                case "weight", "weight (g)" -> idxWeight = i;
                case "feed_intake" -> idxFeedIntake = i;
                }
            }

            if (idxAnimalNumber == -1 || idxResponder == -1 || idxLocation == -1 || idxVisitTime == -1
                    || idxDuration == -1 || idxWeight == -1 || idxFeedIntake == -1) {
                throw new IOException("CSV missing required columns");
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split(";");

                int animalNumber = Integer.parseInt(parts[idxAnimalNumber].trim());
                String responder = parts[idxResponder].trim();
                int location = Integer.parseInt(parts[idxLocation].trim());
                LocalDateTime visitTime = LocalDateTime.parse(parts[idxVisitTime].trim(), dtFormatter);
                int durationSec = Integer.parseInt(parts[idxDuration].trim());

                // Weight value can be absent, if PPT machine did not have time to weigh the
                // pig during the visit
                Integer weightG = null;
                String weightStr = parts[idxWeight].trim();
                if (!weightStr.isEmpty()) {
                    weightG = Integer.parseInt(weightStr);
                }

                int feedIntakeG = Integer.parseInt(parts[idxFeedIntake].trim());

                Visit visit = new Visit(0, animalNumber, responder, -1, location, visitTime, durationSec,
                        weightG != null ? weightG : 0, feedIntakeG);

                visits.add(visit);

                pigNumbers.add(animalNumber);

                if (minVisit == null || visitTime.isBefore(minVisit))
                    minVisit = visitTime;
                if (maxVisit == null || visitTime.isAfter(maxVisit))
                    maxVisit = visitTime;

                rowCount++;
            }
        }

        int groupId;
        int reportId;

        try {
            groupId = GroupHandlingService.getOrCreateGroup(groupName);
        } catch (SQLException e) {
            throw new IOException("Failed to create group in database", e);
        }

        try {
            AnimalSyncService.syncAnimalData(visits, groupId);
        } catch (Exception e) {
            throw new IOException("Failed to sync animal data", e);
        }

        try {
            reportId = ReportImportService.createReport(groupId, minVisit, maxVisit, rowCount, pigNumbers.size(),
                    Session.getCurrentUser().id());
        } catch (SQLException e) {
            throw new IOException("Failed to create report in database", e);
        }

        for (int i = 0; i < visits.size(); ++i) {
            visits.set(i, visits.get(i).withReportId(reportId));
        }

        try {
            VisitDAO.insertBatch(visits);
        } catch (Exception e) {
            throw new IOException("Failed to insert batch into database", e);
        }

        try {
            ReportDAO.updateStatus(reportId, Report.Status.COMPLETE);
            GroupDAO.updateStatus(groupId, Group.CreationStatus.COMPLETE);
        } catch (SQLException e) {
            throw new IOException("Failed to mark report/group as completed", e);
        }

        try {
            AppContext.getDashboardController().setAndUpdateDashboardIfEmpty();
        } catch (SQLException e) {
            throw new IOException("Failed to set dashboard", e);
        }

        return reportId;
    }

    public static int importFromCSV(File file, String groupName, Runnable callback) throws IOException {
        int reportId = importFromCSV(file, groupName);
        if (callback != null) {
            callback.run();
        }
        return reportId;
    }

}
