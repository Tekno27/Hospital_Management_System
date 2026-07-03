package models;

/**
 * Represents a row in the users table - an account that can log in.
 * role is one of "ADMIN", "DOCTOR", "NURSE".
 * linkedStaffId points to a row in doctors or nurses, depending on role
 * (it is -1/null for ADMIN, who has no staff record).
 */
public class User {

    private int id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private Integer linkedStaffId;

    public User(int id, String username, String password, String role, String fullName, Integer linkedStaffId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.linkedStaffId = linkedStaffId;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public Integer getLinkedStaffId() {
        return linkedStaffId;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isDoctor() {
        return "DOCTOR".equals(role);
    }

    public boolean isNurse() {
        return "NURSE".equals(role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}