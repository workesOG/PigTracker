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
import java.util.*;

public class ImportService {
    private static class ParsedCSV {
        List<Visit> visits;
        Set<Integer> pigNumbers;
        LocalDateTime minVisit;
        LocalDateTime maxVisit;
        int rowCount;

        ParsedCSV(List<Visit> v, Set<Integer> p, LocalDateTime min, LocalDateTime max, int rows) {
            this.visits = v;
            this.pigNumbers = p;
            this.minVisit = min;
            this.maxVisit = max;
            this.rowCount = rows;
        }
    }

    private static ParsedCSV parseVisitsFromCSV(File file) throws IOException {
        List<Visit> visits = new ArrayList<>();
        Set<Integer> pigNumbers = new HashSet<>();
        LocalDateTime minVisit = null, maxVisit = null;
        int rowCount = 0;

        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm", Locale.ENGLISH);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null)
                throw new IOException("CSV file is empty");

            String[] headers = headerLine.split(";");

            int idxAnimalNumber = -1;
            int idxResponder = -1;
            int idxLocation = -1;
            int idxVisitTime = -1;
            int idxDuration = -1;
            int idxWeight = -1;
            int idxFeedIntake = -1;

            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].toLowerCase(Locale.ENGLISH).trim().replaceAll("[\uFEFF]", "");
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

                Integer weightG = null;
                String weightStr = parts[idxWeight].trim();
                if (!weightStr.isEmpty())
                    weightG = Integer.parseInt(weightStr);

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

        if (visits.isEmpty() || minVisit == null || maxVisit == null) {
            throw new IOException("CSV file contains no valid visit data.");
        }
        return new ParsedCSV(visits, pigNumbers, minVisit, maxVisit, rowCount);
    }

    private static int performImport(ParsedCSV parsed, String groupName, LocalDateTime customCreatedAt)
            throws IOException {
        int groupId;
        int reportId;
        try {
            groupId = GroupHandlingService.getOrCreateGroup(groupName);
        } catch (SQLException e) {
            throw new IOException("Failed to create group in database", e);
        }
        try {
            AnimalSyncService.syncAnimalData(parsed.visits, groupId);
        } catch (Exception e) {
            throw new IOException("Failed to sync animal data", e);
        }
        try {
            if (customCreatedAt == null) {
                reportId = ReportImportService.createReport(groupId, parsed.minVisit, parsed.maxVisit, parsed.rowCount,
                        parsed.pigNumbers.size(), Session.getCurrentUser().id());
            } else {
                reportId = ReportImportService.createReport(groupId, parsed.minVisit, parsed.maxVisit, parsed.rowCount,
                        parsed.pigNumbers.size(), Session.getCurrentUser().id(), customCreatedAt);
            }
        } catch (SQLException e) {
            throw new IOException("Failed to create report in database", e);
        }

        // Set reportId on all visits
        for (int i = 0; i < parsed.visits.size(); ++i) {
            parsed.visits.set(i, parsed.visits.get(i).withReportId(reportId));
        }
        try {
            VisitDAO.insertBatch(parsed.visits);
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
            AppContext.getDashboardController().setAndUpdateDashboard();
        } catch (SQLException e) {
            throw new IOException("Failed to set dashboard", e);
        }
        AppContext.getDataController().loadAnimalData();
        AppContext.getDataController().loadVisitData();

        return reportId;
    }

    public static int importFromCSV(File file, String groupName) throws IOException {
        ParsedCSV parsed = parseVisitsFromCSV(file);
        return performImport(parsed, groupName, null);
    }

    public static int importFromCSV(File file, String groupName, Runnable callback) throws IOException {
        int reportId = importFromCSV(file, groupName);
        if (callback != null) {
            callback.run();
        }
        return reportId;
    }

    public static int importFromCSV(File file, String groupName, LocalDateTime createdAt) throws IOException {
        ParsedCSV parsed = parseVisitsFromCSV(file);
        return performImport(parsed, groupName, createdAt);
    }
}