// Af Nikolaj Jakobsen

package pigtracker.service;

import pigtracker.dao.UserDAO;
import pigtracker.model.User;
import pigtracker.util.Session;

import java.sql.SQLException;
import java.util.List;

public final class UserService {

    private UserService() {}

    // Validates the credentials and stores the user in the session, or throws if they are invalid.
    public static void login(String username, String password) throws SQLException, AuthenticationException {
        User user = UserDAO.findByCredentials(username, password)
            .orElseThrow(() -> new AuthenticationException("Invalid username or password"));
            
        Session.setCurrentUser(user);
    }

    // Logs the current user out by clearing the session.
    public static void logout() {
        Session.clear();
    }

    // Creates and saves a new user in the database.
    public static User register(String username, String password, User.Permission permission) throws SQLException {
        return UserDAO.create(username, password, permission);
    }

    // Returns all users in the system.
    public static List<User> getAllUsers() throws SQLException {
        return UserDAO.getAll();
    }

    // Updates an existing user.
    public static boolean updateUser(User user) throws SQLException {
        return UserDAO.update(user);
    }

    // Deletes the user with the given id.
    public static boolean deleteUser(int id) throws SQLException {
        return UserDAO.delete(id);
    }
}