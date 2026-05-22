package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilterChipContainerController {

    @FXML
    private FlowPane container;

    @FXML
    private Button addFilterButton;

    private final List<FilterChipController> filters = new ArrayList<>();

    public FilterChipController addFilter(
            String value,
            FilterChipController.Operator operator,
            double num1) {

        return addFilter(value, operator, num1, null);
    }

    public FilterChipController addFilter(
            String value,
            FilterChipController.Operator operator,
            double num1,
            Double num2) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/filter-chip.fxml"));
            Node chip = loader.load();

            FilterChipController controller = loader.getController();
            controller.setRule(value, operator, num1, num2);
            controller.setRemoveAction(() -> removeFilter(chip, controller));

            int insertIndex = container.getChildren().indexOf(addFilterButton);
            container.getChildren().add(insertIndex, chip);

            filters.add(controller);

            return controller;

        } catch (IOException e) {

            throw new RuntimeException("Could not load filter chip", e);
        }
    }

    public void removeFilter(
            Node chip,
            FilterChipController controller) {

        container.getChildren().remove(chip);

        filters.remove(controller);
    }

    public List<FilterChipController> getFilters() {

        return List.copyOf(filters);
    }
}