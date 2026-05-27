// Af Nikolaj Jakobsen

package pigtracker.dao;

import pigtracker.model.Animal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AnimalDAO {

    private AnimalDAO() {}

    // Inserts a new animal and returns it, including the database-generated id.
    public static Animal create(Animal animal) throws SQLException {
        String sql = "INSERT INTO Animals (animal_number, responder, location, status, stopped_reason, stopped_at, fcr, start_weight_kg, total_feed_kg, weight_gain_kg, latest_weight_kg, completed_days, start_day) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindAnimal(ps, animal);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;

                return animal.withId(id);
            }
        }
    }

    // Returns the animal with the given id, or empty if it does not exist.
    public static Optional<Animal> findById(int id) throws SQLException {
        String sql = "SELECT id, animal_number, responder, location, status, stopped_reason, stopped_at, fcr, start_weight_kg, total_feed_kg, weight_gain_kg, latest_weight_kg, completed_days, start_day, created_at FROM Animals WHERE id = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    // Returns the animal with the given responder (RFID tag), or empty if none.
    public static Optional<Animal> findByResponder(String responder) throws SQLException {
        String sql = "SELECT id, animal_number, responder, location, status, stopped_reason, stopped_at, fcr, start_weight_kg, total_feed_kg, weight_gain_kg, latest_weight_kg, completed_days, start_day, created_at FROM Animals WHERE responder = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, responder);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    // Theis Thomsen
    // Returns the animal with the given animal number, or empty if none exists.
    public static Optional<Animal> findByAnimalNumber(int animalNumber) throws SQLException {
        String sql = "SELECT id, animal_number, responder, location, status, stopped_reason, stopped_at, fcr, start_weight_kg, total_feed_kg, weight_gain_kg, latest_weight_kg, completed_days, start_day, created_at FROM Animals WHERE animal_number = ?";
        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, animalNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    // Returns every animal, ordered by location and animal number.
    public static List<Animal> getAll() throws SQLException {
        String sql = "SELECT id, animal_number, responder, location, status, stopped_reason, stopped_at, fcr, start_weight_kg, total_feed_kg, weight_gain_kg, latest_weight_kg, completed_days, start_day, created_at FROM Animals ORDER BY location, animal_number";
        List<Animal> animals = new ArrayList<>();

        try (Connection conn = ConnectionDAO.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                animals.add(mapRow(rs));
            }
        }

        return animals;
    }

    // Returns all animals with the given status - used when filtering the herd.
    public static List<Animal> findByStatus(Animal.Status status) throws SQLException {
        String sql = "SELECT id, animal_number, responder, location, status, stopped_reason, stopped_at, fcr, start_weight_kg, total_feed_kg, weight_gain_kg, latest_weight_kg, completed_days, start_day, created_at FROM Animals WHERE status = ? ORDER BY location, animal_number";
        List<Animal> animals = new ArrayList<>();

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    animals.add(mapRow(rs));
                }
            }
        }

        return animals;
    }

    // Updates an existing animal by id; returns true if a row was changed.
    public static boolean update(Animal animal) throws SQLException {
        String sql = "UPDATE Animals SET animal_number = ?, responder = ?, location = ?, status = ?, stopped_reason = ?, stopped_at = ?, fcr = ?, start_weight_kg = ?, total_feed_kg = ?, weight_gain_kg = ?, latest_weight_kg = ?, completed_days = ?, start_day = ? WHERE id = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindAnimal(ps, animal);
            ps.setInt(14, animal.id());

            return ps.executeUpdate() > 0;
        }
    }

    // Deletes the animal with the given id; returns true if a row was removed.
    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Animals WHERE id = ?";

        try (Connection conn = ConnectionDAO.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Binds the 16 insert/update columns (everything except id and created_at) onto
    // the statement.
    private static void bindAnimal(PreparedStatement ps, Animal a) throws SQLException {
        ps.setInt(1, a.animalNumber());
        ps.setString(2, a.responder());
        ps.setInt(3, a.location());
        ps.setString(4, a.status().name());
        ps.setObject(5, a.stoppedReason());
        ps.setObject(6, a.stoppedAt());
        ps.setObject(7, a.fcr());
        ps.setObject(8, a.startWeightKg());
        ps.setObject(9, a.totalFeedKg());
        ps.setObject(10, a.weightGainKg());
        ps.setObject(11, a.latestWeightKg());
        ps.setObject(12, a.completedDays());
        ps.setObject(13, a.startDay());
    }

    // Builds an Animal object from the current row of the given ResultSet.
    private static Animal mapRow(ResultSet rs) throws SQLException {
        return new Animal(rs.getInt("id"), rs.getInt("animal_number"), rs.getString("responder"), rs.getInt("location"),
                Animal.Status.valueOf(rs.getString("status").trim().toUpperCase()), rs.getString("stopped_reason"),
                rs.getObject("stopped_at", LocalDateTime.class), getNullableDouble(rs, "fcr"),
                getNullableDouble(rs, "start_weight_kg"), getNullableDouble(rs, "total_feed_kg"),
                getNullableDouble(rs, "weight_gain_kg"), getNullableDouble(rs, "latest_weight_kg"),
                getNullableInt(rs, "completed_days"), rs.getObject("start_day", LocalDate.class),
                rs.getObject("created_at", LocalDateTime.class));
    }

    // Reads an int column that may be NULL, returning null instead of 0.
    private static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);

        return rs.wasNull() ? null : value;
    }

    // Reads a decimal column that may be NULL, returning null instead of 0.0.
    private static Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);

        return rs.wasNull() ? null : value;
    }
}