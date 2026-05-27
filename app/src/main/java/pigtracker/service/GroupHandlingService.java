// Theis Thomsen

package pigtracker.service;

import pigtracker.dao.GroupDAO;
import pigtracker.model.Group;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GroupHandlingService {
    public static int getOrCreateGroup(String name) throws SQLException {
        Optional<Group> existing = GroupDAO.findByName(name);
        if (existing.isPresent()) {
            return existing.get().id();
        }
        Group created = GroupDAO.create(new Group(0, name, Group.CreationStatus.IN_PROGRESS));
        return created.id();
    }

    public static Group getFirstGroup() throws SQLException {
        List<Group> groups = GroupDAO.getAll();
        if (groups.isEmpty()) {
            return null;
        }
        return groups.get(0);
    }

    public static boolean doesDatabaseHoldAtLeastTwoGroups() throws SQLException {
        List<Group> groups = GroupDAO.getAll();
        if (groups.isEmpty() || groups.size() < 2) {
            return false;
        }
        return true;
    }
}