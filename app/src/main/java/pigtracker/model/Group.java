// Theis Thomsen

package pigtracker.model;

public record Group(
    int id,
    String name,
    CreationStatus creationStatus
) {
    public enum CreationStatus {
        IN_PROGRESS, COMPLETE
    }

    // Used after insertions to set the database-generated id
    public Group withId(int newId) {
        return new Group(newId, name, creationStatus);
    }
}