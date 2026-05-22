package pigtracker.controller;

import javafx.fxml.FXML;
import pigtracker.Main;
import pigtracker.service.UserService;

public class MainController {
    @FXML
    private void handleLogout() {
        // Nikolaj Jakobsen
        UserService.logout();

        try {
            Main.showLoginView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
