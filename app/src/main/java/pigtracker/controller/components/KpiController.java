package pigtracker.controller.components;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class KpiController {
    @FXML
    private Label titleLabel;

    @FXML
    private Label valueLabel;

    @FXML
    private Label unitLabel;

    @FXML
    private Label trendLabel;

    @FXML
    private ImageView trendIcon;

    @FXML
    private LineChart<?, ?> sparklineChart;

    private final Image UP_IMAGE = new Image(
            getClass().getResourceAsStream(
                    "/images/upwards_64.png"));

    private final Image DOWN_IMAGE = new Image(
            getClass().getResourceAsStream(
                    "/images/downwards_64.png"));

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setValue(double value, int decimals) {

        String format = "%." + decimals + "f";
        valueLabel.setText(
                String.format(format, value));
    }

    public void setUnit(String unit) {
        unitLabel.setText(unit);
    }

    public void setTrend(double percentChange) {
        trendLabel.setText(
                String.format("%+.1f%%", percentChange));

        if (percentChange >= 0) {
            trendLabel.setStyle("-fx-text-fill: green;");
            trendIcon.setImage(UP_IMAGE);
        } else {
            trendLabel.setStyle("-fx-text-fill: red;");
            trendIcon.setImage(DOWN_IMAGE);
        }
    }
}
