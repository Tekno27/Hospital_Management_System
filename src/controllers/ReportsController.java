package controllers;

import database.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.AuthGuard;
import utils.Rbac;

import java.util.Map;

public class ReportsController {

    @FXML private Label totalPatientsLabel;
    @FXML private Label admittedLabel;
    @FXML private Label outstandingLabel;
    @FXML private Label upcomingApptsLabel;
    @FXML private PieChart patientStatusChart;
    @FXML private BarChart<String, Number> wardBarChart;
    @FXML private BarChart<String, Number> billingBarChart;
    @FXML private TableView<ReportRow> summaryTable;
    @FXML private TableColumn<ReportRow, String> colMetric;
    @FXML private TableColumn<ReportRow, String> colValue;

    private final PatientDAO patientDAO = new PatientDAO();
    private final BillingDAO billingDAO = new BillingDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ReportsDAO reportsDAO = new ReportsDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final NurseDAO nurseDAO = new NurseDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final LabOrderDAO labOrderDAO = new LabOrderDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.REPORTS)) return;

        colMetric.setCellValueFactory(new PropertyValueFactory<>("metric"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));

        loadSummary();
        loadCharts();
        loadSummaryTable();
    }

    private void loadSummary() {
        totalPatientsLabel.setText(String.valueOf(patientDAO.countAll()));
        admittedLabel.setText(String.valueOf(patientDAO.countAdmitted()));
        outstandingLabel.setText(String.format("GHS %.2f", billingDAO.getTotalOutstanding()));
        upcomingApptsLabel.setText(String.valueOf(appointmentDAO.countUpcoming()));
    }

    private void loadCharts() {
        patientStatusChart.getData().clear();
        ObservableList<PieChart.Data> patientData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> e : reportsDAO.getPatientStatusBreakdown().entrySet()) {
            patientData.add(new PieChart.Data(formatLabel(e.getKey()), e.getValue()));
        }
        patientStatusChart.setData(patientData);
        patientStatusChart.setLabelsVisible(true);
        patientStatusChart.setLegendVisible(false);

        wardBarChart.getData().clear();
        XYChart.Series<String, Number> wardSeries = new XYChart.Series<>();
        wardSeries.setName("Admitted");
        for (Map.Entry<String, Integer> e : reportsDAO.getWardOccupancyBreakdown().entrySet()) {
            wardSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        wardBarChart.getData().add(wardSeries);

        billingBarChart.getData().clear();
        XYChart.Series<String, Number> billingSeries = new XYChart.Series<>();
        billingSeries.setName("Bills");
        for (Map.Entry<String, Integer> e : reportsDAO.getBillingStatusBreakdown().entrySet()) {
            billingSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        billingBarChart.getData().add(billingSeries);
    }

    private void loadSummaryTable() {
        summaryTable.setItems(FXCollections.observableArrayList(
                new ReportRow("Total doctors", String.valueOf(doctorDAO.getAll().size())),
                new ReportRow("Total nurses", String.valueOf(nurseDAO.getAll().size())),
                new ReportRow("Departments", String.valueOf(departmentDAO.countAll())),
                new ReportRow("Medicines in stock", String.valueOf(medicineDAO.getAll().size())),
                new ReportRow("Pending prescriptions", String.valueOf(prescriptionDAO.countPending())),
                new ReportRow("Pending lab orders", String.valueOf(labOrderDAO.countPending())),
                new ReportRow("Lab completion rate", String.format("%.0f%%", labOrderDAO.getCompletionRate())),
                new ReportRow("Low stock medicines", String.valueOf(medicineDAO.countLowStock()))
        ));
    }

    private String formatLabel(String status) {
        if (status == null) return "Unknown";
        return status.charAt(0) + status.substring(1).toLowerCase().replace('_', ' ');
    }

    public static class ReportRow {
        private final String metric;
        private final String value;

        public ReportRow(String metric, String value) {
            this.metric = metric;
            this.value = value;
        }

        public String getMetric() { return metric; }
        public String getValue() { return value; }
    }
}
