// Theis Thomsen

package pigtracker.controller.components;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class SegmentedToggleController {

    @FXML private HBox container;

    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final List<ToggleButton> buttons = new ArrayList<>();
    private final ReadOnlyStringWrapper selected = new ReadOnlyStringWrapper();

    private ToggleButton pressedButton;
    private boolean allowNoSelection;

    public SegmentedToggleController() {
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) ->
                selected.set(newToggle == null ? null : ((ToggleButton) newToggle).getText()));
    }

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
            button.getStyleClass().add(segmentStyleClass(i, labels.length));

            if (allowNoSelection) {
                button.setOnMousePressed(e -> pressedButton = button.isSelected() ? button : null);
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

        if (!allowNoSelection && !buttons.isEmpty()) {
            buttons.get(0).setSelected(true);
        }
        container.setFillHeight(true);
    }

    private static String segmentStyleClass(int index, int total) {
        if (index == 0) return "segment-left";
        if (index == total - 1) return "segment-right";
        return "segment-middle";
    }

    public ReadOnlyStringProperty selectedProperty() {
        return selected.getReadOnlyProperty();
    }

    public void addSelectionChangeListener(ChangeListener<? super String> listener) {
        selected.addListener(listener);
    }

    public void setSelected(String label) {
        ToggleButton button = findByLabel(label);
        if (button == null) {
            throw new IllegalArgumentException("Option not found: " + label);
        }
        button.setSelected(true);
    }

    public void clearSelection() {
        if (!allowNoSelection)
            throw new IllegalStateException("Selection cannot be cleared");
        toggleGroup.selectToggle(null);
    }

    public String getSelected() {
        return selected.get();
    }

    public boolean hasSelection() {
        return toggleGroup.getSelectedToggle() != null;
    }

    public void enableToggle(String label, boolean enabled) {
        ToggleButton button = findByLabel(label);
        if (button == null) {
            throw new IllegalArgumentException("Option not found: " + label);
        }
        button.setDisable(!enabled);
    }

    private ToggleButton findByLabel(String label) {
        for (ToggleButton button : buttons) {
            if (button.getText().equals(label)) {
                return button;
            }
        }
        return null;
    }
}
