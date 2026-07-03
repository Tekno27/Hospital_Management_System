package database;

import models.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private static final String SELECT_BASE =
            "SELECT a.id, a.patient_id, p.full_name AS patient_name, a.doctor_id, d.full_name AS doctor_name, "
                    + "a.appointment_date, a.appointment_time, a.status, a.reason, a.notes "
                    + "FROM appointments a "
                    + "JOIN patients p ON a.patient_id = p.id "
                    + "JOIN doctors d ON a.doctor_id = d.id ";

    public List<Appointment> getAll() {
        return query(SELECT_BASE + "ORDER BY a.appointment_date DESC, a.appointment_time DESC");
    }

    public List<Appointment> getByDoctorId(int doctorId) {
        return query(SELECT_BASE + "WHERE a.doctor_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC", doctorId);
    }

    public List<Appointment> search(String keyword) {
        String sql = SELECT_BASE + "WHERE p.full_name LIKE ? OR d.full_name LIKE ? OR a.reason LIKE ? "
                + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        List<Appointment> results = new ArrayList<>();
        String pattern = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search appointments: " + e.getMessage(), e);
        }
        return results;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count appointments: " + e.getMessage(), e);
        }
    }

    public int countUpcoming() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE status = 'SCHEDULED' AND appointment_date >= date('now')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count upcoming appointments: " + e.getMessage(), e);
        }
    }

    public void add(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, reason, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());
            stmt.setString(3, appointment.getAppointmentDate());
            stmt.setString(4, appointment.getAppointmentTime());
            stmt.setString(5, appointment.getStatus() != null ? appointment.getStatus() : "SCHEDULED");
            stmt.setString(6, appointment.getReason());
            stmt.setString(7, appointment.getNotes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add appointment: " + e.getMessage(), e);
        }
    }

    public void update(Appointment appointment) {
        String sql = "UPDATE appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, "
                + "appointment_time = ?, status = ?, reason = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());
            stmt.setString(3, appointment.getAppointmentDate());
            stmt.setString(4, appointment.getAppointmentTime());
            stmt.setString(5, appointment.getStatus());
            stmt.setString(6, appointment.getReason());
            stmt.setString(7, appointment.getNotes());
            stmt.setInt(8, appointment.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update appointment: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM appointments WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete appointment: " + e.getMessage(), e);
        }
    }

    private List<Appointment> query(String sql, Object... params) {
        List<Appointment> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch appointments: " + e.getMessage(), e);
        }
        return results;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getInt("doctor_id"),
                rs.getString("doctor_name"),
                rs.getString("appointment_date"),
                rs.getString("appointment_time"),
                rs.getString("status"),
                rs.getString("reason"),
                rs.getString("notes")
        );
    }
}
