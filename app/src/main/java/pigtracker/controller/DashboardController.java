// Af Theis Thomsen

package pigtracker.controller;

import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import pigtracker.Main;
import pigtracker.controller.components.KpiController;
import pigtracker.controller.components.SegmentedToggleController;
import pigtracker.model.DashboardMetrics;
import pigtracker.model.HistoricalImportComparisonMetrics;
import pigtracker.model.KpiMetrics;
import pigtracker.model.PopulationDistributionGraphMetrics;
import pigtracker.service.DashboardCreationService;
import pigtracker.util.MathUtil;
import pigtracker.util.MetricsUtil;

public class DashboardController {
    @FXML
    private KpiController fcrCardController;

    @FXML
    private KpiController weightCardController;

    @FXML
    private KpiController feedCardController;

    @FXML
    private KpiController populationCardController;

    @FXML
    private SegmentedToggleController pdgMetricSegmentedToggleController;

    @FXML
    private SegmentedToggleController hicMetricSegmentedToggleController;

    @FXML
    private SegmentedToggleController hicPeriodSegmentedToggleController;

    @FXML
    private BarChart<String, Number> populationDistributionGraph;

    @FXML
    private BarChart<String, Number> historicalImportComparisonGraph;

    @FXML
    public void initialize() throws Exception {
        pigtracker.util.AppContext.setDashboardController(this);
        setupPopulationDistributionGraphSection();
        setupHistoricalImportGraphSection();
        Main.onDashboardReady();
    }

    public void setDashboardMetrics() throws SQLException {
        DashboardMetrics dashboardMetrics = DashboardCreationService.calculateDashboardMetrics();

        String pdgMetric = getPdgMetric();
        PopulationDistributionGraphMetrics pdgMetrics = DashboardCreationService
                .calculatePopulationDistributionMetrics(pdgMetric);

        String hicMetric = getHicMetric();
        String hicPeriod = getHicPeriod();
        HistoricalImportComparisonMetrics hicMetrics = DashboardCreationService
                .calculateHistoricalImportComparisonMetrics(hicMetric, hicPeriod);

        if (dashboardMetrics == null) {
            setEmpty();
            return;
        }
        setKpis(dashboardMetrics);
        setPopulationDistributionGraph(pdgMetrics, pdgMetric);
        setHistoricalImportComparisonGraph(hicMetrics);
        // TODO: Add calls for graphs once you wire them in
    }

    public void setEmpty() {
        fcrCardController.setTitle("Feed Conversion Rate (FCR)");
        fcrCardController.setValue(0, 1);
        fcrCardController.setUnit("kg feed / kg gain");
        fcrCardController.setTrend(0);
        fcrCardController.setDescription("Mean");

        weightCardController.setTitle("Animal Weight");
        weightCardController.setValue(0, 2);
        weightCardController.setUnit("kg gain / day");
        weightCardController.setTrend(0);
        weightCardController.setDescription("Mean");

        feedCardController.setTitle("Feed Consumption");
        feedCardController.setValue(0, 0);
        feedCardController.setUnit("g feed / day");
        feedCardController.setTrend(0);
        feedCardController.setDescription("Mean");

        populationCardController.setTitle("Animal Population");
        populationCardController.setValue(0, 0);
        populationCardController.setUnit("pigs");
        populationCardController.setTrend(0);
        populationCardController.setDescription("Total");
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
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = historyData.size() - 1; i >= 0; i--) {
            String label = String.valueOf(historyData.size() - i);
            series.getData().add(new XYChart.Data<>(label, historyData.get(i)));
        }
        chart.getData().clear();
        chart.getData().add(series);
        chart.getXAxis().setLabel("Report");
        chart.getYAxis().setLabel("");

        NumberAxis yAxis = (NumberAxis)chart.getYAxis();
        double min = historyData.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = historyData.stream().mapToDouble(Double::doubleValue).max().orElse(1);

        if (min == max) {
            min = min - (min == 0 ? 1 : Math.abs(min * 0.1));
            max = max + (max == 0 ? 1 : Math.abs(max * 0.1));
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
                String format = "%." + decimals + "f";
                return String.format(format, object.doubleValue());
            }
        });
    }

    public void onDistributionGraphMetricChanged(String metric) throws SQLException {
        PopulationDistributionGraphMetrics pdgMetrics = DashboardCreationService
                .calculatePopulationDistributionMetrics(metric);

        setPopulationDistributionGraph(pdgMetrics, metric);
    }

    public void onHistoricalGraphChanged(String metric, String period) throws SQLException {
        HistoricalImportComparisonMetrics metrics = DashboardCreationService
                .calculateHistoricalImportComparisonMetrics(metric, period);

        setHistoricalImportComparisonGraph(metrics);
    }

    public void setPopulationDistributionGraph(PopulationDistributionGraphMetrics metrics, String metric) {
        populationDistributionGraph.getData().clear();
        populationDistributionGraph.setLegendVisible(false);

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
        populationDistributionGraph.getXAxis().setLabel(xAxisLabel);
        populationDistributionGraph.getYAxis().setLabel("Animals in bracket");

        NumberAxis yAxis = (NumberAxis)populationDistributionGraph.getYAxis();
        // Compute min/max for the axis, based on your bin counts:
        int maxCount = metrics.binCounts().stream().mapToInt(Integer::intValue).max().orElse(2);
        int maxY = Math.max(2, ((maxCount + 1) / 2) * 2); // next even number ≥ counts

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(maxY);
        yAxis.setTickUnit(1);
        yAxis.setMinorTickCount(0);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return String.format("%d", object.intValue());
            }
        });
        yAxis.setForceZeroInRange(true);
    }

    public void setHistoricalImportComparisonGraph(HistoricalImportComparisonMetrics metrics) {
        historicalImportComparisonGraph.getData().clear();
        historicalImportComparisonGraph.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        List<String> labels = metrics.periods();
        List<Double> values = metrics.values();

        for (int i = 0; i < labels.size(); i++) {
            series.getData().add(new XYChart.Data<>(labels.get(i), values.get(i)));
        }

        historicalImportComparisonGraph.getData().add(series);
        historicalImportComparisonGraph.getXAxis().setLabel("Report date");
        historicalImportComparisonGraph.getYAxis().setLabel("Percent change (%)");
    }

    private void setupPopulationDistributionGraphSection() {
        pdgMetricSegmentedToggleController.setOptions("FCR", "Feed Cns.", "Weight");
        pdgMetricSegmentedToggleController.addSelectionChangeListener((observable, oldValue, newValue) -> {
            try {
                onDistributionGraphMetricChanged(newValue);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupHistoricalImportGraphSection() {
        hicMetricSegmentedToggleController.setOptions("FCR", "Feed Cns.", "Weight");
        hicPeriodSegmentedToggleController.setOptions("7 Days", "1 Month", "6 Months");

        hicMetricSegmentedToggleController.addSelectionChangeListener((obs, oldValue, newValue) -> {
            try {
                onHistoricalGraphChanged(getHicMetric(), getHicPeriod());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        hicPeriodSegmentedToggleController.addSelectionChangeListener((obs, oldValue, newValue) -> {
            try {
                onHistoricalGraphChanged(getHicMetric(), getHicPeriod());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
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
