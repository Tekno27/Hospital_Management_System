package controllers;

import database.DoctorDAO;
import database.PatientDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Doctor;
import models.Patient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField fullNameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField bloodGroupField;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private TextField wardField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea notesArea;
    @FXML private Label formErrorLabel;

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private Patient editingPatient; // null when adding a new patient
    private Runnable onSaveCallback;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        genderCombo.setItems(FXCollections.observableArrayList("MALE", "FEMALE", "OTHER"));
        statusCombo.setItems(FXCollections.observableArrayList("ADMITTED", "DISCHARGED", "OUTPATIENT"));

        List<Doctor> doctors = doctorDAO.getAll();
        doctorCombo.setItems(FXCollections.observableArrayList(doctors));
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    /** Call this to switch the form into edit mode, pre-filled with the given patient. */
    public void setPatientToEdit(Patient patient) {
        this.editingPatient = patient;
        formTitleLabel.setText("Edit Patient");

        fullNameField.setText(patient.getFullName());
        if (patient.getDateOfBirth() != null && !patient.getDateOfBirth().isEmpty()) {
            try {
                dobPicker.setValue(LocalDate.parse(patient.getDateOfBirth()));
            } catch (Exception ignored) {
                // leave date picker empty if stored value isn't a valid ISO date
            }
        }
        genderCombo.setValue(patient.getGender());
        phoneField.setText(patient.getPhone());
        addressField.setText(patient.getAddress());
        bloodGroupField.setText(patient.getBloodGroup());
        wardField.setText(patient.getWard());
        statusCombo.setValue(patient.getStatus());
        notesArea.setText(patient.getNotes());

        if (patient.getAssignedDoctorId() != null) {
            doctorCombo.getItems().stream()
                    .filter(d -> d.getId() == patient.getAssignedDoctorId())
                    .findFirst()
                    .ifPresent(doctorCombo::setValue);
        }
    }

    @FXML
    private void handleSave() {
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            showError("Full name is required.");
            return;
        }
        if (genderCombo.getValue() == null) {
            showError("Please select a gender.");
            return;
        }
        if (statusCombo.getValue() == null) {
            showError("Please select a status.");
            return;
        }

        String dob = dobPicker.getValue() != null
                ? dobPicker.getValue().format(DateTimeFormatter.ISO_DATE)
                : null;

        Doctor selectedDoctor = doctorCombo.getValue();
        Integer doctorId = selectedDoctor != null ? selectedDoctor.getId() : null;

        try {
            if (editingPatient == null) {
                Patient newPatient = new Patient(
                        0, fullName, dob, genderCombo.getValue(), phoneField.getText().trim(),
                        addressField.getText().trim(), bloodGroupField.getText().trim(),
                        doctorId, null, wardField.getText().trim(), statusCombo.getValue(),
                        null, null, notesArea.getText().trim()
                );
                patientDAO.add(newPatient);
            } else {
                editingPatient.setFullName(fullName);
                editingPatient.setDateOfBirth(dob);
                editingPatient.setGender(genderCombo.getValue());
                editingPatient.setPhone(phoneField.getText().trim());
                editingPatient.setAddress(addressField.getText().trim());
                editingPatient.setBloodGroup(bloodGroupField.getText().trim());
                editingPatient.setAssignedDoctorId(doctorId);
                editingPatient.setWard(wardField.getText().trim());
                editingPatient.setStatus(statusCombo.getValue());
                editingPatient.setNotes(notesArea.getText().trim());
                patientDAO.update(editingPatient);
            }
        } catch (RuntimeException e) {
            showError("Could not save patient. Please try again.");
            return;
        }

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void showError(String message) {
        formErrorLabel.setText(message);
        formErrorLabel.setVisible(true);
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}