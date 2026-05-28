// Theis Thomsen

package pigtracker.model;

import java.util.Arrays;

public record TopThreePigs(String metric, int[] pigNumbers, double[] pigValues, DisplayType displayType) {

    public String[] getDisplayStrings() {
        return Arrays.stream(pigValues).mapToObj(displayType::format).toArray(String[]::new);
    }
}
