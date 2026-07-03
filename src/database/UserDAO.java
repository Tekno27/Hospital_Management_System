package database;

import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles all database queries related to the users table (authentication).
 */
public class UserDAO {

    /**
     * Looks up a user by username and password.
     * Returns the matching User if credentials are correct, otherwise null.
     *
     * NOTE: passwords are compared as plain text for now. This will be
     * upgraded to hashed comparison once the login flow is confirmed working.
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT id, username, password, role, full_name, linked_staff_id "
                + "FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login query failed: " + e.getMessage(), e);
        }

        return null;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int linkedStaffIdRaw = rs.getInt("linked_staff_id");
        Integer linkedStaffId = rs.wasNull() ? null : linkedStaffIdRaw;

        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("full_name"),
                linkedStaffId
        );
    }
}