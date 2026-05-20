// Af Nikolaj Jakobsen

package pigtracker.dao;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionDAO {

    private static final Dotenv ENV = Dotenv.configure()
        .ignoreIfMissing()
        .load();

    private static final String URL = require("DB_URL");
    private static final String USER = require("DB_USER");
    private static final String PASSWORD = require("DB_PASSWORD");

    private ConnectionDAO() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean canConnectToDatabase() {
        try (Connection _ = getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String require(String key) {
        String value = ENV.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required variable in .env: " + key);
        }

        return value;
    }
}