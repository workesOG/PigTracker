// Af Nikolaj Jakobsen

package pigtracker.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import pigtracker.model.Report;

public final class ReportDAO {

    private static final String COLUMNS = "id, group_id, import_start, import_end, row_count, pig_count, status, "
            + "created_by, created_at";
    private static final String SELECT_ALL = "SELECT " + COLUMNS + " FROM Reports";

    private ReportDAO() {}

    // Inserts a new report and returns it, including the database-generated id.
    public static Report create(Report report) throws SQLException {
        int id = Db.insertReturningId(
                "INSERT INTO Reports (group_id, import_start, import_end, row_count, pig_count, created_by) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, report.groupId());
                    ps.setObject(2, report.importStart());
                    ps.setObject(3, report.importEnd());
                    ps.setInt(4, report.rowCount());
                    ps.setInt(5, report.pigCount());
                    ps.setInt(6, report.createdBy());
                });
        return report.withId(id);
    }

    // Returns the report with the given id, or empty if it does not exist.
    public static Optional<Report> findById(int id) throws SQLException {
        return Db.findOne(SELECT_ALL + " WHERE id = ?", ps -> ps.setInt(1, id), ReportDAO::mapRow);
    }

    // Returns every report, newest first.
    public static List<Report> getAll() throws SQLException {
        return Db.findMany(SELECT_ALL + " ORDER BY created_at DESC", Db.NO_PARAMS, ReportDAO::mapRow);
    }

    // Theis Thomsen
    // Returns only completed reports, newest first.
    public static List<Report> getAllCompleted() throws SQLException {
        return Db.findMany(SELECT_ALL + " WHERE status = ? ORDER BY created_at DESC",
                ps -> ps.setString(1, Report.Status.COMPLETE.name()), ReportDAO::mapRow);
    }

    // Returns all reports created by the given user, newest first.
    public static List<Report> findByCreatedBy(int userId) throws SQLException {
        return Db.findMany(SELECT_ALL + " WHERE created_by = ? ORDER BY created_at DESC",
                ps -> ps.setInt(1, userId), ReportDAO::mapRow);
    }

    // Theis Thomsen
    public static List<Report> findCompletedByGroupId(int groupId) throws SQLException {
        return Db.findMany(SELECT_ALL + " WHERE group_id = ? AND status = ? ORDER BY created_at DESC", ps -> {
            ps.setInt(1, groupId);
            ps.setString(2, Report.Status.COMPLETE.name());
        }, ReportDAO::mapRow);
    }

    // Deletes the report with the given id; returns true if a row was removed.
    public static boolean delete(int id) throws SQLException {
        return Db.executeUpdate("DELETE FROM Reports WHERE id = ?", ps -> ps.setInt(1, id)) > 0;
    }

    // Theis Thomsen
    public static void deleteAllInProgress() throws SQLException {
        Db.executeUpdate("DELETE FROM Reports WHERE status = ?",
                ps -> ps.setString(1, Report.Status.IN_PROGRESS.name()));
    }

    // Theis Thomsen
    // Updates the status of a report with the given id.
    public static void updateStatus(int reportId, Report.Status status) throws SQLException {
        Db.executeUpdate("UPDATE Reports SET status = ? WHERE id = ?", ps -> {
            ps.setString(1, status.name());
            ps.setInt(2, reportId);
        });
    }

    private static Report mapRow(ResultSet rs) throws SQLException {
        return new Report(
                rs.getInt("id"),
                rs.getInt("group_id"),
                rs.getObject("import_start", LocalDateTime.class),
                rs.getObject("import_end", LocalDateTime.class),
                rs.getInt("row_count"),
                rs.getInt("pig_count"),
                Report.Status.valueOf(rs.getString("status").trim().toUpperCase()),
                rs.getInt("created_by"),
                rs.getObject("created_at", LocalDateTime.class));
    }
}
