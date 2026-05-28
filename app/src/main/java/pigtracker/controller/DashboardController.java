// Af Theis Thomsen

package pigtracker.controller;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import pigtracker.Main;
import pigtracker.controller.components.KpiController;
import pigtracker.controller.components.SegmentedToggleController;
import pigtracker.dao.GroupDAO;
import pigtracker.model.DashboardMetrics;
import pigtracker.model.Group;
import pigtracker.model.HistoricalImportComparisonMetrics;
import pigtracker.model.KpiMetrics;
import pigtracker.model.PopulationDistributionGraphMetrics;
import pigtracker.service.DashboardCreationService;
import pigtracker.service.GroupHandlingService;
import pigtracker.util.Alerts;
import pigtracker.util.AppContext;
import pigtracker.util.DateFormattingUtil;

public class DashboardController {

    @FXML private Label activeGroupLabel;
    @FXML private Button setGroupButton;

    @FXML private KpiController fcrCardController;
    @FXML private KpiController weightCardController;
    @FXML private KpiController feedCardController;
    @FXML private KpiController populationCardController;

    @FXML private SegmentedToggleController pdgMetricSegmentedToggleController;
    @FXML private SegmentedToggleController hicMetricSegmentedToggleController;
    @FXML private SegmentedToggleController hicPeriodSegmentedToggleController;

    @FXML private BarChart<String, Number> populationDistributionGraph;
    @FXML private BarChart<String, Number> historicalImportComparisonGraph;

    private Integer activeGroupId;

    @FXML
    public void initialize() throws Exception {
        AppContext.setDashboardController(this);
        setupPopulationDistributionGraphSection();
        setupHistoricalImportGraphSection();
        setGroup(GroupHandlingService.getFirstGroup());
        updateGroupCreationButton();
        Main.onDashboardReady();
    }

    public void setGroup(Group group) {
        if (group == null) {
            activeGroupId = null;
            activeGroupLabel.setText("No groups found in DB");
            return;
        }
        activeGroupId = group.id();
        activeGroupLabel.setText(String.format("#%s", group.name()));
    }

    public void updateGroupCreationButton() throws SQLException {
        setGroupButton.setDisable(!GroupHandlingService.doesDatabaseHoldAtLeastTwoGroups());
    }

