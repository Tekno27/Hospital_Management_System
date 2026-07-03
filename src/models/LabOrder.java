package models;

public class LabOrder {

    private int id;
    private int patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private int testId;
    private String testName;
    private String status;
    private String resultValue;
    private String resultNotes;
    private String orderedDate;
    private String completedDate;

    public LabOrder(int id, int patientId, String patientName, Integer doctorId, String doctorName,
                    int testId, String testName, String status, String resultValue, String resultNotes,
                    String orderedDate, String completedDate) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.testId = testId;
        this.testName = testName;
        this.status = status;
        this.resultValue = resultValue;
        this.resultNotes = resultNotes;
        this.orderedDate = orderedDate;
        this.completedDate = completedDate;
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

    public int getTestId() {
        return testId;
    }

    public String getTestName() {
        return testName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getResultNotes() {
        return resultNotes;
    }

    public void setResultNotes(String resultNotes) {
        this.resultNotes = resultNotes;
    }

    public String getOrderedDate() {
        return orderedDate;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }
}
