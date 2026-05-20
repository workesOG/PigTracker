// Af Nikolaj Jakobsen

package pigtracker.util;

import pigtracker.model.User;

public final class Session {

    private static User currentUser;

    private Session() {}

    // Stores the user that is currently logged in.
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Returns the currently logged-in user, or null if nobody is logged in.
    public static User getCurrentUser() {
        return currentUser;
    }

    // Clears the session, effectively logging the user out.
    public static void clear() {
        currentUser = null;
    }

    // Returns true if a user is currently logged in.
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Returns true if the logged-in user has admin permission.
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}