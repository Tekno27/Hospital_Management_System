package controllers;

import database.AppointmentDAO;
import database.DoctorDAO;
import database.PatientDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Appointment;
import models.Doctor;
import models.Patient;
import models.User;
import utils.Session;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AppointmentFormController {

    @FXML private Label formTitleLabel;
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField reasonField;
    @FXML private TextArea notesArea;
    @FXML private Label formErrorLabel;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private Appointment editing;
    private Runnable onSaveCallback;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.getAll()));
        doctorCombo.setItems(FXCollections.observableArrayList(doctorDAO.getAll()));
        statusCombo.setItems(FXCollections.observableArrayList("SCHEDULED", "COMPLETED", "CANCELLED", "NO_SHOW"));
        statusCombo.setValue("SCHEDULED");
        timeField.setPromptText("HH:MM (e.g. 09:30)");

        User user = Session.getCurrentUser();
        if (user != null && user.isDoctor() && user.getLinkedStaffId() != null) {
            doctorCombo.getItems().stream()
                    .filter(d -> d.getId() == user.getLinkedStaffId())
                    .findFirst()
                    .ifPresent(d -> {
                        doctorCombo.setValue(d);
                        doctorCombo.setDisable(true);
                    });
        }
    }

    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public void setOnSaveCallback(Runnable callback) { this.onSaveCallback = callback; }

    public void setAppointmentToEdit(Appointment appointment) {
        this.editing = appointment;
        formTitleLabel.setText("Edit Appointment");
        patientCombo.getItems().stream().filter(p -> p.getId() == appointment.getPatientId())
                .findFirst().ifPresent(patientCombo::setValue);
        doctorCombo.getItems().stream().filter(d -> d.getId() == appointment.getDoctorId())
                .findFirst().ifPresent(doctorCombo::setValue);
        if (appointment.getAppointmentDate() != null) {
            try { datePicker.setValue(LocalDate.parse(appointment.getAppointmentDate())); } catch (Exception ignored) {}
        }
        timeField.setText(appointment.getAppointmentTime());
        statusCombo.setValue(appointment.getStatus());
        reasonField.setText(appointment.getReason());
        notesArea.setText(appointment.getNotes());
    }

    @FXML
    private void handleSave() {
        if (patientCombo.getValue() == null) { showError("Select a patient."); return; }
        if (doctorCombo.getValue() == null) { showError("Select a doctor."); return; }
        if (datePicker.getValue() == null) { showError("Select appointment date."); return; }
        if (timeField.getText().trim().isEmpty()) { showError("Enter appointment time."); return; }

        Appointment appt = editing != null ? editing : new Appointment(0, 0, null, 0, null,
                null, null, "SCHEDULED", null, null);
        appt.setPatientId(patientCombo.getValue().getId());
        appt.setDoctorId(doctorCombo.getValue().getId());
        appt.setAppointmentDate(datePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE));
        appt.setAppointmentTime(timeField.getText().trim());
        appt.setStatus(statusCombo.getValue());
        appt.setReason(reasonField.getText().trim());
        appt.setNotes(notesArea.getText().trim());

        if (editing != null) {
            appointmentDAO.update(appt);
        } else {
            appointmentDAO.add(appt);
        }
        if (onSaveCallback != null) onSaveCallback.run();
        dialogStage.close();
    }

    @FXML
    private void handleCancel() { dialogStage.close(); }

    private void showError(String msg) {
        formErrorLabel.setText(msg);
        formErrorLabel.setVisible(true);
        formErrorLabel.setManaged(true);
    }
}
