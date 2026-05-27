// Af Nikolaj Jakobsen

package pigtracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void isAdminIsTrueForAdminPermission() {
        User user = new User(1, "alice", "secret", User.Permission.ADMIN);

        assertTrue(user.isAdmin());
    }

    @Test
    void isAdminIsFalseForDefaultPermission() {
        User user = new User(2, "bob", "secret", User.Permission.DEFAULT);

        assertFalse(user.isAdmin());
    }
}
