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
import java.util.function.BiConsumer;
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
import pigtracker.model.MetricOption.MetricType;
import pigtracker.model.Visit;
import pigtracker.service.AnimalUpdateService;
import pigtracker.util.AppContext;

public class DataController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Boolean[] ALL_OPS = { true, true, true, true };
    private static final Boolean[] EQUALS_ONLY = { true, false, false, false };
    private static final String READ_ONLY_FIELD_STYLE = "-fx-background-color: #F4F4F4;";
    private static final String INVALID_FIELD_STYLE = "invalid-field";

    @FXML private SpecificDataController animalDataController;
    @FXML private SpecificDataController visitDataController;
    @FXML private TabPane tabPane;
    @FXML private Tab animalTab;
    @FXML private Tab visitTab;
    @FXML private VBox inspectorFieldContainer;
    @FXML private Button inspectorResetButton;
    @FXML private Button inspectorUpdateButton;

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
            if (!col.editable())
                continue;

            TextField tf = inspectorEditors.get(col.label());
            if (tf == null)
                continue;

            String editedValue = tf.getText();
            String originalValue = originalValues.get(col.label());
            if (Objects.equals(editedValue, originalValue))
                continue;

            Object parsedValue = parseFieldValue(col.type(), editedValue);
            if (parsedValue == null)
                continue;

            BiConsumer<Object, Object> updateInstruction = col.updateInstruction();
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
            TextField tf = inspectorEditors.get(col.label());
            String original = originalValues.get(col.label());
            if (tf != null && original != null) {
                tf.setText(original);
            }
        }
        updateButtonStates();
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

    public void loadAnimalData() {
        List<Animal> allAnimals;
        try {
            allAnimals = AnimalDAO.getAll();
        } catch (Exception e) {
            allAnimals = List.of();
        }

        boolean showDiscontinued = AppContext.getMainController() != null
                && AppContext.getMainController().isShowDiscontinuedAnimals();
        List<Animal> filtered = allAnimals.stream()
                .filter(animal -> showDiscontinued || animal.isActive())
                .toList();

        animalDataController.setData(new ArrayList<>(filtered));
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

    public void populateInspector(Object selectedRow, List<MetricColumn> columns, InspectorOptions options)
            throws IOException {
        inspectorFieldContainer.getChildren().clear();

        Map<String, String> originals = new HashMap<>();
        Map<String, TextField> editors = new HashMap<>();
        List<MetricColumn> editableColumns = columns.stream().filter(MetricColumn::editable).toList();

        for (MetricColumn column : columns) {
            HBox fieldRow = renderInspectorField(selectedRow, column, originals, editors);
            inspectorFieldContainer.getChildren().add(fieldRow);
        }

        if (options != null) {
            for (InspectorOptions.Field extraField : options.getExtraFields()) {
                if (extraField.visibleIf() != null && !extraField.visibleIf().test(selectedRow))
                    continue;
                inspectorFieldContainer.getChildren().add(renderExtraField(selectedRow, extraField));
            }

            for (InspectorOptions.Button button : options.getExtraButtons()) {
                if (button.visibleIf() != null && !button.visibleIf().test(selectedRow))
                    continue;
                Button btn = new Button(button.label());
                btn.setOnAction(evt -> button.onClick().accept(selectedRow));
                inspectorFieldContainer.getChildren().add(btn);
            }
        }

        this.originalValues = originals;
        this.inspectorRow = selectedRow;
        this.inspectorColumns = editableColumns;
        this.inspectorEditors = editors;
        updateButtonStates();
    }

    private HBox renderInspectorField(Object selectedRow, MetricColumn column,
            Map<String, String> originals, Map<String, TextField> editors) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/inspector-field.fxml"));
        HBox fieldRow = loader.load();
        InspectorFieldController fieldController = loader.getController();

        String originalValue = column.formatter().apply(column.extractor().apply(selectedRow));
        fieldController.getValueLabel().setText(column.label());
        fieldController.getValueField().setText(originalValue);
        fieldController.getValueField().setEditable(column.editable());

        originals.put(column.label(), originalValue);
        editors.put(column.label(), fieldController.getValueField());

        if (!column.editable()) {
            fieldController.getValueField().setStyle(READ_ONLY_FIELD_STYLE);
        } else {
            fieldController.getValueField().textProperty().addListener((obs, oldVal, newVal) -> {
                boolean valid = validateField(newVal, column.type());
                if (!valid) {
                    fieldController.getValueField().getStyleClass().add(INVALID_FIELD_STYLE);
                } else {
                    fieldController.getValueField().getStyleClass()
                            .removeAll(Collections.singleton(INVALID_FIELD_STYLE));
                }
                updateButtonStates();
            });
        }
        return fieldRow;
    }

    private HBox renderExtraField(Object selectedRow, InspectorOptions.Field extraField) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/inspector-field.fxml"));
        HBox fieldRow = loader.load();
        InspectorFieldController fieldController = loader.getController();
        fieldController.getValueLabel().setText(extraField.label());
        fieldController.getValueField().setText(extraField.valueFunction().apply(selectedRow));
        fieldController.getValueField().setEditable(false);
        fieldController.getValueField().setStyle(READ_ONLY_FIELD_STYLE);
        return fieldRow;
    }

    private void setupMetrics(SpecificDataController controller, List<MetricColumn> columns, InspectorOptions options) {
        List<MetricOption> metricOptions = new ArrayList<>();
        Map<MetricOption, Function<Object, Object>> extractors = new HashMap<>();
        Map<MetricOption, Function<Object, String>> formatters = new HashMap<>();

        for (MetricColumn column : columns) {
            MetricOption opt = new MetricOption(column.label(), column.type(), column.visibility());
            metricOptions.add(opt);
            extractors.put(opt, column.extractor());
            if (column.formatter() != null)
                formatters.put(opt, column.formatter());
        }

        controller.setMetrics(metricOptions);
        controller.setMetricExtractors(extractors);
        controller.setMetricFormatters(formatters);
        controller.enableDateFilters(true);
        controller.setColumns(columns);
        controller.setInspectorOptions(options);
    }

    private void updateButtonStates() {
        boolean anyDirty = false;
        boolean allValid = true;
        for (MetricColumn col : inspectorColumns) {
            TextField tf = inspectorEditors.get(col.label());
            String current = tf.getText();
            String original = originalValues.get(col.label());
            boolean valid = validateField(current, col.type());
            if (!valid)
                allValid = false;
            if (!Objects.equals(current, original) && valid)
                anyDirty = true;
        }
        inspectorResetButton.setDisable(!anyDirty && allValid);
        inspectorUpdateButton.setDisable(!(anyDirty && allValid));
    }

    private boolean validateField(String value, MetricType type) {
        if (value == null || value.isBlank())
            return false;
        return switch (type) {
            case INTEGER -> value.matches("-?\\d+");
            case DECIMAL -> value.matches("-?\\d+(\\.\\d+)?");
            case DATE -> parseDate(value) != null;
            default -> true;
        };
    }

    private static Object parseFieldValue(MetricType type, String value) {
        try {
            return switch (type) {
                case INTEGER -> Integer.parseInt(value);
                case DECIMAL -> Double.parseDouble(value);
                case DATE -> LocalDate.parse(value, DATE_FMT);
                default -> value;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FMT);
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

    private void loadAnimalMetrics() {
        animalColumns = List.of(
                animalCol("ID", MetricType.INTEGER, Animal::id, asString(), false, null),
                animalCol("Animal Number", MetricType.INTEGER, Animal::animalNumber, asString(), true,
                        (row, newValue) -> AnimalUpdateService.updateAnimalNumber(row, newValue, this::loadAnimalData)),
                animalCol("Responder", MetricType.STRING, Animal::responder, asString(), false, null),
                animalCol("Group ID", MetricType.INTEGER, Animal::groupId, asString(), false, null),
                animalCol("FCR", MetricType.DECIMAL, Animal::fcr, formatDecimal(), false, null),
                animalCol("Start Weight", MetricType.DECIMAL, Animal::startWeightKg, formatDecimal(), false, null),
                animalCol("Weight", MetricType.DECIMAL, Animal::latestWeightKg, formatDecimal(), false, null),
                animalCol("Weight gain", MetricType.DECIMAL, Animal::weightGainKg, formatDecimal(), false, null),
                animalCol("Feed intake", MetricType.DECIMAL, Animal::totalFeedKg, formatDecimal(), false, null),
                animalCol("Days tested", MetricType.INTEGER, Animal::completedDays, asString(), false, null),
                animalCol("Start day", MetricType.DATE, Animal::startDay, formatDate(DATE_FMT), false, null),
                animalCol("Location", MetricType.INTEGER, Animal::location, asString(), false, null));
    }

    private void loadVisitMetrics() {
        visitColumns = List.of(
                visitCol("ID", MetricType.INTEGER, ALL_OPS, Visit::id, asString()),
                visitCol("Animal Number", MetricType.INTEGER, ALL_OPS, Visit::animalNumber, asString()),
                visitCol("Responder", MetricType.STRING, EQUALS_ONLY, Visit::responder,
                        val -> val == null ? "<none>" : val.toString()),
                visitCol("Report ID", MetricType.INTEGER, ALL_OPS, Visit::reportId, asString()),
                visitCol("Location", MetricType.INTEGER, ALL_OPS, Visit::location, asString()),
                visitCol("Visit Time", MetricType.DATE, ALL_OPS, Visit::visitTime, formatDate(DATE_TIME_FMT)),
                visitCol("Duration (sec)", MetricType.INTEGER, ALL_OPS, Visit::durationSec, asString()),
                visitCol("Weight (g)", MetricType.INTEGER, ALL_OPS, Visit::weightG, asString()),
                visitCol("Feed Intake (g)", MetricType.INTEGER, ALL_OPS, Visit::feedIntakeG, asString()));
    }

    private static MetricColumn animalCol(String label, MetricType type, Function<Animal, Object> extract,
            Function<Object, String> format, boolean editable, BiConsumer<Object, Object> updateInstruction) {
        return new MetricColumn(label, type, ALL_OPS,
                row -> extract.apply((Animal) row), format, editable, updateInstruction);
    }

    private static MetricColumn visitCol(String label, MetricType type, Boolean[] visibility,
            Function<Visit, Object> extract, Function<Object, String> format) {
        return new MetricColumn(label, type, visibility, row -> extract.apply((Visit) row), format, false, null);
    }

    private static Function<Object, String> asString() {
        return val -> val == null ? "" : val.toString();
    }

    private static Function<Object, String> formatDecimal() {
        return val -> val == null ? "" : String.format("%.2f", val);
    }

    private static Function<Object, String> formatDate(DateTimeFormatter formatter) {
        return val -> switch (val) {
            case null -> "";
            case LocalDate date -> date.format(formatter);
            case LocalDateTime dateTime -> dateTime.format(formatter);
            default -> val.toString();
        };
    }

    private InspectorOptions getAnimalInspectorOptions() {
        InspectorOptions options = new InspectorOptions();

        options.addField(new InspectorOptions.Field("Status",
                row -> row instanceof Animal a ? a.status().name() : "", true, obj -> true));
        options.addField(new InspectorOptions.Field("Stopped Reason",
                row -> row instanceof Animal a ? String.valueOf(a.stoppedReason()) : "", true,
                row -> row instanceof Animal a && a.status() == Animal.Status.STOPPED));
        options.addField(new InspectorOptions.Field("Stopped At",
                row -> row instanceof Animal a && a.stoppedAt() != null ? a.stoppedAt().toString() : "", true,
                row -> row instanceof Animal a && a.status() == Animal.Status.STOPPED));

        options.addButton(new InspectorOptions.Button("Discontinue Animal",
                row -> row instanceof Animal a && a.status() == Animal.Status.ACTIVE,
                row -> discontinueAnimal((Animal) row)));
        options.addButton(new InspectorOptions.Button("Reactivate Animal",
                row -> row instanceof Animal a && a.status() == Animal.Status.STOPPED,
                row -> reactivateAnimal((Animal) row)));

        return options;
    }

    private void discontinueAnimal(Animal animal) {
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Discontinue Animal");
            dialog.setHeaderText("Please enter a reason for discontinuation (optional):");
            dialog.setContentText("Reason:");

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty())
                return;

            Animal updated = withStatus(animal, Animal.Status.STOPPED, result.get(), LocalDateTime.now());
            persistAnimalUpdate(updated);
        });
    }

    private void reactivateAnimal(Animal animal) {
        Animal updated = withStatus(animal, Animal.Status.ACTIVE, null, null);
        persistAnimalUpdate(updated);
    }

    private void persistAnimalUpdate(Animal updated) {
        try {
            AnimalDAO.update(updated);
            loadAnimalData();
            clearInspector();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Animal withStatus(Animal a, Animal.Status status, String stoppedReason, LocalDateTime stoppedAt) {
        return new Animal(a.id(), a.animalNumber(), a.responder(), a.groupId(), a.location(), status,
                stoppedReason, stoppedAt, a.fcr(), a.startWeightKg(), a.totalFeedKg(), a.weightGainKg(),
                a.latestWeightKg(), a.completedDays(), a.startDay(), a.createdAt());
    }
}
