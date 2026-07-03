package models;

public class Patient {

    private int id;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String phone;
    private String address;
    private String bloodGroup;
    private Integer assignedDoctorId;
    private String assignedDoctorName; // populated by JOIN, not a real column
    private String ward;
    private String status;
    private String admittedDate;
    private String dischargedDate;
    private String notes;

    public Patient(int id, String fullName, String dateOfBirth, String gender, String phone,
                   String address, String bloodGroup, Integer assignedDoctorId, String assignedDoctorName,
                   String ward, String status, String admittedDate, String dischargedDate, String notes) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.bloodGroup = bloodGroup;
        this.assignedDoctorId = assignedDoctorId;
        this.assignedDoctorName = assignedDoctorName;
        this.ward = ward;
        this.status = status;
        this.admittedDate = admittedDate;
        this.dischargedDate = dischargedDate;
        this.notes = notes;
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Integer getAssignedDoctorId() {
        return assignedDoctorId;
    }

    public void setAssignedDoctorId(Integer assignedDoctorId) {
        this.assignedDoctorId = assignedDoctorId;
    }

    public String getAssignedDoctorName() {
        return assignedDoctorName;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdmittedDate() {
        return admittedDate;
    }

    public String getDischargedDate() {
        return dischargedDate;
    }

    public void setDischargedDate(String dischargedDate) {
        this.dischargedDate = dischargedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return fullName;
    }
}