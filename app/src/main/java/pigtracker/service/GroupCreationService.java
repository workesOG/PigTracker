// Theis Thomsen

package pigtracker.service;

import pigtracker.dao.GroupDAO;
import pigtracker.model.Group;
import java.sql.SQLException;
import java.util.Optional;

public class GroupCreationService {
    public static int getOrCreateGroup(String name) throws SQLException {
        Optional<Group> existing = GroupDAO.findByName(name);
        if (existing.isPresent()) {
            return existing.get().id();
        }
        Group created = GroupDAO.create(new Group(0, name, Group.CreationStatus.IN_PROGRESS));
        return created.id();
    }
}