// Af Nikolaj Jakobsen

package pigtracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import pigtracker.model.Visit;

public final class VisitDAO {

    private static final String COLUMNS = "id, animal_number, responder, report_id, location, visit_time, "
            + "duration_sec, weight_g, feed_intake_g";
    private static final String SELECT_ALL = "SELECT " + COLUMNS + " FROM Visits";
    private static final String INSERT = "INSERT INTO Visits (animal_number, responder, report_id, location, "
            + "visit_time, duration_sec, weight_g, feed_intake_g) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private VisitDAO() {}

    // Inserts a single visit and returns it, including the database-generated id.
    public static Visit insert(Visit visit) throws SQLException {
        int id = Db.insertReturningId(INSERT, ps -> bindVisit(ps, visit));
        return visit.withId(id);
    }

    // Inserts many visits in one transaction. Used when importing a PPT visit file.
    public static void insertBatch(List<Visit> visits) throws SQLException {
        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(INSERT)) {
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

    // Returns all visits for one animal (by responder), oldest first.
    public static List<Visit> findByResponder(String responder) throws SQLException {
        return Db.findMany(SELECT_ALL + " WHERE responder = ? ORDER BY visit_time",
                ps -> ps.setString(1, responder), VisitDAO::mapRow);
    }

    // Returns all visits belonging to the given report, oldest first.
    public static List<Visit> findByReportId(int reportId) throws SQLException {
        return Db.findMany(SELECT_ALL + " WHERE report_id = ? ORDER BY visit_time",
                ps -> ps.setInt(1, reportId), VisitDAO::mapRow);
    }

    // Theis Thomsen
    // Returns all visits for a given animal_number, oldest first.
    public static List<Visit> findByAnimalNumber(int animalNumber) throws SQLException {
        return Db.findMany(SELECT_ALL + " WHERE animal_number = ? ORDER BY visit_time",
                ps -> ps.setInt(1, animalNumber), VisitDAO::mapRow);
    }

    // Returns every visit, oldest first. Note: this table can be very large.
    public static List<Visit> getAll() throws SQLException {
        return Db.findMany(SELECT_ALL + " ORDER BY visit_time", Db.NO_PARAMS, VisitDAO::mapRow);
    }

    // Deletes all visits for one animal (by responder); returns the number of rows removed.
    public static int deleteByResponder(String responder) throws SQLException {
        return Db.executeUpdate("DELETE FROM Visits WHERE responder = ?", ps -> ps.setString(1, responder));
    }

    // Theis Thomsen
    public static int deleteByReportId(int reportId) throws SQLException {
        return Db.executeUpdate("DELETE FROM Visits WHERE report_id = ?", ps -> ps.setInt(1, reportId));
    }

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

    private static Visit mapRow(ResultSet rs) throws SQLException {
        return new Visit(
                rs.getInt("id"),
                rs.getInt("animal_number"),
                rs.getString("responder"),
                rs.getInt("report_id"),
                rs.getInt("location"),
                rs.getObject("visit_time", LocalDateTime.class),
                rs.getInt("duration_sec"),
                Db.nullableInt(rs, "weight_g"),
                rs.getInt("feed_intake_g"));
    }
}
