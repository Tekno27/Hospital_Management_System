package database;

import models.Ward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WardDAO {

    public List<Ward> getAllWithStats() {
        List<Ward> wards = new ArrayList<>();
        String sql = "SELECT w.id, w.name, w.department, w.bed_capacity, w.floor, w.notes, "
                + "(SELECT COUNT(*) FROM patients p WHERE p.ward = w.name AND p.status = 'ADMITTED') AS occupied, "
                + "(SELECT COUNT(*) FROM nurses n WHERE n.ward = w.name) AS nurse_count "
                + "FROM wards w ORDER BY w.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                wards.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch wards: " + e.getMessage(), e);
        }
        return wards;
    }

    public List<Ward> search(String keyword) {
        List<Ward> all = getAllWithStats();
        String lower = keyword.toLowerCase();
        return all.stream()
                .filter(w -> w.getName().toLowerCase().contains(lower)
                        || (w.getDepartment() != null && w.getDepartment().toLowerCase().contains(lower)))
                .toList();
    }

    public Ward getByName(String name) {
        return getAllWithStats().stream()
                .filter(w -> w.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private Ward mapRow(ResultSet rs) throws SQLException {
        return new Ward(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getInt("bed_capacity"),
                rs.getString("floor"),
                rs.getString("notes"),
                rs.getInt("occupied"),
                rs.getInt("nurse_count")
        );
    }
}
