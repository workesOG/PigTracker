// Theis Thomsen

package pigtracker.model;

import pigtracker.util.DateFormattingUtil;

public record TopThreePigs(String metric, int[] pigNumbers, double[] pigValues, DisplayType displayType) {
    public String[] getDisplayStrings() {
        String[] result = new String[pigValues.length];
        switch (displayType) {
        case DECIMAL -> {
            for (int i = 0; i < pigValues.length; i++) {
                result[i] = String.format("%.2f", pigValues[i]);
            }
            return result;
        }
        case INT -> {
            for (int i = 0; i < pigValues.length; i++) {
                result[i] = String.format("%d", (int)Math.round(pigValues[i]));
            }
            return result;
        }
        case TIME -> {
            for (int i = 0; i < pigValues.length; i++) {
                result[i] = DateFormattingUtil.formatSecondsAsMinSec(pigValues[i]);
            }
            return result;
        }
        }
        return result; // Fallback, should not be reached
    }
}
