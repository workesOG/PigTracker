package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.List;

public class SegmentedToggleController {
    @FXML
    private HBox container;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private final List<ToggleButton> buttons = new ArrayList<>();

    public void setOptions(
            String... labels) {

        container.getChildren().clear();
        buttons.clear();

        for (int i = 0; i < labels.length; i++) {

            ToggleButton button = new ToggleButton(labels[i]);

            button.setToggleGroup(toggleGroup);

            // Equal width
            button.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(button, Priority.ALWAYS);

            if (i == 0) {
                button.getStyleClass().add("segment-left");

            } else if (i == labels.length - 1) {
                button.getStyleClass().add("segment-right");

            } else {
                button.getStyleClass().add("segment-middle");
            }

            container.getChildren().add(button);

            buttons.add(button);
        }

        if (!buttons.isEmpty()) {
            buttons.get(0).setSelected(true);
        }

        container.fillHeightProperty().set(true);
    }

    public String getSelected() {

        ToggleButton selected = (ToggleButton) toggleGroup
                .getSelectedToggle();

        return selected == null
                ? null
                : selected.getText();
    }
}
