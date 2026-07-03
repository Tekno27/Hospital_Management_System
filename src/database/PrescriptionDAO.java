package database;

import models.Prescription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    private static final String SELECT_BASE =
            "SELECT pr.id, pr.patient_id, p.full_name AS patient_name, pr.doctor_id, d.full_name AS doctor_name, "
                    + "pr.medicine_id, m.name AS medicine_name, pr.dosage, pr.frequency, pr.duration, pr.quantity, "
                    + "pr.status, pr.prescribed_date, pr.dispensed_date, pr.notes "
                    + "FROM prescriptions pr "
                    + "JOIN patients p ON pr.patient_id = p.id "
                    + "JOIN medicines m ON pr.medicine_id = m.id "
                    + "LEFT JOIN doctors d ON pr.doctor_id = d.id ";

    public List<Prescription> getAll() {
        return query(SELECT_BASE + "ORDER BY pr.prescribed_date DESC");
    }

    public List<Prescription> getByDoctorId(int doctorId) {
        return query(SELECT_BASE + "WHERE pr.doctor_id = ? ORDER BY pr.prescribed_date DESC", doctorId);
    }

    public List<Prescription> getByPatientWard(String ward) {
        return query(SELECT_BASE + "WHERE p.ward = ? ORDER BY pr.prescribed_date DESC", ward);
    }

    public int countPending() {
        return countPending(null, null);
    }

    public int countPendingForDoctor(int doctorId) {
        return countPending(doctorId, null);
    }

    public int countPendingForWard(String ward) {
        return countPending(null, ward);
    }

    private int countPending(Integer doctorId, String ward) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM prescriptions pr JOIN patients p ON pr.patient_id = p.id "
                        + "WHERE pr.status = 'PENDING'");
        if (doctorId != null) {
            sql.append(" AND pr.doctor_id = ?");
        }
        if (ward != null) {
            sql.append(" AND p.ward = ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (doctorId != null) {
                stmt.setInt(idx++, doctorId);
            }
            if (ward != null) {
                stmt.setString(idx, ward);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count pending prescriptions: " + e.getMessage(), e);
        }
    }

    public List<Prescription> getRecent(int limit) {
        return query(SELECT_BASE + "ORDER BY pr.prescribed_date DESC LIMIT " + limit);
    }

    public void add(Prescription prescription) {
        String sql = "INSERT INTO prescriptions (patient_id, doctor_id, medicine_id, dosage, frequency, duration, "
                + "quantity, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescription.getPatientId());
            if (prescription.getDoctorId() != null) {
                stmt.setInt(2, prescription.getDoctorId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setInt(3, prescription.getMedicineId());
            stmt.setString(4, prescription.getDosage());
            stmt.setString(5, prescription.getFrequency());
            stmt.setString(6, prescription.getDuration());
            stmt.setInt(7, prescription.getQuantity());
            stmt.setString(8, prescription.getStatus());
            stmt.setString(9, prescription.getNotes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add prescription: " + e.getMessage(), e);
        }
    }

    public void update(Prescription prescription) {
        String sql = "UPDATE prescriptions SET dosage = ?, frequency = ?, duration = ?, quantity = ?, status = ?, "
                + "dispensed_date = ?, notes = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prescription.getDosage());
            stmt.setString(2, prescription.getFrequency());
            stmt.setString(3, prescription.getDuration());
            stmt.setInt(4, prescription.getQuantity());
            stmt.setString(5, prescription.getStatus());
            stmt.setString(6, prescription.getDispensedDate());
            stmt.setString(7, prescription.getNotes());
            stmt.setInt(8, prescription.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update prescription: " + e.getMessage(), e);
        }
    }

    public void dispense(int prescriptionId, int medicineId, int quantity) {
        MedicineDAO medicineDAO = new MedicineDAO();
        medicineDAO.reduceStock(medicineId, quantity);

        String sql = "UPDATE prescriptions SET status = 'DISPENSED', dispensed_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.setInt(2, prescriptionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to dispense prescription: " + e.getMessage(), e);
        }
    }

    public void delete(int prescriptionId) {
        String sql = "DELETE FROM prescriptions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prescriptionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete prescription: " + e.getMessage(), e);
        }
    }

    private List<Prescription> query(String sql, Object... params) {
        List<Prescription> prescriptions = new ArrayList<>();
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
                    prescriptions.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch prescriptions: " + e.getMessage(), e);
        }
        return prescriptions;
    }

    private Prescription mapRow(ResultSet rs) throws SQLException {
        int doctorIdRaw = rs.getInt("doctor_id");
        Integer doctorId = rs.wasNull() ? null : doctorIdRaw;

        return new Prescription(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                doctorId,
                rs.getString("doctor_name"),
                rs.getInt("medicine_id"),
                rs.getString("medicine_name"),
                rs.getString("dosage"),
                rs.getString("frequency"),
                rs.getString("duration"),
                rs.getInt("quantity"),
                rs.getString("status"),
                rs.getString("prescribed_date"),
                rs.getString("dispensed_date"),
                rs.getString("notes")
        );
    }
}
