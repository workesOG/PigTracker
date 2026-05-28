// Theis Thomsen

package pigtracker.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pigtracker.controller.components.FilterChipContainerController;
import pigtracker.controller.components.FilterChipController;
import pigtracker.controller.components.FilterChipController.Operator;
import pigtracker.controller.components.SegmentedToggleController;
import pigtracker.model.Animal;
import pigtracker.model.InspectorOptions;
import pigtracker.model.MetricColumn;
import pigtracker.model.MetricOption;
import pigtracker.model.Visit;
import pigtracker.util.Alerts;
import pigtracker.util.AppContext;

public class SpecificDataController {

    @FXML private TextField searchTextField;
    @FXML private ChoiceBox<MetricOption> sortDropdown;
    @FXML private SegmentedToggleController sortSegmentedToggleController;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private FilterChipContainerController filterChipContainerController;
    @FXML private TableView<Object> tableView;

    private List<MetricOption> availableMetrics;
    private List<Object> fullData;
    private final ObservableList<Object> filteredData = FXCollections.observableArrayList();

    private Map<MetricOption, Function<Object, Object>> metricExtractors;
    private Map<MetricOption, Function<Object, String>> metricFormatters = Map.of();

    private List<MetricColumn> columns;
    private InspectorOptions inspectorOptions;

    @FXML
    public void initialize() {
        sortSegmentedToggleController.setOptions("Asc.", "Desc.");
        setListeners();
    }

    public void setData(List<Object> data) {
        this.fullData = data;
        applyAllFiltersAndUpdate();
    }

    public void enableDateFilters(boolean enabled) {
        dateFrom.setVisible(enabled);
        dateFrom.setManaged(enabled);
        dateTo.setVisible(enabled);
        dateTo.setManaged(enabled);
    }

    public TableView<Object> getTableView() {
        return tableView;
    }

    public void setMetrics(List<MetricOption> metrics) {
        availableMetrics = metrics;

        sortDropdown.getItems().clear();
        sortDropdown.getItems().add(null);
        sortDropdown.getItems().addAll(metrics);

        filterChipContainerController.setMetrics(metrics);
        tableView.getColumns().clear();

        for (MetricOption metric : metrics) {
            tableView.getColumns().add(buildTableColumn(metric));
        }
    }

    public void setColumns(List<MetricColumn> columns) {
        this.columns = columns;
    }

    public void setInspectorOptions(InspectorOptions inspectorOptions) {
        this.inspectorOptions = inspectorOptions;
    }

    public void setMetricExtractors(Map<MetricOption, Function<Object, Object>> extractors) {
        this.metricExtractors = extractors;
    }

    public void setMetricFormatters(Map<MetricOption, Function<Object, String>> formatters) {
        this.metricFormatters = formatters;
    }

    @FXML
    private void applyAllFiltersAndUpdate() {
        String search = searchTextField.getText();
        MetricOption sortMetric = sortDropdown.getValue();
        boolean ascending = "Asc.".equals(sortSegmentedToggleController.getSelected());
        List<FilterChipController> chips = filterChipContainerController.getFilters();
        LocalDate from = dateFrom.isDisabled() ? null : dateFrom.getValue();
        LocalDate to = dateTo.isDisabled() ? null : dateTo.getValue();

        Predicate<Object> filterPred = row -> {
            if (search != null && !search.isEmpty() && !rowMatchesSearch(row, search))
                return false;
            if ((from != null || to != null) && !rowMatchesDateInterval(row, from, to))
                return false;
            for (FilterChipController chip : chips) {
                if (!chipMatchesRow(row, chip))
                    return false;
            }
            return true;
        };

        List<Object> filtered = fullData.stream().filter(filterPred).collect(Collectors.toList());
        if (sortMetric != null) {
            filtered.sort(getComparatorForMetric(sortMetric, ascending));
        }
        filteredData.setAll(filtered);
        tableView.setItems(filteredData);
    }

    private TableColumn<Object, String> buildTableColumn(MetricOption metric) {
        TableColumn<Object, String> col = new TableColumn<>(metric.getLabel());
        col.setSortable(false);
        col.setCellValueFactory(cellData -> {
            Object value = extractMetricValue(cellData.getValue(), metric);
            return new SimpleStringProperty(value == null ? "" : formatValue(metric, value));
        });
        return col;
    }

