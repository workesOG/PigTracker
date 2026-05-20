package pigtracker;

import java.sql.Connection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pigtracker.dao.ConnectionDAO;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {

        primaryStage = stage;

        if (hasSavedSession()) {
            showMainView();
        } else {
            showLoginView();
        }

        primaryStage.show();

        try (Connection conn = ConnectionDAO.getConnection()) {
            System.out.println("Connected. Database: " + conn.getCatalog());
        } catch (Exception e) {
            System.out.println("FAILED to connect:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private boolean hasSavedSession() {
        // needs implementation
        return false;
    }

    public static void showLoginView() throws Exception {
        FXMLLoader loader = 
                new FXMLLoader(Main.class.getResource("/views/login-view.fxml"));

        Scene scene = new Scene(loader.load());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
    }
    
    public static void showMainView() throws Exception {
        FXMLLoader loader = 
                new FXMLLoader(Main.class.getResource("/views/main-view.fxml"));

        Scene scene = new Scene(loader.load());

        primaryStage.setScene(scene);
        primaryStage.setTitle("PPT Manager Interface");
    }
}
