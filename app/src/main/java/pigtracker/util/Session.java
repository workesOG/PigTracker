// Af Nikolaj Jakobsen

package pigtracker.util;

import java.util.prefs.Preferences;

import pigtracker.model.User;

public final class Session {

    private static final Preferences PREFS = Preferences.userNodeForPackage(Session.class);
    private static final String KEY_USERNAME = "remembered_username";

    private static User currentUser;

    private Session() {}

    // Stores the logged-in user and remembers the username on this machine.
    public static void setCurrentUser(User user) {
        currentUser = user;
        PREFS.put(KEY_USERNAME, user.username());
    }

    // Returns the currently logged-in user, or null if nobody is logged in.
    public static User getCurrentUser() {
        return currentUser;
    }

    // Clears the session and forgets the remembered username (logout).
    public static void clear() {
        currentUser = null;
        PREFS.remove(KEY_USERNAME);
    }

    // Returns true if a user is currently logged in.
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Returns true if the logged-in user has admin permission.
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    // Returns the username remembered from a previous login, or null if none.
    public static String getRememberedUsername() {
        return PREFS.get(KEY_USERNAME, null);
    }
}