// Af Nikolaj Jakobsen

package pigtracker.dao;

import pigtracker.model.DbReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ReportDAO {

    private ReportDAO() {}

    // Inserts a new report and returns it, including the database-generated id.
    public static DbReport create(DbReport report) throws SQLException {
        String sql = "INSERT INTO Reports (import_start, import_end, row_count, pig_count, created_by) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindReport(ps, report);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;

                return report.withId(id);
            }
        }
    }

    // Returns the report with the given id, or empty if it does not exist.
    public static Optional<DbReport> findById(int id) throws SQLException {
        String sql = "SELECT id, import_start, import_end, row_count, pig_count, created_by, created_at FROM Reports WHERE id = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    // Returns every report, newest first.
    public static List<DbReport> getAll() throws SQLException {
        String sql = "SELECT id, import_start, import_end, row_count, pig_count, created_by, created_at FROM Reports ORDER BY created_at DESC";
        List<DbReport> reports = new ArrayList<>();

        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reports.add(mapRow(rs));
            }
        }

        return reports;
    }

    // Returns all reports created by the given user, newest first.
    public static List<DbReport> findByCreatedBy(int userId) throws SQLException {
        String sql = "SELECT id, import_start, import_end, row_count, pig_count, created_by, created_at FROM Reports WHERE created_by = ? ORDER BY created_at DESC";
        List<DbReport> reports = new ArrayList<>();

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapRow(rs));
                }
            }
        }

        return reports;
    }

    // Deletes the report with the given id; returns true if a row was removed.
    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Reports WHERE id = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Binds the five insert columns (everything except id and created_at) onto the
    // statement.
    private static void bindReport(PreparedStatement ps, DbReport r) throws SQLException {
        ps.setObject(1, r.importStart());
        ps.setObject(2, r.importEnd());
        ps.setInt(3, r.rowCount());
        ps.setInt(4, r.pigCount());
        ps.setInt(5, r.createdBy());
    }

    // Builds a DbReport object from the current row of the given ResultSet.
    private static DbReport mapRow(ResultSet rs) throws SQLException {
        return new DbReport(rs.getInt("id"), rs.getObject("import_start", LocalDateTime.class), rs.getObject("import_end", LocalDateTime.class), rs.getInt("row_count"), rs.getInt("pig_count"), rs.getInt("created_by"), rs.getObject("created_at", LocalDateTime.class));
    }
}
