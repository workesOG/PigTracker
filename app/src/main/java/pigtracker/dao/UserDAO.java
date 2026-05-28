// Af Nikolaj Jakobsen

package pigtracker.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import pigtracker.model.User;

public final class UserDAO {

    private static final String SELECT_ALL = "SELECT id, username, password, permission FROM Users";

    private UserDAO() {}

    // Inserts a new user and returns it, including the database-generated id.
    public static User create(String username, String password, User.Permission permission) throws SQLException {
        int id = Db.insertReturningId(
                "INSERT INTO Users (username, password, permission) VALUES (?, ?, ?)",
                ps -> {
                    ps.setString(1, username);
                    ps.setString(2, password);
                    ps.setString(3, permission.name());
                });
        return new User(id, username, password, permission);
    }

    // Returns the user matching both username and password, or empty if none match.
    public static Optional<User> findByCredentials(String username, String password) throws SQLException {
        return Db.findOne(SELECT_ALL + " WHERE username = ? AND password = ?", ps -> {
            ps.setString(1, username);
            ps.setString(2, password);
        }, UserDAO::mapRow);
    }

    // Returns the user with the given username, or empty if no such user exists.
    public static Optional<User> findByUsername(String username) throws SQLException {
        return Db.findOne(SELECT_ALL + " WHERE username = ?",
                ps -> ps.setString(1, username), UserDAO::mapRow);
    }

    // Returns every user in the table, ordered alphabetically by username.
    public static List<User> getAll() throws SQLException {
        return Db.findMany(SELECT_ALL + " ORDER BY username", Db.NO_PARAMS, UserDAO::mapRow);
    }

    // Updates an existing user by id; returns true if a row was changed.
    public static boolean update(User user) throws SQLException {
        return Db.executeUpdate(
                "UPDATE Users SET username = ?, password = ?, permission = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, user.username());
                    ps.setString(2, user.password());
                    ps.setString(3, user.permission().name());
                    ps.setInt(4, user.id());
                }) > 0;
    }

    // Deletes the user with the given id; returns true if a row was removed.
    public static boolean delete(int id) throws SQLException {
        return Db.executeUpdate("DELETE FROM Users WHERE id = ?", ps -> ps.setInt(1, id)) > 0;
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                User.Permission.valueOf(rs.getString("permission").trim().toUpperCase()));
    }
}
