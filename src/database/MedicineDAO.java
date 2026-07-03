package database;

import models.Medicine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    public List<Medicine> getAll() {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT id, name, category, stock_quantity, unit_price, reorder_level, expiry_date "
                + "FROM medicines ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                medicines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch medicines: " + e.getMessage(), e);
        }
        return medicines;
    }

    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM medicines WHERE stock_quantity <= reorder_level";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count low stock medicines: " + e.getMessage(), e);
        }
    }

    public void add(Medicine medicine) {
        String sql = "INSERT INTO medicines (name, category, stock_quantity, unit_price, reorder_level, expiry_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, medicine.getName());
            stmt.setString(2, medicine.getCategory());
            stmt.setInt(3, medicine.getStockQuantity());
            stmt.setDouble(4, medicine.getUnitPrice());
            stmt.setInt(5, medicine.getReorderLevel());
            stmt.setString(6, medicine.getExpiryDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add medicine: " + e.getMessage(), e);
        }
    }

    public void update(Medicine medicine) {
        String sql = "UPDATE medicines SET name = ?, category = ?, stock_quantity = ?, unit_price = ?, "
                + "reorder_level = ?, expiry_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, medicine.getName());
            stmt.setString(2, medicine.getCategory());
            stmt.setInt(3, medicine.getStockQuantity());
            stmt.setDouble(4, medicine.getUnitPrice());
            stmt.setInt(5, medicine.getReorderLevel());
            stmt.setString(6, medicine.getExpiryDate());
            stmt.setInt(7, medicine.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update medicine: " + e.getMessage(), e);
        }
    }

    public void delete(int medicineId) {
        String sql = "DELETE FROM medicines WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, medicineId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete medicine: " + e.getMessage(), e);
        }
    }

    public void reduceStock(int medicineId, int quantity) {
        String sql = "UPDATE medicines SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, medicineId);
            stmt.setInt(3, quantity);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Insufficient stock for medicine id " + medicineId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reduce stock: " + e.getMessage(), e);
        }
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        return new Medicine(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("stock_quantity"),
                rs.getDouble("unit_price"),
                rs.getInt("reorder_level"),
                rs.getString("expiry_date")
        );
    }
}
