// Theis Thomsen

package pigtracker.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pigtracker.controller.components.InspectorFieldController;
import pigtracker.dao.AnimalDAO;
import pigtracker.dao.VisitDAO;
import pigtracker.model.Animal;
import pigtracker.model.InspectorOptions;
import pigtracker.model.MetricColumn;
import pigtracker.model.MetricOption;
import pigtracker.model.Visit;
import pigtracker.service.AnimalUpdateService;
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

    private List<MetricColumn> animalColumns;
    private List<MetricColumn> visitColumns;

    @FXML
    public void initialize() {
        AppContext.setDataController(this);
        loadAnimalMetrics();
        loadVisitMetrics();
        setupMetrics(animalDataController, animalColumns, getAnimalInspectorOptions());
        setupMetrics(visitDataController, visitColumns, null);
        loadAnimalData();
        loadVisitData();

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

    private void setupMetrics(SpecificDataController controller, List<MetricColumn> columns, InspectorOptions options) {
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
        controller.enableDateFilters(true);
        controller.setColumns(columns);
        controller.setInspectorOptions(options);
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
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        animalColumns = List.of(
                new MetricColumn("ID", MetricOption.MetricType.INTEGER, new Boolean[] { true, true, true, true },
                        r -> ((Animal)r).id(), val -> val == null ? "" : val.toString(), false, null),
                new MetricColumn("Animal Number", MetricOption.MetricType.INTEGER,
                        new Boolean[] { true, true, true, true }, r -> ((Animal)r).animalNumber(),
                        val -> val == null ? "" : val.toString(), true, (row, newValue) -> {
                            AnimalUpdateService.updateAnimalNumber(row, newValue, this::loadAnimalData);
                        }),
                new MetricColumn("Responder", MetricOption.MetricType.STRING, new Boolean[] { true, true, true, true },
                        r -> ((Animal)r).responder(), val -> val == null ? "" : val.toString(), false, null),
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
    }

    private InspectorOptions getAnimalInspectorOptions() {
        InspectorOptions animalInspectorOptions = new InspectorOptions();

        // Status field: Visible always
        animalInspectorOptions.addField(new InspectorOptions.Field("Status", row -> {
            if (!(row instanceof Animal a))
                return "";
            return a.status().name();
        }, true, obj -> true));

        animalInspectorOptions.addField(new InspectorOptions.Field("Stopped Reason",
                row -> row instanceof Animal a ? String.valueOf(a.stoppedReason()) : "", true,
                row -> row instanceof Animal a && a.status() == Animal.Status.STOPPED));
        animalInspectorOptions.addField(new InspectorOptions.Field("Stopped At",
                row -> row instanceof Animal a && a.stoppedAt() != null ? a.stoppedAt().toString() : "", true,
                row -> row instanceof Animal a && a.status() == Animal.Status.STOPPED));

        animalInspectorOptions.addButton(new InspectorOptions.Button("Discontinue Animal",
                row -> row instanceof Animal a && a.status() == Animal.Status.ACTIVE, row -> {
                    if (!(row instanceof Animal animal))
                        return;
                    Platform.runLater(() -> {
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setTitle("Discontinue Animal");
                        dialog.setHeaderText("Please enter a reason for discontinuation (optional):");
                        dialog.setContentText("Reason:");

                        Optional<String> result = dialog.showAndWait();
                        if (result.isPresent()) {
                            String reason = result.get();
                            Animal updated = new Animal(animal.id(), animal.animalNumber(), animal.responder(),
                                    animal.groupId(), animal.location(), Animal.Status.STOPPED, reason,
                                    LocalDateTime.now(), animal.fcr(), animal.startWeightKg(), animal.totalFeedKg(),
                                    animal.weightGainKg(), animal.latestWeightKg(), animal.completedDays(),
                                    animal.startDay(), animal.createdAt());
                            try {
                                AnimalDAO.update(updated);
                                loadAnimalData();
                                clearInspector();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }));

        animalInspectorOptions.addButton(new InspectorOptions.Button("Reactivate Animal",
                row -> row instanceof Animal a && a.status() == Animal.Status.STOPPED, row -> {
                    if (!(row instanceof Animal animal))
                        return;
                    Animal updated = new Animal(animal.id(), animal.animalNumber(), animal.responder(),
                            animal.groupId(), animal.location(), Animal.Status.ACTIVE, null, null, animal.fcr(),
                            animal.startWeightKg(), animal.totalFeedKg(), animal.weightGainKg(),
                            animal.latestWeightKg(), animal.completedDays(), animal.startDay(), animal.createdAt());
                    try {
                        AnimalDAO.update(updated);
                        loadAnimalData();
                        clearInspector();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }));

        return animalInspectorOptions;
    }

    public void loadAnimalData() {
        List<Animal> allAnimals;
        try {
            allAnimals = pigtracker.dao.AnimalDAO.getAll();
        } catch (Exception e) {
            allAnimals = List.of();
        }
        boolean showDiscontinued = AppContext.getMainController() != null
                && AppContext.getMainController().isShowDiscontinuedAnimals();

        List<Animal> filteredList = allAnimals.stream().filter(animal -> showDiscontinued || animal.isActive())
                .toList();

        animalDataController.setData(new ArrayList<>(filteredList));
    }

    private void loadVisitMetrics() {
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        visitColumns = List.of(
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
    }

    public void loadVisitData() {
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

    public void populateInspector(Object selectedRow, List<MetricColumn> columns, InspectorOptions options)
            throws IOException {
        inspectorFieldContainer.getChildren().clear();

        Map<String, String> originalValues = new HashMap<>();
        Map<String, TextField> editors = new HashMap<>();
        List<MetricColumn> editableColumns = columns.stream().filter(MetricColumn::isEditable).toList();

        for (MetricColumn column : columns) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/inspector-field.fxml"));
            HBox fieldRow = loader.load();
            InspectorFieldController fieldController = loader.getController();
            String originalValue = column.getFormatter().apply(column.getExtractor().apply(selectedRow));
            fieldController.getValueLabel().setText(column.getLabel());
            fieldController.getValueField().setText(originalValue);
            fieldController.getValueField().setEditable(column.isEditable());
            String fieldKey = column.getLabel();
            originalValues.put(fieldKey, originalValue);
            editors.put(fieldKey, fieldController.getValueField());

            if (!column.isEditable()) {
                fieldController.getValueField().setStyle("-fx-background-color: #F4F4F4;");
            } else {
                fieldController.getValueField().textProperty().addListener((obs, oldVal, newVal) -> {
                    boolean valid = validateField(newVal, column.getType());
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

        if (options != null) {
            for (var extraField : options.getExtraFields()) {
                if (extraField.visibleIf != null && !extraField.visibleIf.test(selectedRow))
                    continue;
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/inspector-field.fxml"));
                HBox fieldRow = loader.load();
                InspectorFieldController fieldController = loader.getController();
                fieldController.getValueLabel().setText(extraField.label);
                fieldController.getValueField().setText(extraField.valueFunction.apply(selectedRow));
                fieldController.getValueField().setEditable(false);
                fieldController.getValueField().setStyle("-fx-background-color: #F4F4F4;");
                inspectorFieldContainer.getChildren().add(fieldRow);
            }

            for (var button : options.getExtraButtons()) {
                if (button.visibleIf != null && !button.visibleIf.test(selectedRow))
                    continue;
                Button btn = new Button(button.label);
                btn.setOnAction(evt -> button.onClick.accept(selectedRow));
                inspectorFieldContainer.getChildren().add(btn);
            }
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
