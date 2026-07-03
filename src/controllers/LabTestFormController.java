package controllers;

import database.LabTestDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.LabTest;

public class LabTestFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextField rangeField;
    @FXML private Label formErrorLabel;

    private final LabTestDAO labTestDAO = new LabTestDAO();
    private LabTest editingTest;
    private Runnable onSaveCallback;
    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setTestToEdit(LabTest test) {
        this.editingTest = test;
        formTitleLabel.setText("Edit Lab Test");
        nameField.setText(test.getName());
        categoryField.setText(test.getCategory());
        priceField.setText(String.valueOf(test.getPrice()));
        rangeField.setText(test.getNormalRange());
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Test name is required.");
            return;
        }

        try {
            double price = priceField.getText().trim().isEmpty() ? 0 : Double.parseDouble(priceField.getText().trim());
            if (editingTest == null) {
                labTestDAO.add(new LabTest(0, name, categoryField.getText().trim(), price, rangeField.getText().trim()));
            } else {
                editingTest.setName(name);
                editingTest.setCategory(categoryField.getText().trim());
                editingTest.setPrice(price);
                editingTest.setNormalRange(rangeField.getText().trim());
                labTestDAO.update(editingTest);
            }
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dialogStage.close();
        } catch (NumberFormatException e) {
            showError("Price must be a valid number.");
        }
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
