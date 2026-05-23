package pigtracker.controller;

import java.util.ArrayList;

import javafx.fxml.FXML;
import pigtracker.model.MetricOption;

public class DataController {
    @FXML
    SpecificDataController animalDataController;

    @FXML
    public void initialize() {
        loadAnimalMetrics();
    }

    private void loadAnimalMetrics() {
        ArrayList<MetricOption> animalMetrics = new ArrayList<>();

        animalMetrics.add(new MetricOption("Animal ID", MetricOption.MetricType.INTEGER,
                new Boolean[] { true, true, true, true }));
        animalMetrics.add(
                new MetricOption("FCR", MetricOption.MetricType.DECIMAL, new Boolean[] { true, true, true, true }));
        animalMetrics.add(new MetricOption("Start Weight", MetricOption.MetricType.DECIMAL,
                new Boolean[] { true, true, true, true }));
        animalMetrics.add(
                new MetricOption("Weight", MetricOption.MetricType.DECIMAL, new Boolean[] { true, true, true, true }));
        animalMetrics.add(new MetricOption("Weight gain", MetricOption.MetricType.DECIMAL,
                new Boolean[] { true, true, true, true }));
        animalMetrics.add(new MetricOption("Feed intake", MetricOption.MetricType.DECIMAL,
                new Boolean[] { true, true, true, true }));
        animalMetrics.add(new MetricOption("Days tested", MetricOption.MetricType.INTEGER,
                new Boolean[] { true, true, true, true }));
        animalMetrics.add(
                new MetricOption("Start day", MetricOption.MetricType.DATE, new Boolean[] { true, true, true, true }));
        animalMetrics.add(new MetricOption("Location", MetricOption.MetricType.INTEGER,
                new Boolean[] { true, true, true, true }));

        animalDataController.setMetrics(animalMetrics);
    }
}
