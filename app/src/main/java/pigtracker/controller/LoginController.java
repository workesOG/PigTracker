package pigtracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pigtracker.Main;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean valid = username.equals("admin") && password.equals("1234");

        if (!valid) {
            errorLabel.setVisible(true);

            passwordField.clear();

            return;
        }

        try {
            Main.showMainView();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Skibidi");
    }
}
