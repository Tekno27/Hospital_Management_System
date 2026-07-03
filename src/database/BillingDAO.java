package database;

import models.Billing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BillingDAO {

    private static final String SELECT_BASE =
            "SELECT b.id, b.patient_id, p.full_name AS patient_name, b.description, b.amount, "
                    + "b.status, b.amount_paid, b.bill_date, b.due_date "
                    + "FROM billing b JOIN patients p ON b.patient_id = p.id ";

    public List<Billing> getAll() {
        List<Billing> bills = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY b.bill_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bills.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bills: " + e.getMessage(), e);
        }
        return bills;
    }

    public double getTotalOutstanding() {
        String sql = "SELECT SUM(amount - amount_paid) FROM billing WHERE status != 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate outstanding balance: " + e.getMessage(), e);
        }
    }

    public void add(Billing billing) {
        String sql = "INSERT INTO billing (patient_id, description, amount, status, amount_paid, due_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, billing.getPatientId());
            stmt.setString(2, billing.getDescription());
            stmt.setDouble(3, billing.getAmount());
            stmt.setString(4, billing.getStatus());
            stmt.setDouble(5, billing.getAmountPaid());
            stmt.setString(6, billing.getDueDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add bill: " + e.getMessage(), e);
        }
    }

    public void update(Billing billing) {
        String sql = "UPDATE billing SET description = ?, amount = ?, status = ?, amount_paid = ?, due_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, billing.getDescription());
            stmt.setDouble(2, billing.getAmount());
            stmt.setString(3, billing.getStatus());
            stmt.setDouble(4, billing.getAmountPaid());
            stmt.setString(5, billing.getDueDate());
            stmt.setInt(6, billing.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update bill: " + e.getMessage(), e);
        }
    }

    public void delete(int billingId) {
        String sql = "DELETE FROM billing WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billingId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete bill: " + e.getMessage(), e);
        }
    }

    private Billing mapRow(ResultSet rs) throws SQLException {
        return new Billing(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getString("description"),
                rs.getDouble("amount"),
                rs.getString("status"),
                rs.getDouble("amount_paid"),
                rs.getString("bill_date"),
                rs.getString("due_date")
        );
    }
}