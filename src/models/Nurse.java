package models;

public class Nurse {

    private int id;
    private String fullName;
    private String phone;
    private String email;
    private String ward;
    private String shift;

    public Nurse(int id, String fullName, String phone, String email, String ward, String shift) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.ward = ward;
        this.shift = shift;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
}
