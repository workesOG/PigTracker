// Af Nikolaj Jakobsen

package pigtracker.model;

public record User(int id, String username, String password, Permission permission) {

    public enum Permission {
        ADMIN,
        DEFAULT
    }

    public boolean isAdmin() {
        return permission == Permission.ADMIN;
    }
}