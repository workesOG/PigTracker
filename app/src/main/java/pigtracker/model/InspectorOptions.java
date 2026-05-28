// Theis Thomsen

package pigtracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class InspectorOptions {

    public record Field(
            String label,
            Function<Object, String> valueFunction,
            boolean readOnly,
            Predicate<Object> visibleIf) {}

    public record Button(
            String label,
            Predicate<Object> visibleIf,
            Consumer<Object> onClick) {}

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
