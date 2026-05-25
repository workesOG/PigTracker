package pigtracker.controller;

import javafx.fxml.FXML;
import pigtracker.Main;
import pigtracker.service.UserService;
import pigtracker.util.Session;

public class MainController {
    // Nikolaj Jakobsen
    @FXML
    private void handleLogout() {
        UserService.logout();

        try {
            Main.showLoginView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Theis Thomsen
    @FXML
    private void handleClearSessionData() {
        Session.clear();
    }
}
