package database;

import models.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    private static final String SELECT_BASE =
            "SELECT p.id, p.full_name, p.date_of_birth, p.gender, p.phone, p.address, p.blood_group, "
                    + "p.assigned_doctor_id, d.full_name AS doctor_name, p.ward, p.status, "
                    + "p.admitted_date, p.discharged_date, p.notes "
                    + "FROM patients p LEFT JOIN doctors d ON p.assigned_doctor_id = d.id ";

    public List<Patient> getAll() {
        List<Patient> patients = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY p.full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch patients: " + e.getMessage(), e);
        }
        return patients;
    }

    public List<Patient> search(String keyword) {
        List<Patient> patients = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE p.full_name LIKE ? OR p.phone LIKE ? ORDER BY p.full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search patients: " + e.getMessage(), e);
        }
        return patients;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count patients: " + e.getMessage(), e);
        }
    }

    public int countByDoctorId(int doctorId) {
        String sql = "SELECT COUNT(*) FROM patients WHERE assigned_doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count patients by doctor: " + e.getMessage(), e);
        }
    }

    public int countByWard(String ward) {
        String sql = "SELECT COUNT(*) FROM patients WHERE ward = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ward);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count patients by ward: " + e.getMessage(), e);
        }
    }

    public int countAdmitted() {
        String sql = "SELECT COUNT(*) FROM patients WHERE status = 'ADMITTED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count admitted patients: " + e.getMessage(), e);
        }
    }

    public List<Patient> getByDoctorId(int doctorId) {
        return queryWithFilter("p.assigned_doctor_id = ?", doctorId);
    }

    public List<Patient> getByWard(String ward) {
        return queryWithFilter("p.ward = ?", ward);
    }

    public List<Patient> searchForDoctor(String keyword, int doctorId) {
        List<Patient> patients = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE p.assigned_doctor_id = ? AND (p.full_name LIKE ? OR p.phone LIKE ?) "
                + "ORDER BY p.full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setInt(1, doctorId);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search patients: " + e.getMessage(), e);
        }
        return patients;
    }

    public List<Patient> searchForWard(String keyword, String ward) {
        List<Patient> patients = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE p.ward = ? AND (p.full_name LIKE ? OR p.phone LIKE ?) ORDER BY p.full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, ward);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search patients: " + e.getMessage(), e);
        }
        return patients;
    }

    public List<WardOccupancy> getWardOccupancy() {
        List<WardOccupancy> result = new ArrayList<>();
        String sql = "SELECT ward, COUNT(*) AS occupied FROM patients WHERE status = 'ADMITTED' "
                + "AND ward IS NOT NULL AND ward != '' GROUP BY ward ORDER BY occupied DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new WardOccupancy(rs.getString("ward"), rs.getInt("occupied")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch ward occupancy: " + e.getMessage(), e);
        }
        return result;
    }

    private List<Patient> queryWithFilter(String condition, Object param) {
        List<Patient> patients = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE " + condition + " ORDER BY p.full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param instanceof Integer) {
                stmt.setInt(1, (Integer) param);
            } else {
                stmt.setString(1, param.toString());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch patients: " + e.getMessage(), e);
        }
        return patients;
    }

    public static class WardOccupancy {
        private final String ward;
        private final int occupied;

        public WardOccupancy(String ward, int occupied) {
            this.ward = ward;
            this.occupied = occupied;
        }

        public String getWard() {
            return ward;
        }

        public int getOccupied() {
            return occupied;
        }
    }

    public void add(Patient patient) {
        String sql = "INSERT INTO patients (full_name, date_of_birth, gender, phone, address, blood_group, "
                + "assigned_doctor_id, ward, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, patient.getFullName());
            stmt.setString(2, patient.getDateOfBirth());
            stmt.setString(3, patient.getGender());
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getBloodGroup());
            setNullableInt(stmt, 7, patient.getAssignedDoctorId());
            stmt.setString(8, patient.getWard());
            stmt.setString(9, patient.getStatus());
            stmt.setString(10, patient.getNotes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add patient: " + e.getMessage(), e);
        }
    }

    public void update(Patient patient) {
        String sql = "UPDATE patients SET full_name = ?, date_of_birth = ?, gender = ?, phone = ?, address = ?, "
                + "blood_group = ?, assigned_doctor_id = ?, ward = ?, status = ?, notes = ?, discharged_date = ? "
                + "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getFullName());
            stmt.setString(2, patient.getDateOfBirth());
            stmt.setString(3, patient.getGender());
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getBloodGroup());
            setNullableInt(stmt, 7, patient.getAssignedDoctorId());
            stmt.setString(8, patient.getWard());
            stmt.setString(9, patient.getStatus());
            stmt.setString(10, patient.getNotes());
            stmt.setString(11, patient.getDischargedDate());
            stmt.setInt(12, patient.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update patient: " + e.getMessage(), e);
        }
    }

    public void delete(int patientId) {
        String sql = "DELETE FROM patients WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete patient: " + e.getMessage(), e);
        }
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, java.sql.Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        int doctorIdRaw = rs.getInt("assigned_doctor_id");
        Integer doctorId = rs.wasNull() ? null : doctorIdRaw;

        return new Patient(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("date_of_birth"),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("blood_group"),
                doctorId,
                rs.getString("doctor_name"),
                rs.getString("ward"),
                rs.getString("status"),
                rs.getString("admitted_date"),
                rs.getString("discharged_date"),
                rs.getString("notes")
        );
    }
}