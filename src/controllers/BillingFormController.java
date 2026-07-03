package controllers;

import database.BillingDAO;
import database.PatientDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Billing;
import models.Patient;

import java.time.format.DateTimeFormatter;

public class BillingFormController {

    @FXML private Label formTitleLabel;
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private TextField amountPaidField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker dueDatePicker;
    @FXML private Label formErrorLabel;

    private final BillingDAO billingDAO = new BillingDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    private Billing editingBilling;
    private Runnable onSaveCallback;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.getAll()));
        statusCombo.setItems(FXCollections.observableArrayList("UNPAID", "PARTIAL", "PAID"));
        amountPaidField.setText("0");
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setBillingToEdit(Billing billing) {
        this.editingBilling = billing;
        formTitleLabel.setText("Edit Bill");

        patientCombo.getItems().stream()
                .filter(p -> p.getId() == billing.getPatientId())
                .findFirst()
                .ifPresent(patientCombo::setValue);
        patientCombo.setDisable(true); // don't allow reassigning a bill to a different patient

        descriptionField.setText(billing.getDescription());
        amountField.setText(String.valueOf(billing.getAmount()));
        amountPaidField.setText(String.valueOf(billing.getAmountPaid()));
        statusCombo.setValue(billing.getStatus());
        if (billing.getDueDate() != null && !billing.getDueDate().isEmpty()) {
            try {
                dueDatePicker.setValue(java.time.LocalDate.parse(billing.getDueDate()));
            } catch (Exception ignored) {
            }
        }
    }

    @FXML
    private void handleSave() {
        if (patientCombo.getValue() == null) {
            showError("Please select a patient.");
            return;
        }
        if (descriptionField.getText().trim().isEmpty()) {
            showError("Description is required.");
            return;
        }
        if (statusCombo.getValue() == null) {
            showError("Please select a status.");
            return;
        }

        double amount;
        double amountPaid;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            amountPaid = amountPaidField.getText().trim().isEmpty()
                    ? 0.0
                    : Double.parseDouble(amountPaidField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Amount and amount paid must be valid numbers.");
            return;
        }

        String dueDate = dueDatePicker.getValue() != null
                ? dueDatePicker.getValue().format(DateTimeFormatter.ISO_DATE)
                : null;

        try {
            if (editingBilling == null) {
                Billing newBilling = new Billing(
                        0, patientCombo.getValue().getId(), null, descriptionField.getText().trim(),
                        amount, statusCombo.getValue(), amountPaid, null, dueDate
                );
                billingDAO.add(newBilling);
            } else {
                editingBilling.setDescription(descriptionField.getText().trim());
                editingBilling.setAmount(amount);
                editingBilling.setAmountPaid(amountPaid);
                editingBilling.setStatus(statusCombo.getValue());
                editingBilling.setDueDate(dueDate);
                billingDAO.update(editingBilling);
            }
        } catch (RuntimeException e) {
            showError("Could not save bill. Please try again.");
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