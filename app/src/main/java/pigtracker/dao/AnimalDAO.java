// Af Nikolaj Jakobsen

package pigtracker.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import pigtracker.model.Animal;

public final class AnimalDAO {

    private static final String COLUMNS = "id, animal_number, responder, group_id, location, status, stopped_reason, "
            + "stopped_at, fcr, start_weight_kg, total_feed_kg, weight_gain_kg, latest_weight_kg, completed_days, "
            + "start_day, created_at";
    private static final String SELECT_ALL = "SELECT " + COLUMNS + " FROM Animals";
    private static final String INSERT = "INSERT INTO Animals (animal_number, responder, group_id, location, status, "
            + "stopped_reason, stopped_at, fcr, start_weight_kg, total_feed_kg, weight_gain_kg, latest_weight_kg, "
            + "completed_days, start_day) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE Animals SET animal_number = ?, responder = ?, group_id = ?, "
            + "location = ?, status = ?, stopped_reason = ?, stopped_at = ?, fcr = ?, start_weight_kg = ?, "
            + "total_feed_kg = ?, weight_gain_kg = ?, latest_weight_kg = ?, completed_days = ?, start_day = ? "
            + "WHERE id = ?";

    private AnimalDAO() {}

    // Inserts a new animal and returns it, including the database-generated id.
    public static Animal create(Animal animal) throws SQLException {
        int id = Db.insertReturningId(INSERT, ps -> bindAnimal(ps, animal));
        return animal.withId(id);
    }

    // Returns the animal with the given id, or empty if it does not exist.
    public static Optional<Animal> findById(int id) throws SQLException {
        return Db.findOne(SELECT_ALL + " WHERE id = ?", ps -> ps.setInt(1, id), AnimalDAO::mapRow);
    }

    // Returns the animal with the given responder (RFID tag), or empty if none.
    public static Optional<Animal> findByResponder(String responder) throws SQLException {
        return Db.findOne(SELECT_ALL + " WHERE responder = ?", ps -> ps.setString(1, responder), AnimalDAO::mapRow);
    }

    // Theis Thomsen
    // Returns the animal with the given animal number, or empty if none exists.
    public static Optional<Animal> findByAnimalNumber(int animalNumber) throws SQLException {
        return Db.findOne(SELECT_ALL + " WHERE animal_number = ?",
                ps -> ps.setInt(1, animalNumber), AnimalDAO::mapRow);
    }

    // Returns every animal, ordered by location and animal number.
    public static List<Animal> getAll() throws SQLException {
        return Db.findMany(SELECT_ALL + " ORDER BY location, animal_number", Db.NO_PARAMS, AnimalDAO::mapRow);
    }

    // Returns all animals with the given status — used when filtering the herd.
    public static List<Animal> findByStatus(Animal.Status status) throws SQLException {
        return Db.findMany(SELECT_ALL + " WHERE status = ? ORDER BY location, animal_number",
                ps -> ps.setString(1, status.name()), AnimalDAO::mapRow);
    }

    // Updates an existing animal by id; returns true if a row was changed.
    public static boolean update(Animal animal) throws SQLException {
        return Db.executeUpdate(UPDATE, ps -> {
            bindAnimal(ps, animal);
            ps.setInt(15, animal.id());
        }) > 0;
    }

    // Deletes the animal with the given id; returns true if a row was removed.
    public static boolean delete(int id) throws SQLException {
        return Db.executeUpdate("DELETE FROM Animals WHERE id = ?", ps -> ps.setInt(1, id)) > 0;
    }

    public static Optional<Integer> getGroupIdByAnimalNumber(int animalNumber) throws SQLException {
        return Db.findOne("SELECT group_id FROM Animals WHERE animal_number = ?",
                ps -> ps.setInt(1, animalNumber),
                rs -> rs.getInt("group_id"));
    }

    // Binds the 14 insert/update columns (everything except id and created_at) onto the statement.
    private static void bindAnimal(PreparedStatement ps, Animal a) throws SQLException {
        ps.setInt(1, a.animalNumber());
        ps.setString(2, a.responder());
        ps.setInt(3, a.groupId());
        ps.setInt(4, a.location());
        ps.setString(5, a.status().name());
        ps.setObject(6, a.stoppedReason());
        ps.setObject(7, a.stoppedAt());
        ps.setObject(8, a.fcr());
        ps.setObject(9, a.startWeightKg());
        ps.setObject(10, a.totalFeedKg());
        ps.setObject(11, a.weightGainKg());
        ps.setObject(12, a.latestWeightKg());
        ps.setObject(13, a.completedDays());
        ps.setObject(14, a.startDay());
    }

    private static Animal mapRow(ResultSet rs) throws SQLException {
        return new Animal(
                rs.getInt("id"),
                rs.getInt("animal_number"),
                rs.getString("responder"),
                rs.getInt("group_id"),
                rs.getInt("location"),
                Animal.Status.valueOf(rs.getString("status").trim().toUpperCase()),
                rs.getString("stopped_reason"),
                rs.getObject("stopped_at", LocalDateTime.class),
                Db.nullableDouble(rs, "fcr"),
                Db.nullableDouble(rs, "start_weight_kg"),
                Db.nullableDouble(rs, "total_feed_kg"),
                Db.nullableDouble(rs, "weight_gain_kg"),
                Db.nullableDouble(rs, "latest_weight_kg"),
                Db.nullableInt(rs, "completed_days"),
                rs.getObject("start_day", LocalDate.class),
                rs.getObject("created_at", LocalDateTime.class));
    }
}
