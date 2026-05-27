// Theis Thomsen

package pigtracker.dao;

import pigtracker.model.Group;
import java.sql.*;
import java.util.*;

public final class GroupDAO {
    private GroupDAO() {}

    public static Group create(Group group) throws SQLException {
        String sql = "INSERT INTO Groups (name, creation_status) VALUES (?, ?)";
        try (Connection conn = ConnectionDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, group.name());
            ps.setString(2, group.creationStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;
                return group.withId(id);
            }
        }
    }

    public static Optional<Group> findById(int id) throws SQLException {
        String sql = "SELECT id, name, creation_status FROM Groups WHERE id = ?";
        try (Connection conn = ConnectionDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public static Optional<Group> findByName(String name) throws SQLException {
        String sql = "SELECT id, name, creation_status FROM Groups WHERE name = ?";
        try (Connection conn = ConnectionDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public static List<Group> getAll() throws SQLException {
        String sql = "SELECT id, name, creation_status FROM Groups ORDER BY id";
        List<Group> groups = new ArrayList<>();
        try (Connection conn = ConnectionDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                groups.add(mapRow(rs));
            }
        }
        return groups;
    }

    public static boolean update(Group group) throws SQLException {
        String sql = "UPDATE Groups SET name = ?, creation_status = ? WHERE id = ?";
        try (Connection conn = ConnectionDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, group.name());
            ps.setString(2, group.creationStatus().name());
            ps.setInt(3, group.id());
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Groups WHERE id = ?";
        try (Connection conn = ConnectionDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Group mapRow(ResultSet rs) throws SQLException {
        return new Group(
            rs.getInt("id"),
            rs.getString("name"),
            Group.CreationStatus.valueOf(rs.getString("creation_status").trim().toUpperCase())
        );
    }
}