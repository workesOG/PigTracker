// Theis Thomsen

package pigtracker.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pigtracker.controller.components.InspectorFieldController;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Animal;
import pigtracker.model.MetricColumn;
import pigtracker.model.MetricOption;
import pigtracker.model.Visit;
import pigtracker.util.AppContext;

public class DataController {
    @FXML
    private SpecificDataController animalDataController;

    @FXML
    private SpecificDataController visitDataController;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab animalTab;

    @FXML
    private Tab visitTab;

    @FXML
    private VBox inspectorFieldContainer;

    @FXML
    private Button inspectorResetButton;

    @FXML
    private Button inspectorUpdateButton;

    private Object inspectorRow;
    private List<MetricColumn> inspectorColumns;
    private Map<String, TextField> inspectorEditors;
    private Map<String, String> originalValues;

    @FXML
    public void initialize() {
        AppContext.setDataController(this);
        loadAnimalMetrics();
        loadVisitMetrics();

        addTabPaneListener();
    }

    @FXML
    private void opUpdate() {
        if (inspectorRow == null || inspectorColumns == null || inspectorEditors == null)
            return;

        for (MetricColumn col : inspectorColumns) {
            if (!col.isEditable())
                continue;

            TextField tf = inspectorEditors.get(col.getLabel());
            if (tf == null)
                continue;

            String editedValue = tf.getText();
            String originalValue = originalValues.get(col.getLabel());

            // Only update if changed
            if (Objects.equals(editedValue, originalValue))
                continue;

            Object parsedValue = null;
            try {
                switch (col.getType()) {
                case INTEGER:
                    parsedValue = Integer.parseInt(editedValue);
                    break;
                case DECIMAL:
                    parsedValue = Double.parseDouble(editedValue);
                    break;
                case DATE:
                    parsedValue = LocalDate.parse(editedValue, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    break;
                default:
                    parsedValue = editedValue;
                }
            } catch (Exception e) {
                continue;
            }

            var updateInstruction = col.getUpdateInstruction();
            if (updateInstruction != null) {
                updateInstruction.accept(inspectorRow, parsedValue);
            }
        }
    }

    @FXML
    private void onReset() {
        if (inspectorEditors == null || originalValues == null || inspectorColumns == null)
            return;

        for (MetricColumn col : inspectorColumns) {
            TextField tf = inspectorEditors.get(col.getLabel());
            String original = originalValues.get(col.getLabel());
            if (tf != null && original != null) {
                tf.setText(original);
            }
        }
        updateButtonStates();
    }

    private void setupMetrics(SpecificDataController controller, List<MetricColumn> columns, boolean enableDateFilter) {
        ArrayList<MetricOption> metricOptions = new ArrayList<>();
        Map<MetricOption, Function<Object, Object>> extractors = new HashMap<>();
        Map<MetricOption, Function<Object, String>> formatters = new HashMap<>();
        for (MetricColumn column : columns) {
            MetricOption opt = new MetricOption(column.getLabel(), column.getType(), column.getVisibility());
            metricOptions.add(opt);
            extractors.put(opt, column.getExtractor());
            if (column.getFormatter() != null)
                formatters.put(opt, column.getFormatter());
        }
        controller.setMetrics(metricOptions);
        controller.setMetricExtractors(extractors);
        controller.setMetricFormatters(formatters);
        controller.enableDateFilters(enableDateFilter);
        controller.setColumns(columns);
    }

    public void clearInspector() {
        inspectorFieldContainer.getChildren().clear();
        inspectorRow = null;
        inspectorColumns = null;
        inspectorEditors = null;
        originalValues = null;
        inspectorResetButton.setDisable(true);
        inspectorUpdateButton.setDisable(true);
    }

    private void loadAnimalMetrics() {
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<MetricColumn> animalColumns = List.of(
                new MetricColumn("ID", MetricOption.MetricType.INTEGER, new Boolean[] { true, true, true, true },
                        r -> ((Animal)r).id(), val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("Animal Number", MetricOption.MetricType.INTEGER,
                        new Boolean[] { true, true, true, true }, r -> ((Animal)r).animalNumber(),
                        val -> val == null ? "" : val.toString(), true,
                        (row, newValue) -> System.out.println("Success")),
                new MetricColumn("Responder", MetricOption.MetricType.STRING, new Boolean[] { true, true, true, true },
                        r -> ((Animal)r).responder(), val -> val == null ? "" : val.toString(), true,
                        (row, newValue) -> System.out.println("Success")),
                new MetricColumn("Group ID", MetricOption.MetricType.INTEGER, new Boolean[] { true, true, true, true },
                        r -> ((Animal)r).groupId(), val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("FCR", MetricOption.MetricType.DECIMAL, new Boolean[] { true, true, true, true },
                        r -> ((Animal)r).fcr(), val -> val == null ? "" : String.format("%.2f", val), false, null),
                new MetricColumn("Start Weight", MetricOption.MetricType.DECIMAL,
                        new Boolean[] { true, true, true, true }, r -> ((Animal)r).startWeightKg(),
                        val -> val == null ? "" : String.format("%.2f", val), false, null),
                new MetricColumn("Weight", MetricOption.MetricType.DECIMAL, new Boolean[] { true, true, true, true },
                        r -> ((Animal)r).latestWeightKg(), val -> val == null ? "" : String.format("%.2f", val), false,
                        null),
                new MetricColumn("Weight gain", MetricOption.MetricType.DECIMAL,
                        new Boolean[] { true, true, true, true }, r -> ((Animal)r).weightGainKg(),
                        val -> val == null ? "" : String.format("%.2f", val), false, null),
                new MetricColumn("Feed intake", MetricOption.MetricType.DECIMAL,
                        new Boolean[] { true, true, true, true }, r -> ((Animal)r).totalFeedKg(),
                        val -> val == null ? "" : String.format("%.2f", val), false, null),
                new MetricColumn("Days tested", MetricOption.MetricType.INTEGER,
                        new Boolean[] { true, true, true, true }, r -> ((Animal)r).completedDays(),
                        val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("Start day", MetricOption.MetricType.DATE, new Boolean[] { true, true, true, true },
                        r -> ((Animal)r).startDay(), val -> {
                            if (val instanceof java.time.LocalDate date) {
                                return date.format(dateFmt);
                            }
                            return val == null ? "" : val.toString();
                        }, false, null),
                new MetricColumn("Location", MetricOption.MetricType.INTEGER, new Boolean[] { true, true, true, true },
                        r -> ((Animal)r).location(), val -> val == null ? "" : val.toString(), false, null));
        setupMetrics(animalDataController, animalColumns, true);
        List<Animal> allAnimals;
        try {
            allAnimals = pigtracker.dao.AnimalDAO.getAll();
        } catch (Exception e) {
            allAnimals = List.of();
            // Optionally log/show error
        }
        animalDataController.setData(new ArrayList<>(allAnimals));
    }

    private void loadVisitMetrics() {
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<MetricColumn> visitColumns = List.of(
                new MetricColumn("ID", MetricOption.MetricType.INTEGER, new Boolean[] { true, true, true, true },
                        r -> ((Visit)r).id(), val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("Animal Number", MetricOption.MetricType.INTEGER,
                        new Boolean[] { true, true, true, true }, r -> ((Visit)r).animalNumber(),
                        val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("Responder", MetricOption.MetricType.STRING,
                        new Boolean[] { true, false, false, false }, r -> ((Visit)r).responder(),
                        val -> val == null ? "<none>" : val.toString(), false, null),
                new MetricColumn("Report ID", MetricOption.MetricType.INTEGER, new Boolean[] { true, true, true, true },
                        r -> ((Visit)r).reportId(), val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("Location", MetricOption.MetricType.INTEGER, new Boolean[] { true, true, true, true },
                        r -> ((Visit)r).location(), val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("Visit Time", MetricOption.MetricType.DATE, new Boolean[] { true, true, true, true },
                        r -> ((Visit)r).visitTime(), val -> {
                            if (val instanceof java.time.LocalDateTime dt) {
                                return dt.format(dateTimeFmt);
                            }
                            return val == null ? "" : val.toString();
                        }, false, null),
                new MetricColumn("Duration (sec)", MetricOption.MetricType.INTEGER,
                        new Boolean[] { true, true, true, true }, r -> ((Visit)r).durationSec(),
                        val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("Weight (g)", MetricOption.MetricType.INTEGER,
                        new Boolean[] { true, true, true, true }, r -> ((Visit)r).weightG(),
                        val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("Feed Intake (g)", MetricOption.MetricType.INTEGER,
                        new Boolean[] { true, true, true, true }, r -> ((Visit)r).feedIntakeG(),
                        val -> val == null ? "" : val.toString(), false, null));
        setupMetrics(visitDataController, visitColumns, true);
        List<Visit> allVisits;
        try {
            allVisits = VisitDAO.getAll();
        } catch (Exception e) {
            allVisits = List.of();
        }
        visitDataController.setData(new ArrayList<>(allVisits));
    }

    private void updateButtonStates() {
        boolean anyDirty = false;
        boolean allValid = true;
        for (MetricColumn col : inspectorColumns) {
            TextField tf = inspectorEditors.get(col.getLabel());
            String current = tf.getText();
            String original = originalValues.get(col.getLabel());
            boolean valid = validateField(current, col.getType());
            if (!valid)
                allValid = false;
            if (!Objects.equals(current, original) && valid)
                anyDirty = true;
        }
        inspectorResetButton.setDisable(!anyDirty && allValid);
        inspectorUpdateButton.setDisable(!(anyDirty && allValid));
    }

    public void populateInspector(Object selectedRow, List<MetricColumn> columns) throws IOException {
        inspectorFieldContainer.getChildren().clear();

        Map<String, String> originalValues = new HashMap<>();
        Map<String, TextField> editors = new HashMap<>();
        List<MetricColumn> editableColumns = columns.stream().filter(c -> c.isEditable()).toList();

        for (MetricColumn col : columns) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/inspector-field.fxml"));
            HBox fieldRow = loader.load();
            InspectorFieldController fieldController = loader.getController();

            String originalValue = col.getFormatter().apply(col.getExtractor().apply(selectedRow));
            fieldController.getValueLabel().setText(col.getLabel());
            fieldController.getValueField().setText(originalValue);
            fieldController.getValueField().setEditable(col.isEditable());

            String fieldKey = col.getLabel();
            originalValues.put(fieldKey, originalValue);
            editors.put(fieldKey, fieldController.getValueField());

            if (!col.isEditable()) {
                fieldController.getValueField().setStyle("-fx-background-color: #F4F4F4;");
            } else {
                fieldController.getValueField().textProperty().addListener((obs, oldVal, newVal) -> {
                    boolean valid = validateField(newVal, col.getType());
                    if (!valid) {
                        fieldController.getValueField().getStyleClass().add("invalid-field");
                    } else {
                        fieldController.getValueField().getStyleClass()
                                .removeAll(Collections.singleton("invalid-field"));
                    }
                    updateButtonStates();
                });
            }
            inspectorFieldContainer.getChildren().add(fieldRow);
        }
        this.originalValues = originalValues;
        this.inspectorRow = selectedRow;
        this.inspectorColumns = editableColumns;
        this.inspectorEditors = editors;
        updateButtonStates();
    }

    private boolean validateField(String value, MetricOption.MetricType type) {
        if (value == null || value.isBlank())
            return false;
        switch (type) {
        case INTEGER:
            return value.matches("-?\\d+");
        case DECIMAL:
            return value.matches("-?\\d+(\\.\\d+)?");
        case DATE:
            return parseDate(value) != null;
        default:
            return true;
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void addTabPaneListener() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == animalTab) {
                visitDataController.getTableView().getSelectionModel().clearSelection();
            } else if (newTab == visitTab) {
                animalDataController.getTableView().getSelectionModel().clearSelection();
            }
            clearInspector();
        });
    }
}
