package controllers;

import database.LabOrderDAO;
import database.LabTestDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.LabOrder;
import models.LabTest;
import models.Patient;
import utils.PatientAccess;
import utils.Session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LabOrderFormController {

    @FXML private Label formTitleLabel;
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<LabTest> testCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField resultValueField;
    @FXML private TextArea notesArea;
    @FXML private Label formErrorLabel;

    private final LabOrderDAO labOrderDAO = new LabOrderDAO();
    private final LabTestDAO labTestDAO = new LabTestDAO();
    private LabOrder editingOrder;
    private Runnable onSaveCallback;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        patientCombo.setItems(FXCollections.observableArrayList(PatientAccess.getVisiblePatients()));
        testCombo.setItems(FXCollections.observableArrayList(labTestDAO.getAll()));
        statusCombo.setItems(FXCollections.observableArrayList(
                "ORDERED", "IN_PROGRESS", "COMPLETED", "CANCELLED"));
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setOrderToEdit(LabOrder order) {
        this.editingOrder = order;
        formTitleLabel.setText("Update Lab Order");
        patientCombo.setDisable(true);
        testCombo.setDisable(true);
        statusCombo.setVisible(true);
        statusCombo.setManaged(true);
        resultValueField.setVisible(true);
        resultValueField.setManaged(true);
        statusCombo.setValue(order.getStatus());
        resultValueField.setText(order.getResultValue());
        notesArea.setText(order.getResultNotes());
    }

    @FXML
    private void handleSave() {
        if (editingOrder != null) {
            String status = statusCombo.getValue();
            if (status == null) {
                showError("Status is required.");
                return;
            }
            editingOrder.setStatus(status);
            editingOrder.setResultValue(resultValueField.getText().trim());
            editingOrder.setResultNotes(notesArea.getText().trim());
            if ("COMPLETED".equals(status)) {
                editingOrder.setCompletedDate(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            labOrderDAO.update(editingOrder);
            finishSave();
            return;
        }

        Patient patient = patientCombo.getValue();
        LabTest test = testCombo.getValue();
        if (patient == null || test == null) {
            showError("Patient and test are required.");
            return;
        }

        Integer doctorId = null;
        var user = Session.getCurrentUser();
        if (user != null && user.isDoctor() && user.getLinkedStaffId() != null) {
            doctorId = user.getLinkedStaffId();
        } else if (user != null && user.isAdmin()) {
            doctorId = patient.getAssignedDoctorId();
        }

        LabOrder order = new LabOrder(0, patient.getId(), patient.getFullName(), doctorId, null,
                test.getId(), test.getName(), "ORDERED", null, notesArea.getText().trim(), null, null);
        labOrderDAO.add(order);
        finishSave();
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
