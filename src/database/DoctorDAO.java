package database;

import models.Doctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public List<Doctor> getAll() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT id, full_name, specialization, phone, email, department FROM doctors ORDER BY full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                doctors.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch doctors: " + e.getMessage(), e);
        }
        return doctors;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM doctors";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count doctors: " + e.getMessage(), e);
        }
    }

    public void add(Doctor doctor) {
        String sql = "INSERT INTO doctors (full_name, specialization, phone, email, department) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, doctor.getFullName());
            stmt.setString(2, doctor.getSpecialization());
            stmt.setString(3, doctor.getPhone());
            stmt.setString(4, doctor.getEmail());
            stmt.setString(5, doctor.getDepartment());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add doctor: " + e.getMessage(), e);
        }
    }

    public void update(Doctor doctor) {
        String sql = "UPDATE doctors SET full_name = ?, specialization = ?, phone = ?, email = ?, department = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctor.getFullName());
            stmt.setString(2, doctor.getSpecialization());
            stmt.setString(3, doctor.getPhone());
            stmt.setString(4, doctor.getEmail());
            stmt.setString(5, doctor.getDepartment());
            stmt.setInt(6, doctor.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update doctor: " + e.getMessage(), e);
        }
    }

    public void delete(int doctorId) {
        String sql = "DELETE FROM doctors WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete doctor: " + e.getMessage(), e);
        }
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("specialization"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("department")
        );
    }
}