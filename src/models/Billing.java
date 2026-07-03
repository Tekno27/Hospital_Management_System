package models;

public class Billing {

    private int id;
    private int patientId;
    private String patientName; // populated by JOIN, not a real column
    private String description;
    private double amount;
    private String status;
    private double amountPaid;
    private String billDate;
    private String dueDate;

    public Billing(int id, int patientId, String patientName, String description, double amount,
                   String status, double amountPaid, String billDate, String dueDate) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.amountPaid = amountPaid;
        this.billDate = billDate;
        this.dueDate = dueDate;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getBalance() {
        return amount - amountPaid;
    }

    public String getBillDate() {
        return billDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}