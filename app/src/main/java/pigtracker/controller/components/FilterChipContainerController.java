// Theis Thomsen

package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pigtracker.controller.FilterCreationFormController;
import pigtracker.controller.components.FilterChipController.Operator;
import pigtracker.model.MetricOption;

public class FilterChipContainerController {

    @FXML
    private FlowPane container;

    @FXML
    private Button addFilterButton;

    private ArrayList<MetricOption> availableMetrics;
    private Runnable filtersChangedListener;

    private final List<FilterChipController> filters = new ArrayList<>();

    public void setMetrics(ArrayList<MetricOption> metrics) {
        availableMetrics = metrics;
    }

    public void setOnFiltersChanged(Runnable listener) {
        this.filtersChangedListener = listener;
    }

    @FXML
    private void openFilterCreationPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/filter-creation-form.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            FilterCreationFormController controller = loader.getController();

            controller.setupForm(availableMetrics);
            controller.setFilterChipContainerController(this);

            stage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Could not open filter creation popup", e);
        }
    }

    public FilterChipController addFilter(MetricOption metric, Operator operator, String value1) {

        return addFilter(metric, operator, value1, null);
    }

    public FilterChipController addFilter(MetricOption metric, FilterChipController.Operator operator, String value1,
            String value2) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/filter-chip.fxml"));
            Node chip = loader.load();

            FilterChipController controller = loader.getController();
            controller.setRule(metric.getLabel(), operator, value1, value2);
            controller.setRemoveAction(() -> removeFilter(chip, controller));

            int insertIndex = container.getChildren().indexOf(addFilterButton);
            container.getChildren().add(insertIndex, chip);

            filters.add(controller);

            if (filtersChangedListener != null)
                filtersChangedListener.run();
            return controller;

        } catch (IOException e) {

            throw new RuntimeException("Could not load filter chip", e);
        }
    }

    public void removeFilter(Node chip, FilterChipController controller) {

        container.getChildren().remove(chip);

        filters.remove(controller);
        if (filtersChangedListener != null)
            filtersChangedListener.run();
    }

    public List<FilterChipController> getFilters() {

        return List.copyOf(filters);
    }
}