    private boolean rowMatchesSearch(Object row, String query) {
        String lcQuery = query.toLowerCase();

        if (row instanceof Animal animal) {
            return contains(animal.id(), lcQuery)
                    || contains(animal.animalNumber(), lcQuery)
                    || containsString(animal.responder(), lcQuery)
                    || contains(animal.groupId(), lcQuery)
                    || contains(animal.location(), lcQuery)
                    || (animal.status() != null && animal.status().name().toLowerCase().contains(lcQuery))
                    || containsString(animal.stoppedReason(), lcQuery);
        }
        if (row instanceof Visit visit) {
            return contains(visit.id(), lcQuery)
                    || contains(visit.animalNumber(), lcQuery)
                    || containsString(visit.responder(), lcQuery)
                    || contains(visit.reportId(), lcQuery)
                    || contains(visit.location(), lcQuery)
                    || (visit.visitTime() != null && visit.visitTime().toString().toLowerCase().contains(lcQuery));
        }
        return false;
    }

    private static boolean contains(Object value, String lcQuery) {
        return String.valueOf(value).toLowerCase().contains(lcQuery);
    }

    private static boolean containsString(String value, String lcQuery) {
        return value != null && value.toLowerCase().contains(lcQuery);
    }

    private boolean rowMatchesDateInterval(Object row, LocalDate from, LocalDate to) {
        LocalDate rowDate;
        if (row instanceof Visit visit) {
            rowDate = visit.visitTime().toLocalDate();
        } else if (row instanceof Animal animal) {
            rowDate = animal.startDay();
            if (rowDate == null)
                return false;
        } else {
            return true;
        }
        if (from != null && rowDate.isBefore(from))
            return false;
        if (to != null && rowDate.isAfter(to))
            return false;
        return true;
    }

    private boolean chipMatchesRow(Object row, FilterChipController chip) {
        MetricOption metricOption = findMetricByLabel(chip.getValue());
        Object value = extractMetricValue(row, metricOption);
        if (!(value instanceof Number numericValue))
            return false;

        try {
            double val = numericValue.doubleValue();
            double cmp1 = Double.parseDouble(chip.getNum1());
            Operator op = chip.getOperator();
            return switch (op) {
                case LESS_THAN -> val < cmp1;
                case GREATER_THAN -> val > cmp1;
                case EQUALS -> val == cmp1;
                case RANGE -> {
                    if (chip.getNum2() == null)
                        yield false;
                    double cmp2 = Double.parseDouble(chip.getNum2());
                    yield val >= cmp1 && val <= cmp2;
                }
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private MetricOption findMetricByLabel(String label) {
        if (label == null)
            return null;
        for (MetricOption m : availableMetrics) {
            if (label.equals(m.getLabel()))
                return m;
        }
        return null;
    }

    private Object extractMetricValue(Object row, MetricOption metric) {
        if (metricExtractors == null || metric == null)
            return null;
        Function<Object, Object> extractor = metricExtractors.get(metric);
        return extractor == null ? null : extractor.apply(row);
    }

    @SuppressWarnings("rawtypes")
    private Comparator<Object> getComparatorForMetric(MetricOption metric, boolean asc) {
        Comparator<Object> comparator = Comparator.comparing(
                row -> (Comparable) extractMetricValue(row, metric),
                Comparator.nullsLast(Comparator.naturalOrder()));
        return asc ? comparator : comparator.reversed();
    }

    private String formatValue(MetricOption metric, Object value) {
        if (metricFormatters != null && metricFormatters.containsKey(metric)) {
            return metricFormatters.get(metric).apply(value);
        }
        return value == null ? "" : value.toString();
    }

    private void setListeners() {
        sortSegmentedToggleController.addSelectionChangeListener((obs, oldVal, newVal) -> applyAllFiltersAndUpdate());
        sortDropdown.valueProperty().addListener((obs, oldVal, newVal) -> applyAllFiltersAndUpdate());
        dateFrom.valueProperty().addListener((obs, oldVal, newVal) -> applyAllFiltersAndUpdate());
        dateTo.valueProperty().addListener((obs, oldVal, newVal) -> applyAllFiltersAndUpdate());
        filterChipContainerController.setOnFiltersChanged(this::applyAllFiltersAndUpdate);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null)
                return;
            try {
                AppContext.getDataController().populateInspector(newVal, columns, inspectorOptions);
            } catch (IOException e) {
                e.printStackTrace();
                Alerts.error("Inspector Load Error", "Inspector FXML failed to load", e.getMessage());
            }
        });
    }
}
