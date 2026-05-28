// Af Nikolaj Jakobsen

package pigtracker.util;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

// Centralizes the JavaFX Alert boilerplate so callers can pop a dialog in one line.
public final class Alerts {

    private Alerts() {}

    public static void info(String title, String message) {
        show(AlertType.INFORMATION, title, null, message);
    }

    public static void error(String title, String header, String message) {
        show(AlertType.ERROR, title, header, message);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private static void show(AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
