// Theis Thomsen

package pigtracker.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pigtracker.dao.GroupDAO;
import pigtracker.dao.ReportDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Group;
import pigtracker.model.Report;
import pigtracker.model.Visit;
import pigtracker.util.AppContext;
import pigtracker.util.Session;

public final class ImportService {

    private static final String DELIMITER = ";";
    private static final DateTimeFormatter CSV_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm",
            Locale.ENGLISH);
    private static final List<String> REQUIRED_COLUMNS = List.of(
            "animal_number", "responder", "location", "visit_time", "duration", "weight", "feed_intake");

    private ImportService() {}

    private record ParsedCsv(List<Visit> visits, Set<Integer> pigNumbers,
            LocalDateTime minVisit, LocalDateTime maxVisit, int rowCount) {}

    public static int importFromCSV(File file, String groupName) throws IOException {
        return performImport(parseVisitsFromCSV(file), groupName, null);
    }

    public static int importFromCSV(File file, String groupName, Runnable onComplete) throws IOException {
        int reportId = importFromCSV(file, groupName);
        if (onComplete != null) {
            onComplete.run();
        }
        return reportId;
    }

    public static int importFromCSV(File file, String groupName, LocalDateTime createdAt) throws IOException {
        return performImport(parseVisitsFromCSV(file), groupName, createdAt);
    }

    private static ParsedCsv parseVisitsFromCSV(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null)
                throw new IOException("CSV file is empty");

            Map<String, Integer> idx = parseHeaderIndexes(headerLine);
            for (String required : REQUIRED_COLUMNS) {
                if (!idx.containsKey(required))
                    throw new IOException("CSV missing required column: " + required);
            }

            List<Visit> visits = new ArrayList<>();
            Set<Integer> pigNumbers = new HashSet<>();
            LocalDateTime minVisit = null;
            LocalDateTime maxVisit = null;
            int rowCount = 0;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;

                Visit visit = parseRow(line.split(DELIMITER), idx);
                visits.add(visit);
                pigNumbers.add(visit.animalNumber());

                if (minVisit == null || visit.visitTime().isBefore(minVisit))
                    minVisit = visit.visitTime();
                if (maxVisit == null || visit.visitTime().isAfter(maxVisit))
                    maxVisit = visit.visitTime();
                rowCount++;
            }

            if (visits.isEmpty() || minVisit == null || maxVisit == null)
                throw new IOException("CSV file contains no valid visit data.");

            return new ParsedCsv(visits, pigNumbers, minVisit, maxVisit, rowCount);
        }
    }

    private static Map<String, Integer> parseHeaderIndexes(String headerLine) {
        Map<String, Integer> indexes = new HashMap<>();
        String[] headers = headerLine.split(DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            String normalized = headers[i].toLowerCase(Locale.ENGLISH).trim().replace("﻿", "");
            if (normalized.equals("weight (g)"))
                normalized = "weight";
            indexes.put(normalized, i);
        }
        return indexes;
    }

    private static Visit parseRow(String[] parts, Map<String, Integer> idx) {
        int animalNumber = Integer.parseInt(parts[idx.get("animal_number")].trim());
        String responder = parts[idx.get("responder")].trim();
        int location = Integer.parseInt(parts[idx.get("location")].trim());
        LocalDateTime visitTime = LocalDateTime.parse(parts[idx.get("visit_time")].trim(), CSV_DATE_TIME);
        int durationSec = Integer.parseInt(parts[idx.get("duration")].trim());
        String weightStr = parts[idx.get("weight")].trim();
        int weightG = weightStr.isEmpty() ? 0 : Integer.parseInt(weightStr);
        int feedIntakeG = Integer.parseInt(parts[idx.get("feed_intake")].trim());

        return new Visit(0, animalNumber, responder, -1, location, visitTime, durationSec, weightG, feedIntakeG);
    }

    private static int performImport(ParsedCsv parsed, String groupName, LocalDateTime customCreatedAt)
            throws IOException {
        int groupId;
        int reportId;
        try {
            groupId = GroupHandlingService.getOrCreateGroup(groupName);
        } catch (SQLException e) {
            throw new IOException("Failed to create group in database", e);
        }
        try {
            AnimalSyncService.syncAnimalData(parsed.visits(), groupId);
        } catch (Exception e) {
            throw new IOException("Failed to sync animal data", e);
        }
        try {
            int userId = Session.getCurrentUser().id();
            reportId = (customCreatedAt == null)
                    ? ReportImportService.createReport(groupId, parsed.minVisit(), parsed.maxVisit(),
                            parsed.rowCount(), parsed.pigNumbers().size(), userId)
                    : ReportImportService.createReport(groupId, parsed.minVisit(), parsed.maxVisit(),
                            parsed.rowCount(), parsed.pigNumbers().size(), userId, customCreatedAt);
        } catch (SQLException e) {
            throw new IOException("Failed to create report in database", e);
        }

        List<Visit> withReportId = parsed.visits().stream().map(v -> v.withReportId(reportId)).toList();
        try {
            VisitDAO.insertBatch(withReportId);
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
}
