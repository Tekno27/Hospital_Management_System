package controllers;

import database.MedicineDAO;
import database.PrescriptionDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Medicine;
import models.Patient;
import models.Prescription;
import utils.PatientAccess;
import utils.Session;

public class PrescriptionFormController {

    @FXML private Label formTitleLabel;
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Medicine> medicineCombo;
    @FXML private TextField dosageField;
    @FXML private TextField frequencyField;
    @FXML private TextField durationField;
    @FXML private TextField quantityField;
    @FXML private TextArea notesArea;
    @FXML private Label formErrorLabel;

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private Prescription editingPrescription;
    private Runnable onSaveCallback;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        patientCombo.setItems(FXCollections.observableArrayList(PatientAccess.getVisiblePatients()));
        medicineCombo.setItems(FXCollections.observableArrayList(medicineDAO.getAll()));
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setPrescriptionToEdit(Prescription prescription) {
        this.editingPrescription = prescription;
        formTitleLabel.setText("Edit Prescription");
        dosageField.setText(prescription.getDosage());
        frequencyField.setText(prescription.getFrequency());
        durationField.setText(prescription.getDuration());
        quantityField.setText(String.valueOf(prescription.getQuantity()));
        notesArea.setText(prescription.getNotes());
        patientCombo.setDisable(true);
        medicineCombo.setDisable(true);
    }

    @FXML
    private void handleSave() {
        if (editingPrescription != null) {
            editingPrescription.setDosage(dosageField.getText().trim());
            editingPrescription.setFrequency(frequencyField.getText().trim());
            editingPrescription.setDuration(durationField.getText().trim());
            editingPrescription.setNotes(notesArea.getText().trim());
            try {
                editingPrescription.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            } catch (NumberFormatException e) {
                showError("Quantity must be a number.");
                return;
            }
            prescriptionDAO.update(editingPrescription);
            finishSave();
            return;
        }

        Patient patient = patientCombo.getValue();
        Medicine medicine = medicineCombo.getValue();
        String dosage = dosageField.getText().trim();

        if (patient == null || medicine == null || dosage.isEmpty()) {
            showError("Patient, medicine, and dosage are required.");
            return;
        }

        try {
            int quantity = quantityField.getText().trim().isEmpty() ? 1 : Integer.parseInt(quantityField.getText().trim());
            Integer doctorId = null;
            var user = Session.getCurrentUser();
            if (user != null && user.isDoctor() && user.getLinkedStaffId() != null) {
                doctorId = user.getLinkedStaffId();
            } else if (user != null && user.isAdmin()) {
                doctorId = patient.getAssignedDoctorId();
            }

            Prescription rx = new Prescription(0, patient.getId(), patient.getFullName(), doctorId, null,
                    medicine.getId(), medicine.getName(), dosage, frequencyField.getText().trim(),
                    durationField.getText().trim(), quantity, "PENDING", null, null, notesArea.getText().trim());
            prescriptionDAO.add(rx);
            finishSave();
        } catch (NumberFormatException e) {
            showError("Quantity must be a number.");
        }
    }

    private void finishSave() {
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showError(String message) {
        formErrorLabel.setText(message);
        formErrorLabel.setVisible(true);
    }
}
