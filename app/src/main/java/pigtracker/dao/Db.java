// Af Nikolaj Jakobsen

package pigtracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Wraps the JDBC try-with-resources boilerplate every DAO would otherwise repeat.
final class Db {

    @FunctionalInterface
    interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    static final Binder NO_PARAMS = ps -> {};

    private Db() {}

    static <T> Optional<T> findOne(String sql, Binder bind, RowMapper<T> mapper) throws SQLException {
        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            bind.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapper.map(rs)) : Optional.empty();
            }
        }
    }

    static <T> List<T> findMany(String sql, Binder bind, RowMapper<T> mapper) throws SQLException {
        List<T> result = new ArrayList<>();
        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            bind.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }
        }
        return result;
    }

    static int executeUpdate(String sql, Binder bind) throws SQLException {
        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            bind.bind(ps);
            return ps.executeUpdate();
        }
    }

    static int insertReturningId(String sql, Binder bind) throws SQLException {
        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind.bind(ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    static Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}
