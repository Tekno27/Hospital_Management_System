package controllers;

import database.DoctorDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Doctor;

public class DoctorFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField specializationField;
    @FXML private TextField departmentField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label formErrorLabel;

    private final DoctorDAO doctorDAO = new DoctorDAO();

    private Doctor editingDoctor;
    private Runnable onSaveCallback;
    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setDoctorToEdit(Doctor doctor) {
        this.editingDoctor = doctor;
        formTitleLabel.setText("Edit Doctor");

        fullNameField.setText(doctor.getFullName());
        specializationField.setText(doctor.getSpecialization());
        departmentField.setText(doctor.getDepartment());
        phoneField.setText(doctor.getPhone());
        emailField.setText(doctor.getEmail());
    }

    @FXML
    private void handleSave() {
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            showError("Full name is required.");
            return;
        }

        try {
            if (editingDoctor == null) {
                Doctor newDoctor = new Doctor(
                        0, fullName, specializationField.getText().trim(),
                        phoneField.getText().trim(), emailField.getText().trim(),
                        departmentField.getText().trim()
                );
                doctorDAO.add(newDoctor);
            } else {
                editingDoctor.setFullName(fullName);
                editingDoctor.setSpecialization(specializationField.getText().trim());
                editingDoctor.setDepartment(departmentField.getText().trim());
                editingDoctor.setPhone(phoneField.getText().trim());
                editingDoctor.setEmail(emailField.getText().trim());
                doctorDAO.update(editingDoctor);
            }
        } catch (RuntimeException e) {
            showError("Could not save doctor. Please try again.");
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