    @FXML
    private void handleChangeGroup() {
        try {
            List<Group> groups = GroupDAO.getAll();
            if (groups.isEmpty()) {
                Alerts.info("No groups available", "There are no groups to select.");
                return;
            }

            Map<String, Group> nameToGroup = groups.stream().collect(Collectors.toMap(Group::name, g -> g));
            ObservableList<String> groupNames = FXCollections.observableArrayList(nameToGroup.keySet());
            String chosenGroupName = MenuSelectionWindowController.showAndWait(Main.getPrimaryStage(), "Select Group",
                    groupNames);

            if (chosenGroupName != null) {
                setGroup(nameToGroup.get(chosenGroupName));
                setDashboardMetrics();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alerts.error("Error", "Could not fetch groups.", e.getMessage());
        }
    }

    public void setAndUpdateDashboard() throws SQLException {
        updateGroupCreationButton();
        if (activeGroupId == null) {
            setGroup(GroupHandlingService.getFirstGroup());
        }
        setDashboardMetrics();
    }

    public void setDashboardMetrics() throws SQLException {
        DashboardMetrics dashboardMetrics = DashboardCreationService.calculateDashboardMetrics(activeGroupId);

        String pdgMetric = getPdgMetric();
        PopulationDistributionGraphMetrics pdgMetrics = DashboardCreationService
                .calculatePopulationDistributionMetrics(activeGroupId, pdgMetric);

        String hicMetric = getHicMetric();
        String hicPeriod = getHicPeriod();
        HistoricalImportComparisonMetrics hicMetrics = DashboardCreationService
                .calculateHistoricalImportComparisonMetrics(activeGroupId, hicMetric, hicPeriod);

        if (dashboardMetrics == null) {
            setEmpty();
            return;
        }
        setKpis(dashboardMetrics);
        setPopulationDistributionGraph(pdgMetrics, pdgMetric);
        setHistoricalImportComparisonGraph(hicMetrics);
    }

    public void setEmpty() {
        resetKpi(fcrCardController, "Feed Conversion Rate (FCR)", "kg feed / kg gain", 1, "Mean");
        resetKpi(weightCardController, "Animal Weight", "kg gain / day", 2, "Mean");
        resetKpi(feedCardController, "Feed Consumption", "g feed / day", 0, "Mean");
        resetKpi(populationCardController, "Animal Population", "pigs", 0, "Total");
    }

    private static void resetKpi(KpiController card, String title, String unit, int decimals, String description) {
        card.setTitle(title);
        card.setValue(0, decimals);
        card.setUnit(unit);
        card.setTrend(0);
        card.setDescription(description);
    }

    private void setKpis(DashboardMetrics metrics) {
        setKpiCardFromMetrics(fcrCardController, metrics.fcr());
        setKpiCardFromMetrics(weightCardController, metrics.weight());
        setKpiCardFromMetrics(feedCardController, metrics.feed());
        setKpiCardFromMetrics(populationCardController, metrics.population());
    }

    private void setKpiCardFromMetrics(KpiController controller, KpiMetrics kpi) {
        if (kpi == null) {
            controller.setValue(0, 0);
            controller.setTrend(0);
            return;
        }
        controller.setTitle(kpi.title());
        controller.setValue(kpi.value(), kpi.decimals());
        controller.setUnit(kpi.unit());
        controller.setTrend(kpi.trend());
        controller.setDescription(kpi.description());
        setKpiTrendGraph(controller, kpi.history(), kpi.decimals());
    }

    private void setKpiTrendGraph(KpiController controller, List<Double> historyData, int decimals) {
        LineChart<String, Number> chart = controller.getSparklineChart();
        // Disable animation; otherwise an in-flight transition from the previous render
        // can interleave with the new data and the chart shows stale or partial values.
        chart.setAnimated(false);
        chart.setLegendVisible(false);

        // CategoryAxis retains categories across re-renders. Clear them before swapping
        // the series, otherwise old "report N" slots stay on the axis and the new data
        // plots against the wrong x positions (or nothing renders).
        CategoryAxis xAxis = (CategoryAxis) chart.getXAxis();
        chart.getData().clear();
        xAxis.getCategories().clear();

        // Plot oldest-on-the-left, newest-on-the-right. historyData is newest-first.
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int n = historyData.size();
        for (int i = 0; i < n; i++) {
            Double value = historyData.get(n - 1 - i);
            series.getData().add(new XYChart.Data<>(String.valueOf(i + 1), value));
        }
        chart.getData().add(series);
        xAxis.setLabel("Report");
        chart.getYAxis().setLabel("");

        configureSparklineYAxis((NumberAxis) chart.getYAxis(), historyData, decimals);
    }

    private static void configureSparklineYAxis(NumberAxis yAxis, List<Double> historyData, int decimals) {
        double min = historyData.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = historyData.stream().mapToDouble(Double::doubleValue).max().orElse(1);

        if (min == max) {
            double padding = min == 0 ? 1 : Math.abs(min * 0.1);
            min -= padding;
            max += padding;
        } else {
            double pad = (max - min) * 0.2;
            min -= pad;
            max += pad;
        }

        double tickUnit = (max - min) / 3;
        if (tickUnit == 0)
            tickUnit = 1;

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(min);
        yAxis.setUpperBound(max);
        yAxis.setTickUnit(tickUnit);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return String.format(DateFormattingUtil.APP_LOCALE, "%." + decimals + "f", object.doubleValue());
            }
        });
    }

    public void onDistributionGraphMetricChanged(String metric) throws SQLException {
        setPopulationDistributionGraph(
                DashboardCreationService.calculatePopulationDistributionMetrics(activeGroupId, metric),
                metric);
    }

    public void onHistoricalGraphChanged(String metric, String period) throws SQLException {
        setHistoricalImportComparisonGraph(
                DashboardCreationService.calculateHistoricalImportComparisonMetrics(activeGroupId, metric, period));
    }

    public void setPopulationDistributionGraph(PopulationDistributionGraphMetrics metrics, String metric) {
        // Same CategoryAxis pitfall as the sparkline: stale categories from the previous
        // metric stay on the axis, so adding the new series can crash with an out-of-bounds
        // when BarChart maps the new data into the axis's internal slots.
        populationDistributionGraph.setAnimated(false);
        populationDistributionGraph.setLegendVisible(false);

        CategoryAxis xAxis = (CategoryAxis) populationDistributionGraph.getXAxis();
        populationDistributionGraph.getData().clear();
        xAxis.getCategories().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < metrics.labels().size(); i++) {
            series.getData().add(new XYChart.Data<>(metrics.labels().get(i), metrics.binCounts().get(i)));
        }

        String xAxisLabel = switch (metric) {
            case "FCR" -> "FCR";
            case "Feed Cns." -> "kg / day";
            case "Weight" -> "kg";
            default -> "";
        };

        populationDistributionGraph.getData().add(series);
        xAxis.setLabel(xAxisLabel);
        populationDistributionGraph.getYAxis().setLabel("Animals in bracket");

        NumberAxis yAxis = (NumberAxis) populationDistributionGraph.getYAxis();
        int maxCount = metrics.binCounts().stream().mapToInt(Integer::intValue).max().orElse(2);
        int maxY = Math.max(2, ((maxCount + 1) / 2) * 2);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(maxY);
        yAxis.setTickUnit(1);
        yAxis.setMinorTickCount(0);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return Integer.toString(object.intValue());
            }
        });
        yAxis.setForceZeroInRange(true);
    }

    public void setHistoricalImportComparisonGraph(HistoricalImportComparisonMetrics metrics) {
        historicalImportComparisonGraph.setAnimated(false);
        historicalImportComparisonGraph.setLegendVisible(false);

        CategoryAxis xAxis = (CategoryAxis) historicalImportComparisonGraph.getXAxis();
        historicalImportComparisonGraph.getData().clear();
        xAxis.getCategories().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<String> labels = metrics.periods();
        List<Double> values = metrics.values();
        for (int i = 0; i < labels.size(); i++) {
            series.getData().add(new XYChart.Data<>(labels.get(i), values.get(i)));
        }

        historicalImportComparisonGraph.getData().add(series);
        xAxis.setLabel("Report date");
        historicalImportComparisonGraph.getYAxis().setLabel("Percent change (%)");
    }

    private void setupPopulationDistributionGraphSection() {
        pdgMetricSegmentedToggleController.setOptions("FCR", "Feed Cns.", "Weight");
        pdgMetricSegmentedToggleController.addSelectionChangeListener((obs, oldValue, newValue) ->
                runOrLog(() -> onDistributionGraphMetricChanged(newValue)));
    }

    private void setupHistoricalImportGraphSection() {
        hicMetricSegmentedToggleController.setOptions("FCR", "Feed Cns.", "Weight");
        hicPeriodSegmentedToggleController.setOptions("7 Days", "1 Month", "6 Months");

        Consumer<Object> refresh = ignored -> runOrLog(() -> onHistoricalGraphChanged(getHicMetric(), getHicPeriod()));
        hicMetricSegmentedToggleController.addSelectionChangeListener((obs, oldValue, newValue) -> refresh.accept(null));
        hicPeriodSegmentedToggleController.addSelectionChangeListener((obs, oldValue, newValue) -> refresh.accept(null));
    }

    @FunctionalInterface
    private interface SqlAction { void run() throws SQLException; }

    private static void runOrLog(SqlAction action) {
        try {
            action.run();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getPdgMetric() {
        return pdgMetricSegmentedToggleController.getSelected();
    }

    private String getHicMetric() {
        return hicMetricSegmentedToggleController.getSelected();
    }

    private String getHicPeriod() {
        return hicPeriodSegmentedToggleController.getSelected();
    }
}
