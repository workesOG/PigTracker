package pigtracker.controller;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import pigtracker.controller.components.FilterChipContainerController;
import pigtracker.controller.components.FilterChipController;
import pigtracker.controller.components.SegmentedToggleController;
import pigtracker.model.MetricOption;

@SuppressWarnings("unused")
public class SpecificDataController {
    @FXML
    private TextField searchTextField;

    @FXML
    private Button searchButton;

    @FXML
    private ChoiceBox<MetricOption> sortDropdown;

    @FXML
    private SegmentedToggleController sortSegmentedToggleController;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private FilterChipContainerController filterChipContainerController;

    private ArrayList<MetricOption> availableMetrics;

    @FXML
    public void initialize() {
        sortSegmentedToggleController.setOptions("Asc.", "Desc.");
    }

    public void setMetrics(ArrayList<MetricOption> metrics) {
        availableMetrics = metrics;

        sortDropdown.getItems().add(null);
        sortDropdown.getItems().addAll(metrics);

        filterChipContainerController.setMetrics(metrics);
    }
}
