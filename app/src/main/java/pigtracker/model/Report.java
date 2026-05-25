package pigtracker.model;

import java.util.List;

public record Report(String reportNumber, String importDate, String period, String dataRows, String numPigs,
        List<MeanMedianMetric> meanMedianPanels, List<TopThreePigs> topPigPanels) {}
