package models;

public class Doctor {

    private int id;
    private String fullName;
    private String specialization;
    private String phone;
    private String email;
    private String department;

    public Doctor(int id, String fullName, String specialization, String phone, String email, String department) {
        this.id = id;
        this.fullName = fullName;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.department = department;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Dr. " + fullName;
    }
}