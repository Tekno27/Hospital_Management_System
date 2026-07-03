package controllers;

import database.BillingDAO;
import database.LabOrderDAO;
import database.MedicineDAO;
import database.PatientDAO;
import database.PrescriptionDAO;
import database.ReportsDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.LabOrder;
import models.User;
import utils.AuthGuard;
import utils.AppSettings;
import utils.PatientAccess;
import utils.SceneManager;
import utils.Session;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label greetingLabel;
    @FXML private Label dateLabel;
    @FXML private TextField globalSearchField;

    @FXML private HBox kpiRow;
    @FXML private Label kpi1Value;
    @FXML private Label kpi1Title;
    @FXML private Label kpi2Value;
    @FXML private Label kpi2Title;
    @FXML private Label kpi3Value;
    @FXML private Label kpi3Title;
    @FXML private Label kpi4Value;
    @FXML private Label kpi4Title;

    @FXML private Label activityPanelTitle;
    @FXML private VBox activityList;
    @FXML private VBox wardOccupancyList;
    @FXML private Label stat1Value;
    @FXML private Label stat1Title;
    @FXML private Label stat2Value;
    @FXML private Label stat2Title;
    @FXML private Label stat3Value;
    @FXML private Label stat3Title;
    @FXML private Label stat4Value;
    @FXML private Label stat4Title;

    @FXML private PieChart patientStatusChart;
    @FXML private BarChart<String, Number> wardBarChart;

    private final PatientDAO patientDAO = new PatientDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final LabOrderDAO labOrderDAO = new LabOrderDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final BillingDAO billingDAO = new BillingDAO();
    private final ReportsDAO reportsDAO = new ReportsDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireLogin()) {
            return;
        }
        loadUserInfo();
        loadDashboardData();
    }

    private void loadUserInfo() {
        User user = Session.getCurrentUser();
        if (user == null) {
            return;
        }
        String firstName = user.getFullName().split(" ")[0];
        if (greetingLabel != null) {
            greetingLabel.setText(timeBasedGreeting() + ", " + firstName);
        }
        if (dateLabel != null) {
            dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        }
    }

    private void loadDashboardData() {
        User user = Session.getCurrentUser();
        if (user == null) {
            return;
        }
        try {
            int patientCount = PatientAccess.countVisiblePatients();
            loadKpis(user, patientCount);
            loadCharts();
            loadRecentActivity(user);
            loadWardOccupancy();
            loadQuickStats(user);
        } catch (RuntimeException e) {
            System.err.println("Dashboard stats could not be loaded: " + e.getMessage());
        }
    }

    private void loadKpis(User user, int patientCount) {
        if (kpi1Value != null) {
            kpi1Value.setText(String.valueOf(patientCount));
            kpi1Title.setText(user.isDoctor() ? "My Patients" : user.isNurse() ? "Ward Patients" : "Total Patients");
        }
        int pendingRx = pendingPrescriptions(user);
        if (kpi2Value != null) {
            kpi2Value.setText(String.valueOf(pendingRx));
        }
        int pendingLabs = pendingLabOrders(user);
        if (kpi3Value != null) {
            kpi3Value.setText(String.valueOf(pendingLabs));
        }
        if (kpi4Value != null) {
            if (user.isAdmin()) {
                kpi4Value.setText(String.format("GHS %.0f", billingDAO.getTotalOutstanding()));
                kpi4Title.setText("Outstanding Billing");
            } else if (user.isNurse()) {
                kpi4Value.setText(String.valueOf(patientDAO.countAdmitted()));
                kpi4Title.setText("Admitted Patients");
            } else {
                kpi4Value.setText(String.valueOf(medicineDAO.countLowStock()));
                kpi4Title.setText("Low Stock Medicines");
            }
        }
    }

    private int pendingPrescriptions(User user) {
        if (user.isAdmin()) return prescriptionDAO.countPending();
        if (user.isDoctor() && user.getLinkedStaffId() != null) {
            return prescriptionDAO.countPendingForDoctor(user.getLinkedStaffId());
        }
        if (user.isNurse()) {
            String ward = PatientAccess.nurseWard();
            return ward != null ? prescriptionDAO.countPendingForWard(ward) : 0;
        }
        return 0;
    }

    private int pendingLabOrders(User user) {
        if (user.isAdmin()) return labOrderDAO.countPending();
        if (user.isDoctor() && user.getLinkedStaffId() != null) {
            return labOrderDAO.countPendingForDoctor(user.getLinkedStaffId());
        }
        if (user.isNurse()) {
            String ward = PatientAccess.nurseWard();
            return ward != null ? labOrderDAO.countPendingForWard(ward) : 0;
        }
        return 0;
    }

    private void loadCharts() {
        if (patientStatusChart == null || wardBarChart == null) {
            return;
        }
        AppSettings.load();
        boolean show = AppSettings.getBoolean("showCharts");
        patientStatusChart.setVisible(show);
        patientStatusChart.setManaged(show);
        wardBarChart.setVisible(show);
        wardBarChart.setManaged(show);
        if (!show) {
            return;
        }

        ObservableList<PieChart.Data> patientData = FXCollections.observableArrayList();
        for (var entry : reportsDAO.getPatientStatusBreakdown().entrySet()) {
            patientData.add(new PieChart.Data(formatStatus(entry.getKey()), entry.getValue()));
        }
        patientStatusChart.setData(patientData);

        wardBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Admitted");
        for (var entry : reportsDAO.getWardOccupancyBreakdown().entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        wardBarChart.getData().add(series);
    }

    private String formatStatus(String status) {
        if (status == null) return "Unknown";
        return status.charAt(0) + status.substring(1).toLowerCase();
    }

    private void loadRecentActivity(User user) {
        if (activityList == null) return;
        activityList.getChildren().clear();
        if (activityPanelTitle != null) {
            activityPanelTitle.setText(user.isDoctor() ? "Recent Lab Orders" : "Recent Activity");
        }
        List<LabOrder> orders = labOrderDAO.getRecent(5);
        if (user.isDoctor() && user.getLinkedStaffId() != null) {
            orders = labOrderDAO.getByDoctorId(user.getLinkedStaffId());
            if (orders.size() > 5) orders = orders.subList(0, 5);
        } else if (user.isNurse()) {
            String ward = PatientAccess.nurseWard();
            if (ward != null) {
                orders = labOrderDAO.getByPatientWard(ward);
                if (orders.size() > 5) orders = orders.subList(0, 5);
            }
        }
        if (orders.isEmpty()) {
            activityList.getChildren().add(placeholderLabel("No recent lab orders."));
            return;
        }
        for (LabOrder order : orders) {
            activityList.getChildren().add(buildActivityRow(
                    order.getPatientName(), order.getTestName() + " · " + order.getStatus(),
                    formatShortDate(order.getOrderedDate()), order.getStatus()));
        }
    }

    private void loadWardOccupancy() {
        if (wardOccupancyList == null) return;
        wardOccupancyList.getChildren().clear();
        List<PatientDAO.WardOccupancy> wards = patientDAO.getWardOccupancy();
        if (wards.isEmpty()) {
            wardOccupancyList.getChildren().add(placeholderLabel("No admitted patients in wards."));
            return;
        }
        int max = wards.stream().mapToInt(PatientDAO.WardOccupancy::getOccupied).max().orElse(1);
        for (PatientDAO.WardOccupancy ward : wards) {
            wardOccupancyList.getChildren().add(buildOccupancyRow(ward.getWard(), ward.getOccupied(), max));
        }
    }

    private void loadQuickStats(User user) {
        if (stat1Value != null) {
            stat1Value.setText(String.valueOf(PatientAccess.countVisiblePatients()));
            stat1Title.setText(user.isDoctor() ? "Assigned patients" : user.isNurse() ? "Ward patients" : "All patients");
        }
        if (stat2Value != null) stat2Value.setText(String.valueOf(pendingPrescriptions(user)));
        if (stat3Value != null) stat3Value.setText(String.valueOf(pendingLabOrders(user)));
        if (stat4Value != null) stat4Value.setText(String.format("%.0f%%", labOrderDAO.getCompletionRate()));
    }

    private HBox buildActivityRow(String name, String subtitle, String time, String status) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("activity-row");
        StackPane avatar = new StackPane();
        avatar.setPrefSize(30, 30);
        avatar.getStyleClass().add("activity-avatar");
        Label initials = new Label(utils.Rbac.initials(name));
        initials.getStyleClass().add("activity-initials");
        avatar.getChildren().add(initials);
        VBox info = new VBox(1);
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("activity-name");
        Label subLabel = new Label(subtitle);
        subLabel.getStyleClass().add("activity-sub");
        info.getChildren().addAll(nameLabel, subLabel);
        HBox.setHgrow(info, Priority.ALWAYS);
        VBox right = new VBox(3);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("activity-time");
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add(statusClass(status));
        right.getChildren().addAll(timeLabel, statusLabel);
        row.getChildren().addAll(avatar, info, right);
        return row;
    }

    private VBox buildOccupancyRow(String ward, int occupied, int max) {
        VBox box = new VBox(4);
        HBox header = new HBox();
        Label wardLabel = new Label(ward);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label countLabel = new Label(occupied + " admitted");
        countLabel.getStyleClass().add("occupancy-count");
        header.getChildren().addAll(wardLabel, spacer, countLabel);
        ProgressBar bar = new ProgressBar((double) occupied / Math.max(max, 1));
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(6);
        box.getChildren().addAll(header, bar);
        return box;
    }

    private Label placeholderLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("placeholder-label");
        return label;
    }

    private String statusClass(String status) {
        switch (status) {
            case "COMPLETED": case "DISPENSED":
                return "status-badge-completed";
            case "IN_PROGRESS": case "ORDERED": case "PENDING":
                return "status-badge-pending";
            default:
                return "status-badge-alert";
        }
    }

    private String formatShortDate(String dateTime) {
        if (dateTime == null || dateTime.length() < 10) return "—";
        return dateTime.substring(0, 10);
    }

    @FXML
    private void handleGlobalSearch() {
        String keyword = globalSearchField.getText().trim();
        if (!keyword.isEmpty()) {
            Session.setSearchKeyword(keyword);
        }
        SceneManager.loadInShell("/views/patients.fxml", "patients");
    }

    private String timeBasedGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }
}
