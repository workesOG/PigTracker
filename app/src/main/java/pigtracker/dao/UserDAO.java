// Af Nikolaj Jakobsen

package pigtracker.dao;

import pigtracker.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UserDAO {

    private UserDAO() {}

    // Inserts a new user and returns it, including the database-generated id.
    public static User create(String username, String password, User.Permission permission) throws SQLException {
        String sql = "INSERT INTO Users (username, password, permission) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, permission.name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;

                return new User(id, username, password, permission);
            }
        }
    }

    // Returns the user matching both username and password, or empty if none match.
    public static Optional<User> findByCredentials(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, permission FROM Users WHERE username = ? AND password = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    // Returns the user with the given username, or empty if no such user exists.
    public static Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, permission FROM Users WHERE username = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    // Returns every user in the table, ordered alphabetically by username.
    public static List<User> getAll() throws SQLException {
        String sql = "SELECT id, username, password, permission FROM Users ORDER BY username";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }

        return users;
    }

    // Updates an existing user by id; returns true if a row was changed.
    public static boolean update(User user) throws SQLException {
        String sql = "UPDATE Users SET username = ?, password = ?, permission = ? WHERE id = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, user.password());
            ps.setString(3, user.permission().name());
            ps.setInt(4, user.id());

            return ps.executeUpdate() > 0;
        }
    }

    // Deletes the user with the given id; returns true if a row was removed.
    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Users WHERE id = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Builds a User object from the current row of the given ResultSet.
    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            
            User.Permission.valueOf(rs.getString("permission").trim().toUpperCase())
        );
    }
}