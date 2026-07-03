package models;

public class Prescription {

    private int id;
    private int patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private int medicineId;
    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;
    private int quantity;
    private String status;
    private String prescribedDate;
    private String dispensedDate;
    private String notes;

    public Prescription(int id, int patientId, String patientName, Integer doctorId, String doctorName,
                        int medicineId, String medicineName, String dosage, String frequency, String duration,
                        int quantity, String status, String prescribedDate, String dispensedDate, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.quantity = quantity;
        this.status = status;
        this.prescribedDate = prescribedDate;
        this.dispensedDate = dispensedDate;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrescribedDate() {
        return prescribedDate;
    }

    public String getDispensedDate() {
        return dispensedDate;
    }

    public void setDispensedDate(String dispensedDate) {
        this.dispensedDate = dispensedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
