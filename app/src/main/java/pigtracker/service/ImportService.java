package pigtracker.service;

import pigtracker.dao.VisitDAO;
import pigtracker.model.Visit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImportService {

    // CSV header columns (animal_number; responder; location; visit_time; duration;
    // "weight" or "weight (g)""; feed_intake)

    public static List<Visit> importFromCSV(File file, int reportId) throws IOException {
        List<Visit> visits = new ArrayList<>();

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

                Visit visit = new Visit(0, animalNumber, responder, reportId, location, visitTime, durationSec,
                        weightG != null ? weightG : 0, feedIntakeG);

                visits.add(visit);
            }
        }

        try {
            VisitDAO.insertBatch(visits);
        } catch (Exception e) {
            throw new IOException("Failed to insert batch into database", e);
        }

        return visits;
    }

}
