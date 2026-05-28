// Af Nikolaj Jakobsen

package pigtracker.controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pigtracker.Main;
import pigtracker.service.AuthenticationException;
import pigtracker.service.UserService;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            UserService.login(username, password);
        } catch (AuthenticationException e) {
            showError("Invalid username or password");
            passwordField.clear();
            return;
        } catch (SQLException e) {
            showError("Could not reach the database");
            e.printStackTrace();
            return;
        }

        try {
            Main.showMainView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
