// Theis Thomsen

package pigtracker.model;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record MetricColumn(
        String label,
        MetricOption.MetricType type,
        Boolean[] visibility,
        Function<Object, Object> extractor,
        Function<Object, String> formatter,
        boolean editable,
        BiConsumer<Object, Object> updateInstruction) {}
