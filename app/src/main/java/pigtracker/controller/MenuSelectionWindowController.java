// Theis Thomsen

package pigtracker.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MenuSelectionWindowController {

    @FXML private Label selectionOptionLabel;
    @FXML private ListView<String> selectionListView;
    @FXML private Button selectButton;

    private String selectedOption;

    @FXML
    private void initialize() {
        selectButton.setDisable(true);
        selectionListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> selectButton.setDisable(newSel == null));
    }

    public void setOptions(String labelText, ObservableList<String> options) {
        selectionOptionLabel.setText(String.format("Select %s", labelText));
        selectionListView.setItems(options);
    }

    @FXML
    private void select() {
        selectedOption = selectionListView.getSelectionModel().getSelectedItem();
        ((Stage) selectButton.getScene().getWindow()).close();
    }

    public static String showAndWait(Stage owner, String label, ObservableList<String> options) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MenuSelectionWindowController.class.getResource("/views/menu-selection-window-view.fxml"));
            Parent root = loader.load();

            MenuSelectionWindowController controller = loader.getController();
            controller.setOptions(label, options);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null)
                stage.initOwner(owner);
            stage.setTitle(label);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.selectedOption;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
