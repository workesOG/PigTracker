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

    private ToggleButton pressedButton;
    private boolean allowNoSelection;

    public void setOptions(String... labels) {
        setOptions(false, labels);
    }

    public void setOptions(boolean allowNoSelection, String... labels) {

        this.allowNoSelection = allowNoSelection;

        container.getChildren().clear();
        buttons.clear();

        toggleGroup.selectToggle(null);

        for (int i = 0; i < labels.length; i++) {

            ToggleButton button = new ToggleButton(labels[i]);

            button.setToggleGroup(toggleGroup);

            button.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(button, Priority.ALWAYS);

            if (i == 0)
                button.getStyleClass().add("segment-left");
            else if (i == labels.length - 1)
                button.getStyleClass().add("segment-right");
            else
                button.getStyleClass().add("segment-middle");

            if (allowNoSelection) {

                button.setOnMousePressed(e -> {
                    pressedButton = button.isSelected()
                            ? button
                            : null;
                });

                button.setOnAction(e -> {

                    if (pressedButton == button) {
                        toggleGroup.selectToggle(null);
                        pressedButton = null;
                    }
                });
            }

            container.getChildren().add(button);
            buttons.add(button);
        }

        if (!allowNoSelection && !buttons.isEmpty())
            buttons.get(0).setSelected(true);

        container.setFillHeight(true);
    }

    public void setSelected(String value) {

        for (ToggleButton button : buttons) {

            if (button.getText().equals(value)) {

                button.setSelected(true);

                return;
            }
        }

        throw new IllegalArgumentException(
                "Option not found: " + value);
    }

    public void clearSelection() {

        if (!allowNoSelection)
            throw new IllegalStateException(
                    "Selection cannot be cleared");

        toggleGroup.selectToggle(null);
    }

    public String getSelected() {

        ToggleButton selected = (ToggleButton) toggleGroup.getSelectedToggle();

        return selected == null
                ? null
                : selected.getText();
    }

    public boolean hasSelection() {
        return toggleGroup.getSelectedToggle() != null;
    }
}