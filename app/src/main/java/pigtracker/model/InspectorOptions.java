// Theis Thomsen

package pigtracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class InspectorOptions {
    public static class Field {
        public final String label;
        public final Function<Object, String> valueFunction;
        public final boolean readOnly;
        public final Predicate<Object> visibleIf;

        public Field(String label, Function<Object, String> valueFunction, boolean readOnly,
                Predicate<Object> visibleIf) {
            this.label = label;
            this.valueFunction = valueFunction;
            this.readOnly = readOnly;
            this.visibleIf = visibleIf;
        }
    }

    public static class Button {
        public final String label;
        public final Predicate<Object> visibleIf;
        public final Consumer<Object> onClick;

        public Button(String label, Predicate<Object> visibleIf, Consumer<Object> onClick) {
            this.label = label;
            this.visibleIf = visibleIf;
            this.onClick = onClick;
        }
    }

    private final List<Field> extraFields = new ArrayList<>();
    private final List<Button> extraButtons = new ArrayList<>();

    public List<Field> getExtraFields() {
        return extraFields;
    }

    public List<Button> getExtraButtons() {
        return extraButtons;
    }

    public void addField(Field field) {
        extraFields.add(field);
    }

    public void addButton(Button button) {
        extraButtons.add(button);
    }
}