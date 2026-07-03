package models;

public class Department {

    private int id;
    private String name;
    private String floor;
    private Integer headDoctorId;
    private String headDoctorName;
    private int bedCapacity;
    private String phone;
    private String description;
    private int doctorCount;
    private int patientCount;

    public Department(int id, String name, String floor, Integer headDoctorId, String headDoctorName,
                      int bedCapacity, String phone, String description, int doctorCount, int patientCount) {
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.headDoctorId = headDoctorId;
        this.headDoctorName = headDoctorName;
        this.bedCapacity = bedCapacity;
        this.phone = phone;
        this.description = description;
        this.doctorCount = doctorCount;
        this.patientCount = patientCount;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getFloor() { return floor; }
    public Integer getHeadDoctorId() { return headDoctorId; }
    public String getHeadDoctorName() { return headDoctorName; }
    public int getBedCapacity() { return bedCapacity; }
    public String getPhone() { return phone; }
    public String getDescription() { return description; }
    public int getDoctorCount() { return doctorCount; }
    public int getPatientCount() { return patientCount; }

    public void setName(String name) { this.name = name; }
    public void setFloor(String floor) { this.floor = floor; }
    public void setHeadDoctorId(Integer headDoctorId) { this.headDoctorId = headDoctorId; }
    public void setBedCapacity(int bedCapacity) { this.bedCapacity = bedCapacity; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDescription(String description) { this.description = description; }
}
