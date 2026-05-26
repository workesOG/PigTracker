// Af Nikolaj Jakobsen

package pigtracker.dao;

import pigtracker.model.Visit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class VisitDAO {

    private VisitDAO() {}

    // Inserts a single visit and returns it, including the database-generated id.
    public static Visit insert(Visit visit) throws SQLException {
        String sql = "INSERT INTO Visits (animal_number, responder, report_id, location, visit_time, duration_sec, weight_g, feed_intake_g) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindVisit(ps, visit);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;

                return visit.withId(id);
            }
        }
    }

    // Inserts many visits in one transaction. Used when importing a PPT visit
    // file.
    public static void insertBatch(List<Visit> visits) throws SQLException {
        String sql = "INSERT INTO Visits (animal_number, responder, report_id, location, visit_time, duration_sec, weight_g, feed_intake_g) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            try {
                for (Visit visit : visits) {
                    bindVisit(ps, visit);
                    ps.addBatch();
                }

                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Returns all visits for one animal (by responder), oldest first. Used to show
    // its eating events.
    public static List<Visit> findByResponder(String responder) throws SQLException {
        String sql = "SELECT id, animal_number, responder, report_id, location, visit_time, duration_sec, weight_g, feed_intake_g FROM Visits WHERE responder = ? ORDER BY visit_time";
        List<Visit> visits = new ArrayList<>();

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, responder);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    visits.add(mapRow(rs));
                }
            }
        }

        return visits;
    }

    // Returns all visits belonging to the given report, oldest first.
    public static List<Visit> findByReportId(int reportId) throws SQLException {
        String sql = "SELECT id, animal_number, responder, report_id, location, visit_time, duration_sec, weight_g, feed_intake_g FROM Visits WHERE report_id = ? ORDER BY visit_time";
        List<Visit> visits = new ArrayList<>();

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    visits.add(mapRow(rs));
                }
            }
        }

        return visits;
    }

    // Theis Thomsen
    // Returns all visits for a given animal_number, oldest first.
    public static List<Visit> findByAnimalNumber(int animalNumber) throws SQLException {
        String sql = "SELECT id, animal_number, responder, report_id, location, visit_time, duration_sec, weight_g, feed_intake_g FROM Visits WHERE animal_number = ? ORDER BY visit_time";
        List<Visit> visits = new ArrayList<>();

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, animalNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    visits.add(mapRow(rs));
                }
            }
        }

        return visits;
    }

    // Returns every visit, oldest first. Note: this table can be very large.
    public static List<Visit> getAll() throws SQLException {
        String sql = "SELECT id, animal_number, responder, report_id, location, visit_time, duration_sec, weight_g, feed_intake_g FROM Visits ORDER BY visit_time";
        List<Visit> visits = new ArrayList<>();

        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                visits.add(mapRow(rs));
            }
        }

        return visits;
    }

    // Deletes all visits for one animal (by responder); returns the number of rows
    // removed.
    public static int deleteByResponder(String responder) throws SQLException {
        String sql = "DELETE FROM Visits WHERE responder = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, responder);

            return ps.executeUpdate();
        }
    }

    // Binds the seven visit columns onto the statement.
    private static void bindVisit(PreparedStatement ps, Visit v) throws SQLException {
        ps.setInt(1, v.animalNumber());
        ps.setString(2, v.responder());
        ps.setInt(3, v.reportId());
        ps.setInt(4, v.location());
        ps.setObject(5, v.visitTime());
        ps.setInt(6, v.durationSec());
        ps.setObject(7, v.weightG());
        ps.setInt(8, v.feedIntakeG());
    }

    // Builds a Visit object from the current row of the given ResultSet.
    private static Visit mapRow(ResultSet rs) throws SQLException {
        int weight = rs.getInt("weight_g");
        Integer weightG = rs.wasNull() ? null : weight;

        return new Visit(rs.getInt("id"), rs.getInt("animal_number"), rs.getString("responder"), rs.getInt("report_id"),
                rs.getInt("location"), rs.getObject("visit_time", LocalDateTime.class), rs.getInt("duration_sec"),
                weightG, rs.getInt("feed_intake_g"));
    }
}