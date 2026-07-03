package models;

public class Appointment {

    private int id;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private String appointmentDate;
    private String appointmentTime;
    private String status;
    private String reason;
    private String notes;

    public Appointment(int id, int patientId, String patientName, int doctorId, String doctorName,
                       String appointmentDate, String appointmentTime, String status,
                       String reason, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.reason = reason;
        this.notes = notes;
    }

    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public int getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getAppointmentTime() { return appointmentTime; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public String getNotes() { return notes; }

    public void setPatientId(int patientId) { this.patientId = patientId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    public void setStatus(String status) { this.status = status; }
    public void setReason(String reason) { this.reason = reason; }
    public void setNotes(String notes) { this.notes = notes; }
}
