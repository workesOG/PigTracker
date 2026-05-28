// Theis Thomsen

package pigtracker.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import pigtracker.dao.GroupDAO;
import pigtracker.model.Group;

public final class GroupHandlingService {

    private GroupHandlingService() {}

    public static int getOrCreateGroup(String name) throws SQLException {
        Optional<Group> existing = GroupDAO.findByName(name);
        if (existing.isPresent()) {
            return existing.get().id();
        }
        return GroupDAO.create(new Group(0, name, Group.CreationStatus.IN_PROGRESS)).id();
    }

    public static Group getFirstGroup() throws SQLException {
        List<Group> groups = GroupDAO.getAll();
        return groups.isEmpty() ? null : groups.get(0);
    }

    public static boolean doesDatabaseHoldAtLeastTwoGroups() throws SQLException {
        return GroupDAO.getAll().size() >= 2;
    }
}
