package database;

import models.LabOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LabOrderDAO {

    private static final String SELECT_BASE =
            "SELECT lo.id, lo.patient_id, p.full_name AS patient_name, lo.doctor_id, d.full_name AS doctor_name, "
                    + "lo.test_id, t.name AS test_name, lo.status, lo.result_value, lo.result_notes, "
                    + "lo.ordered_date, lo.completed_date "
                    + "FROM lab_orders lo "
                    + "JOIN patients p ON lo.patient_id = p.id "
                    + "JOIN lab_tests t ON lo.test_id = t.id "
                    + "LEFT JOIN doctors d ON lo.doctor_id = d.id ";

    public List<LabOrder> getAll() {
        return query(SELECT_BASE + "ORDER BY lo.ordered_date DESC");
    }

    public List<LabOrder> getByDoctorId(int doctorId) {
        return query(SELECT_BASE + "WHERE lo.doctor_id = ? ORDER BY lo.ordered_date DESC", doctorId);
    }

    public List<LabOrder> getByPatientWard(String ward) {
        return query(SELECT_BASE + "WHERE p.ward = ? ORDER BY lo.ordered_date DESC", ward);
    }

    public List<LabOrder> getRecent(int limit) {
        return query(SELECT_BASE + "ORDER BY lo.ordered_date DESC LIMIT " + limit);
    }

    public int countPending() {
        return countByStatus("ORDERED") + countByStatus("IN_PROGRESS");
    }

    public int countPendingForDoctor(int doctorId) {
        String sql = "SELECT COUNT(*) FROM lab_orders WHERE doctor_id = ? AND status IN ('ORDERED', 'IN_PROGRESS')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count pending lab orders: " + e.getMessage(), e);
        }
    }

    public int countPendingForWard(String ward) {
        String sql = "SELECT COUNT(*) FROM lab_orders lo JOIN patients p ON lo.patient_id = p.id "
                + "WHERE p.ward = ? AND lo.status IN ('ORDERED', 'IN_PROGRESS')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ward);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count pending lab orders: " + e.getMessage(), e);
        }
    }

    public double getCompletionRate() {
        String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed FROM lab_orders";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int total = rs.getInt("total");
                if (total == 0) {
                    return 0;
                }
                return (rs.getDouble("completed") / total) * 100;
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate lab completion rate: " + e.getMessage(), e);
        }
    }

    private int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM lab_orders WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count lab orders: " + e.getMessage(), e);
        }
    }

    public void add(LabOrder order) {
        String sql = "INSERT INTO lab_orders (patient_id, doctor_id, test_id, status, result_notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order.getPatientId());
            if (order.getDoctorId() != null) {
                stmt.setInt(2, order.getDoctorId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setInt(3, order.getTestId());
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getResultNotes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add lab order: " + e.getMessage(), e);
        }
    }

    public void update(LabOrder order) {
        String sql = "UPDATE lab_orders SET status = ?, result_value = ?, result_notes = ?, completed_date = ? "
                + "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, order.getStatus());
            stmt.setString(2, order.getResultValue());
            stmt.setString(3, order.getResultNotes());
            stmt.setString(4, order.getCompletedDate());
            stmt.setInt(5, order.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update lab order: " + e.getMessage(), e);
        }
    }

    public void complete(int orderId, String resultValue, String resultNotes) {
        LabOrder order = new LabOrder(orderId, 0, null, null, null, 0, null,
                "COMPLETED", resultValue, resultNotes, null,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        update(order);
    }

    public void delete(int orderId) {
        String sql = "DELETE FROM lab_orders WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete lab order: " + e.getMessage(), e);
        }
    }

    private List<LabOrder> query(String sql, Object... params) {
        List<LabOrder> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else {
                    stmt.setString(i + 1, param.toString());
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch lab orders: " + e.getMessage(), e);
        }
        return orders;
    }

    private LabOrder mapRow(ResultSet rs) throws SQLException {
        int doctorIdRaw = rs.getInt("doctor_id");
        Integer doctorId = rs.wasNull() ? null : doctorIdRaw;

        return new LabOrder(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                doctorId,
                rs.getString("doctor_name"),
                rs.getInt("test_id"),
                rs.getString("test_name"),
                rs.getString("status"),
                rs.getString("result_value"),
                rs.getString("result_notes"),
                rs.getString("ordered_date"),
                rs.getString("completed_date")
        );
    }
}
