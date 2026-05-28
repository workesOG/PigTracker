// Theis Thomsen

package pigtracker.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import pigtracker.model.Group;

public final class GroupDAO {

    private static final String SELECT_ALL = "SELECT id, name, creation_status FROM Groups";

    private GroupDAO() {}

    public static Group create(Group group) throws SQLException {
        int id = Db.insertReturningId(
                "INSERT INTO Groups (name, creation_status) VALUES (?, ?)",
                ps -> {
                    ps.setString(1, group.name());
                    ps.setString(2, group.creationStatus().name());
                });
        return group.withId(id);
    }

    public static Optional<Group> findById(int id) throws SQLException {
        return Db.findOne(SELECT_ALL + " WHERE id = ?", ps -> ps.setInt(1, id), GroupDAO::mapRow);
    }

    public static Optional<Group> findByName(String name) throws SQLException {
        return Db.findOne(SELECT_ALL + " WHERE name = ?", ps -> ps.setString(1, name), GroupDAO::mapRow);
    }

    public static List<Group> getAll() throws SQLException {
        return Db.findMany(SELECT_ALL + " ORDER BY id", Db.NO_PARAMS, GroupDAO::mapRow);
    }

    public static boolean update(Group group) throws SQLException {
        return Db.executeUpdate(
                "UPDATE Groups SET name = ?, creation_status = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, group.name());
                    ps.setString(2, group.creationStatus().name());
                    ps.setInt(3, group.id());
                }) > 0;
    }

    public static void updateStatus(int groupId, Group.CreationStatus status) throws SQLException {
        Db.executeUpdate("UPDATE Groups SET creation_status = ? WHERE id = ?", ps -> {
            ps.setString(1, status.name());
            ps.setInt(2, groupId);
        });
    }

    public static boolean delete(int id) throws SQLException {
        return Db.executeUpdate("DELETE FROM Groups WHERE id = ?", ps -> ps.setInt(1, id)) > 0;
    }

    public static void deleteAllInProgress() throws SQLException {
        Db.executeUpdate("DELETE FROM Groups WHERE creation_status = ?",
                ps -> ps.setString(1, Group.CreationStatus.IN_PROGRESS.name()));
    }

    private static Group mapRow(ResultSet rs) throws SQLException {
        return new Group(
                rs.getInt("id"),
                rs.getString("name"),
                Group.CreationStatus.valueOf(rs.getString("creation_status").trim().toUpperCase()));
    }
}
