package controllers;

import database.DoctorDAO;
import database.NurseDAO;
import database.PatientDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import models.User;
import utils.SceneManager;
import utils.Session;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    // ── Top bar ──
    @FXML private Label greetingLabel;
    @FXML private Label dateLabel;

    // ── KPI cards (only the ones backed by real tables) ──
    @FXML private Label totalPatientsValue;

    // ── Quick stats ──
    @FXML private Label doctorsOnDutyValue;
    @FXML private Label nursesOnShiftValue;

    // ── User footer ──
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    // ── Sidebar nav items ──
    @FXML private HBox dashboardNav;
    @FXML private HBox patientsNav;
    @FXML private HBox appointmentsNav;
    @FXML private HBox doctorsNav;
    @FXML private HBox departmentsNav;
    @FXML private HBox wardManagementNav;
    @FXML private HBox laboratoryNav;
    @FXML private HBox pharmacyNav;
    @FXML private HBox billingNav;
    @FXML private HBox reportsNav;
    @FXML private HBox settingsNav;

    private static final String ACTIVE_STYLE =
            "-fx-background-color: #7CC8F8; -fx-background-radius: 8; -fx-padding: 9 10 9 10; -fx-cursor: hand;";
    private static final String INACTIVE_STYLE =
            "-fx-padding: 9 10 9 10; -fx-cursor: hand; -fx-background-radius: 8;";

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final NurseDAO nurseDAO = new NurseDAO();

    @FXML
    public void initialize() {
        loadUserInfo();
        loadLiveStats();
        setActiveNav(dashboardNav);
        wireNavHandlers();
    }

    private void loadUserInfo() {
        User user = Session.getCurrentUser();
        if (user == null) {
            return;
        }

        String firstName = user.getFullName().split(" ")[0];
        if (greetingLabel != null) {
            greetingLabel.setText(timeBasedGreeting() + ", " + firstName + " \uD83D\uDC4B");
        }
        if (userNameLabel != null) {
            userNameLabel.setText(user.getFullName());
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText(formatRole(user.getRole()));
        }
        if (dateLabel != null) {
            dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        }
    }

    private void loadLiveStats() {
        if (totalPatientsValue != null) {
            totalPatientsValue.setText(String.valueOf(patientDAO.countAll()));
        }
        if (doctorsOnDutyValue != null) {
            doctorsOnDutyValue.setText(String.valueOf(doctorDAO.countAll()));
        }
        if (nursesOnShiftValue != null) {
            nursesOnShiftValue.setText(String.valueOf(nurseDAO.countAll()));
        }
    }

    private String timeBasedGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    private String formatRole(String role) {
        switch (role) {
            case "ADMIN": return "Chief Administrator";
            case "DOCTOR": return "Doctor";
            case "NURSE": return "Nurse";
            default: return role;
        }
    }

    private void wireNavHandlers() {
        setNavAction(dashboardNav, () -> { /* already here */ });
        setNavAction(patientsNav, () -> SceneManager.switchTo("/views/patients.fxml"));
        setNavAction(doctorsNav, () -> SceneManager.switchTo("/views/doctors.fxml"));
        setNavAction(billingNav, () -> SceneManager.switchTo("/views/billing.fxml"));
        setNavAction(appointmentsNav, () -> SceneManager.switchTo("/views/coming_soon.fxml"));
        setNavAction(departmentsNav, () -> SceneManager.switchTo("/views/coming_soon.fxml"));
        setNavAction(wardManagementNav, () -> SceneManager.switchTo("/views/coming_soon.fxml"));
        setNavAction(laboratoryNav, () -> SceneManager.switchTo("/views/coming_soon.fxml"));
        setNavAction(pharmacyNav, () -> SceneManager.switchTo("/views/coming_soon.fxml"));
        setNavAction(reportsNav, () -> SceneManager.switchTo("/views/coming_soon.fxml"));
        setNavAction(settingsNav, () -> SceneManager.switchTo("/views/coming_soon.fxml"));
    }

    private void setNavAction(HBox navItem, Runnable action) {
        if (navItem == null) {
            return;
        }
        navItem.setOnMouseClicked(event -> action.run());
    }

    private void setActiveNav(HBox activeItem) {
        HBox[] allNavItems = {
                dashboardNav, patientsNav, appointmentsNav, doctorsNav, departmentsNav,
                wardManagementNav, laboratoryNav, pharmacyNav, billingNav, reportsNav, settingsNav
        };
        for (HBox item : allNavItems) {
            if (item == null) {
                continue;
            }
            item.setStyle(item == activeItem ? ACTIVE_STYLE : INACTIVE_STYLE);
        }
    }
}