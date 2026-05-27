// Theis Thomsen

package pigtracker.model;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class MetricColumn {
    final String label;
    final MetricOption.MetricType type;
    final Boolean[] visibility;
    final Function<Object, Object> extractor;
    final Function<Object, String> formatter;
    final boolean editable;
    private final BiConsumer<Object, Object> updateInstruction;

    public MetricColumn(String label, MetricOption.MetricType type, Boolean[] visibility,
            Function<Object, Object> extractor, Function<Object, String> formatter, boolean editable,
            BiConsumer<Object, Object> updateInstruction) {
        this.label = label;
        this.type = type;
        this.visibility = visibility;
        this.extractor = extractor;
        this.formatter = formatter;
        this.editable = editable;
        this.updateInstruction = updateInstruction;
    }

    public String getLabel() {
        return label;
    }

    public MetricOption.MetricType getType() {
        return type;
    }

    public Boolean[] getVisibility() {
        return visibility;
    }

    public Function<Object, Object> getExtractor() {
        return extractor;
    }

    public Function<Object, String> getFormatter() {
        return formatter;
    }

    public boolean isEditable() {
        return editable;
    }

    public BiConsumer<Object, Object> getUpdateInstruction() {
        return updateInstruction;
    }
}