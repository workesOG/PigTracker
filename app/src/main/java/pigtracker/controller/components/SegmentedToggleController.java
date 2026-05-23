package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class SegmentedToggleController {

    @FXML
    private HBox container;

    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final List<ToggleButton> buttons = new ArrayList<>();

    private ToggleButton pressedButton;
    private boolean allowNoSelection;

    private ChangeListener<String> selectionChangeListener;

    public void setOptions(String... labels) {
        setOptions(false, labels);
    }

    public void setOptions(boolean allowNoSelection, String... labels) {

        this.allowNoSelection = allowNoSelection;

        container.getChildren().clear();
        buttons.clear();

        toggleGroup.selectToggle(null);
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (selectionChangeListener != null) {
                String oldSelection = oldToggle == null ? null : ((ToggleButton)oldToggle).getText();
                String newSelection = newToggle == null ? null : ((ToggleButton)newToggle).getText();
                selectionChangeListener.changed(new ObservableValue<String>() {
                    @Override
                    public void addListener(ChangeListener<? super String> listener) {}

                    @Override
                    public void removeListener(ChangeListener<? super String> listener) {}

                    @Override
                    public void addListener(javafx.beans.InvalidationListener listener) {}

                    @Override
                    public void removeListener(javafx.beans.InvalidationListener listener) {}

                    @Override
                    public String getValue() {
                        return newSelection;
                    }
                }, oldSelection, newSelection);
            }
        });

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
                    pressedButton = button.isSelected() ? button : null;
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

    public void addSelectionChangeListener(ChangeListener<String> listener) {
        this.selectionChangeListener = listener;
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

        ToggleButton selected = (ToggleButton)toggleGroup.getSelectedToggle();

        return selected == null ? null : selected.getText();
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