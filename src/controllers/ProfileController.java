package controllers;

import database.AppointmentDAO;
import database.DoctorDAO;
import database.LabOrderDAO;
import database.NurseDAO;
import database.PrescriptionDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import models.Doctor;
import models.Nurse;
import models.User;
import utils.AuthGuard;
import utils.PatientAccess;
import utils.Rbac;
import utils.SceneManager;
import utils.Session;

import java.time.format.DateTimeFormatter;

public class ProfileController {

    @FXML private Label avatarLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label roleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label staffInfoLabel;
    @FXML private Label portalLabel;
    @FXML private Label loginSinceLabel;
    @FXML private Label statPatients;
    @FXML private Label statPatientsLabel;
    @FXML private Label statPendingRx;
    @FXML private Label statPendingLabs;
    @FXML private Label statUpcomingAppts;
    @FXML private Label permissionsLabel;
    @FXML private Button appointmentsQuickBtn;

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final LabOrderDAO labOrderDAO = new LabOrderDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.PROFILE)) {
            return;
        }
        User user = Session.getCurrentUser();
        if (user == null) {
            return;
        }

        avatarLabel.setText(Rbac.initials(user.getFullName()));
        fullNameLabel.setText(user.getFullName());
        roleLabel.setText(formatRole(user.getRole()));
        usernameLabel.setText(user.getUsername());
        portalLabel.setText(Rbac.portalLabel());

        if (Session.getLoginTime() != null) {
            loginSinceLabel.setText("Logged in since "
                    + Session.getLoginTime().format(DateTimeFormatter.ofPattern("HH:mm · d MMM yyyy")));
        }

        loadStaffContact(user);
        loadStats(user);
        loadPermissions(user);

        if (appointmentsQuickBtn != null) {
            boolean showAppts = Rbac.canAccess(Rbac.Module.APPOINTMENTS);
            appointmentsQuickBtn.setVisible(showAppts);
            appointmentsQuickBtn.setManaged(showAppts);
        }
    }

    private void loadStaffContact(User user) {
        staffInfoLabel.setText(resolveStaffInfo(user));
        if (user.isDoctor() && user.getLinkedStaffId() != null) {
            Doctor doctor = new DoctorDAO().getAll().stream()
                    .filter(d -> d.getId() == user.getLinkedStaffId())
                    .findFirst().orElse(null);
            if (doctor != null) {
                emailLabel.setText(doctor.getEmail() != null ? doctor.getEmail() : "—");
                phoneLabel.setText(doctor.getPhone() != null ? doctor.getPhone() : "—");
            }
        } else if (user.isNurse() && user.getLinkedStaffId() != null) {
            Nurse nurse = new NurseDAO().getAll().stream()
                    .filter(n -> n.getId() == user.getLinkedStaffId())
                    .findFirst().orElse(null);
            if (nurse != null) {
                emailLabel.setText(nurse.getEmail() != null ? nurse.getEmail() : "—");
                phoneLabel.setText(nurse.getPhone() != null ? nurse.getPhone() : "—");
            }
        } else {
            emailLabel.setText("admin@medcore.hospital");
            phoneLabel.setText("—");
        }
    }

    private void loadStats(User user) {
        int patients = PatientAccess.countVisiblePatients();
        statPatients.setText(String.valueOf(patients));
        statPatientsLabel.setText(user.isDoctor() ? "Assigned patients" : user.isNurse() ? "Ward patients" : "All patients");

        int pendingRx;
        int pendingLabs;
        if (user.isAdmin()) {
            pendingRx = prescriptionDAO.countPending();
            pendingLabs = labOrderDAO.countPending();
        } else if (user.isDoctor() && user.getLinkedStaffId() != null) {
            pendingRx = prescriptionDAO.countPendingForDoctor(user.getLinkedStaffId());
            pendingLabs = labOrderDAO.countPendingForDoctor(user.getLinkedStaffId());
        } else if (user.isNurse()) {
            String ward = PatientAccess.nurseWard();
            pendingRx = ward != null ? prescriptionDAO.countPendingForWard(ward) : 0;
            pendingLabs = ward != null ? labOrderDAO.countPendingForWard(ward) : 0;
        } else {
            pendingRx = 0;
            pendingLabs = 0;
        }
        statPendingRx.setText(String.valueOf(pendingRx));
        statPendingLabs.setText(String.valueOf(pendingLabs));

        if (user.isDoctor() && user.getLinkedStaffId() != null) {
            statUpcomingAppts.setText(String.valueOf(
                    appointmentDAO.getByDoctorId(user.getLinkedStaffId()).stream()
                            .filter(a -> "SCHEDULED".equals(a.getStatus())).count()));
        } else if (user.isAdmin()) {
            statUpcomingAppts.setText(String.valueOf(appointmentDAO.countUpcoming()));
        } else {
            statUpcomingAppts.setText("—");
        }
    }

    private void loadPermissions(User user) {
        StringBuilder sb = new StringBuilder();
        if (user.isAdmin()) {
            sb.append("Full system access including billing, reports, departments, and user management.");
        } else if (user.isDoctor()) {
            sb.append("View assigned patients, schedule appointments, prescribe medications, and order lab tests.");
        } else if (user.isNurse()) {
            sb.append("Manage ward patients, dispense prescriptions, and update laboratory orders.");
        }
        permissionsLabel.setText(sb.toString());
    }

    private String resolveStaffInfo(User user) {
        if (user.isDoctor() && user.getLinkedStaffId() != null) {
            return new DoctorDAO().getAll().stream()
                    .filter(d -> d.getId() == user.getLinkedStaffId())
                    .findFirst()
                    .map(d -> d.getDepartment() + " · " + d.getSpecialization())
                    .orElse("Doctor profile linked");
        }
        if (user.isNurse() && user.getLinkedStaffId() != null) {
            String ward = new NurseDAO().getWardById(user.getLinkedStaffId());
            return ward != null ? "Assigned ward: " + ward : "Nurse profile linked";
        }
        return "System-wide administrator access";
    }

    @FXML private void handleBackToDashboard() { SceneManager.loadDashboard(); }
    @FXML private void handleGoDashboard() { SceneManager.loadDashboard(); }
    @FXML private void handleGoPatients() { SceneManager.loadInShell("/views/patients.fxml", "patients"); }
    @FXML private void handleGoPharmacy() { SceneManager.loadInShell("/views/pharmacy.fxml", "pharmacy"); }
    @FXML private void handleGoLab() { SceneManager.loadInShell("/views/laboratory.fxml", "laboratory"); }
    @FXML private void handleGoAppointments() { SceneManager.loadInShell("/views/appointments.fxml", "appointments"); }

    @FXML
    private void handleLogout() {
        Session.clear();
        SceneManager.switchTo("/views/login.fxml");
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
