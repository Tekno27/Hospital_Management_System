package database;

import models.LabTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LabTestDAO {

    public List<LabTest> getAll() {
        List<LabTest> tests = new ArrayList<>();
        String sql = "SELECT id, name, category, price, normal_range FROM lab_tests ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tests.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch lab tests: " + e.getMessage(), e);
        }
        return tests;
    }

    public void add(LabTest test) {
        String sql = "INSERT INTO lab_tests (name, category, price, normal_range) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, test.getName());
            stmt.setString(2, test.getCategory());
            stmt.setDouble(3, test.getPrice());
            stmt.setString(4, test.getNormalRange());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add lab test: " + e.getMessage(), e);
        }
    }

    public void update(LabTest test) {
        String sql = "UPDATE lab_tests SET name = ?, category = ?, price = ?, normal_range = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, test.getName());
            stmt.setString(2, test.getCategory());
            stmt.setDouble(3, test.getPrice());
            stmt.setString(4, test.getNormalRange());
            stmt.setInt(5, test.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update lab test: " + e.getMessage(), e);
        }
    }

    public void delete(int testId) {
        String sql = "DELETE FROM lab_tests WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, testId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete lab test: " + e.getMessage(), e);
        }
    }

    private LabTest mapRow(ResultSet rs) throws SQLException {
        return new LabTest(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getString("normal_range")
        );
    }
}
