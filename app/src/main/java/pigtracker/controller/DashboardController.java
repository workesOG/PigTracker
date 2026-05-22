// Af Theis Thomsen

package pigtracker.controller;

import javafx.fxml.FXML;
import pigtracker.controller.components.KpiController;
import pigtracker.controller.components.SegmentedToggleController;

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
    public void initialize() {
        setupKpis();
        setupPopulationDistributionGraphSection();
        setupHistoricalImportGraphSection();
    }

    private void setupKpis() {
        fcrCardController.setTitle("Feed Conversion Rate (FCR)");
        fcrCardController.setValue(82.4, 1);
        fcrCardController.setUnit("kg feed / kg gain");
        fcrCardController.setTrend(3.2);

        weightCardController.setTitle("Animal Weight");
        weightCardController.setValue(2.76, 2);
        weightCardController.setUnit("kg gain / day");
        weightCardController.setTrend(-1.1);

        feedCardController.setTitle("Feed Consumption");
        feedCardController.setValue(812, 0);
        feedCardController.setUnit("g feed / day");
        feedCardController.setTrend(1.8);

        populationCardController.setTitle("Animal Population");
        populationCardController.setValue(1.2, 1);
        populationCardController.setUnit("pigs");
        populationCardController.setTrend(-0.4);
    }

    private void setupPopulationDistributionGraphSection() {
        pdgMetricSegmentedToggleController.setOptions("FCR", "Feed Cns.", "Weight");
    }

    private void setupHistoricalImportGraphSection() {
        hicMetricSegmentedToggleController.setOptions("FCR", "Feed Cns.", "Weight");
        hicPeriodSegmentedToggleController.setOptions("7 Days", "1 Month", "6 Months");
    }
}
