package database;

import models.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAll() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT dep.id, dep.name, dep.floor, dep.head_doctor_id, d.full_name AS head_doctor_name, "
                + "dep.bed_capacity, dep.phone, dep.description, "
                + "(SELECT COUNT(*) FROM doctors doc WHERE doc.department = dep.name) AS doctor_count, "
                + "(SELECT COUNT(*) FROM patients pat WHERE pat.status = 'ADMITTED' "
                + "AND pat.ward IN (SELECT w.name FROM wards w WHERE w.department = dep.name)) AS patient_count "
                + "FROM departments dep LEFT JOIN doctors d ON dep.head_doctor_id = d.id "
                + "ORDER BY dep.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                departments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch departments: " + e.getMessage(), e);
        }
        return departments;
    }

    public List<Department> search(String keyword) {
        List<Department> all = getAll();
        String lower = keyword.toLowerCase();
        return all.stream()
                .filter(d -> d.getName().toLowerCase().contains(lower)
                        || (d.getFloor() != null && d.getFloor().toLowerCase().contains(lower))
                        || (d.getDescription() != null && d.getDescription().toLowerCase().contains(lower)))
                .toList();
    }

    public void add(Department department) {
        String sql = "INSERT INTO departments (name, floor, head_doctor_id, bed_capacity, phone, description) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, department.getName());
            stmt.setString(2, department.getFloor());
            setNullableInt(stmt, 3, department.getHeadDoctorId());
            stmt.setInt(4, department.getBedCapacity());
            stmt.setString(5, department.getPhone());
            stmt.setString(6, department.getDescription());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add department: " + e.getMessage(), e);
        }
    }

    public void update(Department department) {
        String sql = "UPDATE departments SET name = ?, floor = ?, head_doctor_id = ?, bed_capacity = ?, "
                + "phone = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, department.getName());
            stmt.setString(2, department.getFloor());
            setNullableInt(stmt, 3, department.getHeadDoctorId());
            stmt.setInt(4, department.getBedCapacity());
            stmt.setString(5, department.getPhone());
            stmt.setString(6, department.getDescription());
            stmt.setInt(7, department.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update department: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM departments WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete department: " + e.getMessage(), e);
        }
    }

    public int countAll() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM departments");
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count departments: " + e.getMessage(), e);
        }
    }

    private Department mapRow(ResultSet rs) throws SQLException {
        int headIdRaw = rs.getInt("head_doctor_id");
        Integer headId = rs.wasNull() ? null : headIdRaw;
        return new Department(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("floor"),
                headId,
                rs.getString("head_doctor_name"),
                rs.getInt("bed_capacity"),
                rs.getString("phone"),
                rs.getString("description"),
                rs.getInt("doctor_count"),
                rs.getInt("patient_count")
        );
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, java.sql.Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }
}
