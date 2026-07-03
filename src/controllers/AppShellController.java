package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import models.User;
import utils.AuthGuard;
import utils.PatientAccess;
import utils.Rbac;
import utils.Session;

import java.io.IOException;

public class AppShellController {

    private static AppShellController instance;

    @FXML private Label portalLabel;
    @FXML private Label patientCountBadge;
    @FXML private Label avatarLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private StackPane contentPane;

    @FXML private HBox dashboardNav;
    @FXML private HBox patientsNav;
    @FXML private HBox nursesNav;
    @FXML private HBox doctorsNav;
    @FXML private HBox appointmentsNav;
    @FXML private HBox departmentsNav;
    @FXML private HBox wardManagementNav;
    @FXML private HBox laboratoryNav;
    @FXML private HBox pharmacyNav;
    @FXML private HBox billingNav;
    @FXML private HBox reportsNav;
    @FXML private HBox settingsNav;
    @FXML private HBox userProfileSection;

    private HBox activeNav;

    public static AppShellController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        if (!AuthGuard.requireLogin()) {
            return;
        }
        refreshSidebar();
        applyRbacNav();
        wireNavHandlers();
        setActiveNav(dashboardNav);
    }

    public void loadContent(String fxmlPath, String navKey) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentPane.getChildren().setAll(content);
            selectNavByKey(navKey);
            refreshSidebar();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load content: " + fxmlPath, e);
        }
    }

    private void refreshSidebar() {
        User user = Session.getCurrentUser();
        if (user == null) {
            return;
        }
        if (portalLabel != null) {
            portalLabel.setText(Rbac.portalLabel());
        }
        if (patientCountBadge != null) {
            patientCountBadge.setText(String.valueOf(PatientAccess.countVisiblePatients()));
        }
        if (avatarLabel != null) {
            avatarLabel.setText(Rbac.initials(user.getFullName()));
        }
        if (userNameLabel != null) {
            userNameLabel.setText(user.getFullName());
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText(formatRole(user.getRole()));
        }
    }

    private void applyRbacNav() {
        setNavVisible(patientsNav, Rbac.canAccess(Rbac.Module.PATIENTS));
        setNavVisible(nursesNav, Rbac.canAccess(Rbac.Module.NURSES));
        setNavVisible(doctorsNav, Rbac.canAccess(Rbac.Module.DOCTORS));
        setNavVisible(billingNav, Rbac.canAccess(Rbac.Module.BILLING));
        setNavVisible(pharmacyNav, Rbac.canAccess(Rbac.Module.PHARMACY));
        setNavVisible(laboratoryNav, Rbac.canAccess(Rbac.Module.LABORATORY));
        setNavVisible(appointmentsNav, Rbac.canAccess(Rbac.Module.APPOINTMENTS));
        setNavVisible(departmentsNav, Rbac.canAccess(Rbac.Module.DEPARTMENTS));
        setNavVisible(wardManagementNav, Rbac.canAccess(Rbac.Module.WARD));
        setNavVisible(reportsNav, Rbac.canAccess(Rbac.Module.REPORTS));
        setNavVisible(settingsNav, Rbac.canAccess(Rbac.Module.SETTINGS));
    }

    private void setNavVisible(HBox nav, boolean visible) {
        if (nav != null) {
            nav.setVisible(visible);
            nav.setManaged(visible);
        }
    }

    private void wireNavHandlers() {
        setNavAction(dashboardNav, () -> guardedLoad(Rbac.Module.DASHBOARD, "/views/dashboard_content.fxml", "dashboard"));
        setNavAction(patientsNav, () -> guardedLoad(Rbac.Module.PATIENTS, "/views/patients.fxml", "patients"));
        setNavAction(nursesNav, () -> guardedLoad(Rbac.Module.NURSES, "/views/nurses.fxml", "nurses"));
        setNavAction(doctorsNav, () -> guardedLoad(Rbac.Module.DOCTORS, "/views/doctors.fxml", "doctors"));
        setNavAction(billingNav, () -> guardedLoad(Rbac.Module.BILLING, "/views/billing.fxml", "billing"));
        setNavAction(pharmacyNav, () -> guardedLoad(Rbac.Module.PHARMACY, "/views/pharmacy.fxml", "pharmacy"));
        setNavAction(laboratoryNav, () -> guardedLoad(Rbac.Module.LABORATORY, "/views/laboratory.fxml", "laboratory"));
        setNavAction(appointmentsNav, () -> guardedLoad(Rbac.Module.APPOINTMENTS, "/views/appointments.fxml", "appointments"));
        setNavAction(departmentsNav, () -> guardedLoad(Rbac.Module.DEPARTMENTS, "/views/departments.fxml", "departments"));
        setNavAction(wardManagementNav, () -> guardedLoad(Rbac.Module.WARD, "/views/wards.fxml", "ward"));
        setNavAction(reportsNav, () -> guardedLoad(Rbac.Module.REPORTS, "/views/reports.fxml", "reports"));
        setNavAction(settingsNav, () -> guardedLoad(Rbac.Module.SETTINGS, "/views/settings.fxml", "settings"));

        if (userProfileSection != null) {
            userProfileSection.setOnMouseClicked(e -> guardedLoad(Rbac.Module.PROFILE, "/views/profile.fxml", "profile"));
        }
    }

    private void guardedLoad(Rbac.Module module, String fxml, String navKey) {
        if (AuthGuard.requireModule(module)) {
            loadContent(fxml, navKey);
        }
    }

    private void selectNavByKey(String navKey) {
        switch (navKey) {
            case "patients": setActiveNav(patientsNav); break;
            case "nurses": setActiveNav(nursesNav); break;
            case "doctors": setActiveNav(doctorsNav); break;
            case "billing": setActiveNav(billingNav); break;
            case "pharmacy": setActiveNav(pharmacyNav); break;
            case "laboratory": setActiveNav(laboratoryNav); break;
            case "appointments": setActiveNav(appointmentsNav); break;
            case "departments": setActiveNav(departmentsNav); break;
            case "ward": setActiveNav(wardManagementNav); break;
            case "reports": setActiveNav(reportsNav); break;
            case "settings": setActiveNav(settingsNav); break;
            case "profile": setActiveNav(null); break;
            default: setActiveNav(dashboardNav); break;
        }
    }

    private void setNavAction(HBox navItem, Runnable action) {
        if (navItem != null) {
            navItem.setOnMouseClicked(event -> action.run());
        }
    }

    private void setActiveNav(HBox navItem) {
        activeNav = navItem;
        HBox[] allNavItems = {
                dashboardNav, patientsNav, nursesNav, doctorsNav, appointmentsNav, departmentsNav,
                wardManagementNav, laboratoryNav, pharmacyNav, billingNav, reportsNav, settingsNav
        };
        for (HBox item : allNavItems) {
            if (item == null) {
                continue;
            }
            if (!item.getStyleClass().contains("nav-item")) {
                item.getStyleClass().add("nav-item");
            }
            item.getStyleClass().remove("nav-item-active");
        }
        if (navItem != null) {
            navItem.getStyleClass().add("nav-item-active");
        }
    }

    private String formatRole(String role) {
        switch (role) {
            case "ADMIN": return "Chief Administrator";
            case "DOCTOR": return "Doctor";
            case "NURSE": return "Nurse";
            default: return role;
        }
    }
}
