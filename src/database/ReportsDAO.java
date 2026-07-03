package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportsDAO {

    public Map<String, Integer> getPatientStatusBreakdown() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM patients GROUP BY status ORDER BY cnt DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load patient status report: " + e.getMessage(), e);
        }
        return data;
    }

    public Map<String, Integer> getWardOccupancyBreakdown() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT ward, COUNT(*) AS cnt FROM patients WHERE status = 'ADMITTED' "
                + "AND ward IS NOT NULL AND ward != '' GROUP BY ward ORDER BY cnt DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("ward"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load ward occupancy report: " + e.getMessage(), e);
        }
        return data;
    }

    public Map<String, Integer> getBillingStatusBreakdown() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM billing GROUP BY status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load billing report: " + e.getMessage(), e);
        }
        return data;
    }

    public Map<String, Double> getDepartmentRevenueEstimate() {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = "SELECT d.department, COALESCE(SUM(b.amount), 0) AS total "
                + "FROM billing b JOIN patients p ON b.patient_id = p.id "
                + "JOIN doctors d ON p.assigned_doctor_id = d.id "
                + "GROUP BY d.department ORDER BY total DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("department"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load revenue report: " + e.getMessage(), e);
        }
        return data;
    }
}
