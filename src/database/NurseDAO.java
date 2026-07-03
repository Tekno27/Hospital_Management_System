package database;

import models.Nurse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NurseDAO {

    public List<Nurse> getAll() {
        List<Nurse> nurses = new ArrayList<>();
        String sql = "SELECT id, full_name, phone, email, ward, shift FROM nurses ORDER BY full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                nurses.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch nurses: " + e.getMessage(), e);
        }
        return nurses;
    }

    public List<Nurse> search(String keyword) {
        List<Nurse> nurses = new ArrayList<>();
        String sql = "SELECT id, full_name, phone, email, ward, shift FROM nurses "
                + "WHERE full_name LIKE ? OR ward LIKE ? OR phone LIKE ? ORDER BY full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    nurses.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search nurses: " + e.getMessage(), e);
        }
        return nurses;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM nurses";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count nurses: " + e.getMessage(), e);
        }
    }

    public String getWardById(int nurseId) {
        String sql = "SELECT ward FROM nurses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nurseId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("ward") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch nurse ward: " + e.getMessage(), e);
        }
    }

    private Nurse mapRow(ResultSet rs) throws SQLException {
        return new Nurse(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("ward"),
                rs.getString("shift")
        );
    }
}
