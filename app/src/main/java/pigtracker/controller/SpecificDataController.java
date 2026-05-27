// Theis Thomsen

package pigtracker.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import pigtracker.controller.components.FilterChipContainerController;
import pigtracker.controller.components.FilterChipController;
import pigtracker.controller.components.SegmentedToggleController;
import pigtracker.model.Animal;
import pigtracker.model.MetricColumn;
import pigtracker.model.MetricOption;
import pigtracker.model.Visit;
import pigtracker.util.AppContext;

public class SpecificDataController {
    @FXML
    private TextField searchTextField;

    @FXML
    private Button searchButton;

    @FXML
    private ChoiceBox<MetricOption> sortDropdown;

    @FXML
    private SegmentedToggleController sortSegmentedToggleController;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private FilterChipContainerController filterChipContainerController;

    @FXML
    private TableView<Object> tableView;

    private ArrayList<MetricOption> availableMetrics;
    private List<Object> fullData;
    private ObservableList<Object> filteredData = FXCollections.observableArrayList();

    private Map<MetricOption, Function<Object, Object>> metricExtractors;
    private Map<MetricOption, Function<Object, String>> metricFormatters = Map.of();

    private List<MetricColumn> columns;

    public void setMetricExtractors(Map<MetricOption, Function<Object, Object>> extractors) {
        this.metricExtractors = extractors;
    }

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

    @FXML
    private void applyAllFiltersAndUpdate() {
        String search = searchTextField.getText();
        MetricOption sortMetric = sortDropdown.getValue();
        boolean ascending = sortSegmentedToggleController.getSelected() == "Asc.";
        List<FilterChipController> chips = filterChipContainerController.getFilters();
        LocalDate from = dateFrom.isDisabled() ? null : dateFrom.getValue();
        LocalDate to = dateTo.isDisabled() ? null : dateTo.getValue();

        Predicate<Object> filterPred = row -> {
            if (search != null && !search.isEmpty() && !rowMatchesSearch(row, search))
                return false;
            if (from != null || to != null) {
                if (!rowMatchesDateInterval(row, from, to))
                    return false;
            }
            for (FilterChipController chip : chips) {
                if (!chipMatchesRow(row, chip))
                    return false;
            }
            return true;
        };

        var filtered = fullData.stream().filter(filterPred).collect(Collectors.toList());
        if (sortMetric != null) {
            Comparator<Object> cmp = getComparatorForMetric(sortMetric, ascending);
            filtered.sort(cmp);
        }
        filteredData.setAll(filtered);
        tableView.setItems(filteredData);
    }

    public void setMetrics(ArrayList<MetricOption> metrics) {
        availableMetrics = metrics;

        sortDropdown.getItems().clear();
        sortDropdown.getItems().add(null);
        sortDropdown.getItems().addAll(metrics);

        filterChipContainerController.setMetrics(metrics);

        tableView.getColumns().clear();

        for (MetricOption metric : metrics) {
            TableColumn<Object, String> col = new TableColumn<>(metric.getLabel());
            col.setSortable(false);
            col.setCellValueFactory(cellData -> {
                Object row = cellData.getValue();
                Object value = extractMetricValue(row, metric);
                String displayString = value == null ? "" : formatValue(metric, value);
                return new javafx.beans.property.SimpleStringProperty(displayString);
            });
            tableView.getColumns().add(col);
        }
    }

    public void setColumns(List<MetricColumn> columns) {
        this.columns = columns;
    }

    public void setMetricFormatters(Map<MetricOption, Function<Object, String>> formatters) {
        this.metricFormatters = formatters;
    }

    private boolean rowMatchesSearch(Object row, String query) {
        String lcQuery = query.toLowerCase();

        if (row instanceof Animal animal) {
            return (String.valueOf(animal.id()).toLowerCase().contains(lcQuery)
                    || String.valueOf(animal.animalNumber()).toLowerCase().contains(lcQuery)
                    || (animal.responder() != null && animal.responder().toLowerCase().contains(lcQuery))
                    || String.valueOf(animal.groupId()).toLowerCase().contains(lcQuery)
                    || String.valueOf(animal.location()).toLowerCase().contains(lcQuery)
                    || (animal.status() != null && animal.status().name().toLowerCase().contains(lcQuery))
                    || (animal.stoppedReason() != null && animal.stoppedReason().toLowerCase().contains(lcQuery)));
        }
        if (row instanceof Visit visit) {
            return (String.valueOf(visit.id()).toLowerCase().contains(lcQuery)
                    || String.valueOf(visit.animalNumber()).toLowerCase().contains(lcQuery)
                    || (visit.responder() != null && visit.responder().toLowerCase().contains(lcQuery))
                    || String.valueOf(visit.reportId()).toLowerCase().contains(lcQuery)
                    || String.valueOf(visit.location()).toLowerCase().contains(lcQuery)
                    || (visit.visitTime() != null && visit.visitTime().toString().toLowerCase().contains(lcQuery)));
        }
        return false;
    }

    private boolean rowMatchesDateInterval(Object row, LocalDate from, LocalDate to) {
        if (row instanceof Visit visit) {
            LocalDate rowDate = visit.visitTime().toLocalDate();
            if (from != null && rowDate.isBefore(from))
                return false;
            if (to != null && rowDate.isAfter(to))
                return false;
            return true;
        } else if (row instanceof Animal animal) {
            LocalDate rowDate = animal.startDay();
            if (rowDate == null)
                return false;
            if (from != null && rowDate.isBefore(from))
                return false;
            if (to != null && rowDate.isAfter(to))
                return false;
            return true;
        }
        return true;
    }

    private boolean chipMatchesRow(Object row, FilterChipController chip) {
        String metricLabel = chip.getValue();
        FilterChipController.Operator op = chip.getOperator();
        String num1 = chip.getNum1();
        String num2 = chip.getNum2();

        MetricOption metricOption = findMetricByLabel(metricLabel);
        Object value = extractMetricValue(row, metricOption);
        if (value == null)
            return false;

        try {
            if (value instanceof Number) {
                double val = ((Number)value).doubleValue();
                double cmp1 = Double.parseDouble(num1);
                switch (op) {
                case LESS_THAN:
                    return val < cmp1;
                case GREATER_THAN:
                    return val > cmp1;
                case EQUALS:
                    return val == cmp1;
                case RANGE:
                    if (num2 == null)
                        return false;
                    double cmp2 = Double.parseDouble(num2);
                    return val >= cmp1 && val <= cmp2;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
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
        if (extractor == null)
            return null;
        return extractor.apply(row);
    }

    private Comparator<Object> getComparatorForMetric(MetricOption metric, boolean asc) {
        Comparator<Object> comparator = Comparator.comparing(row -> {
            Object value = extractMetricValue(row, metric);
            return (Comparable)value;
        }, Comparator.nullsLast(Comparator.naturalOrder()));
        if (!asc)
            return comparator.reversed();
        return comparator;
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

        filterChipContainerController.setOnFiltersChanged(() -> applyAllFiltersAndUpdate());

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    AppContext.getDataController().populateInspector(newVal, columns);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Inspector Load Error");
                    alert.setHeaderText("Inspector FXML failed to load");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }
}
