package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Medicine;
import database.MedicineDAO;

public class MedicineFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField stockField;
    @FXML private TextField priceField;
    @FXML private TextField reorderField;
    @FXML private TextField expiryField;
    @FXML private Label formErrorLabel;

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private Medicine editingMedicine;
    private Runnable onSaveCallback;
    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setMedicineToEdit(Medicine medicine) {
        this.editingMedicine = medicine;
        formTitleLabel.setText("Edit Medicine");
        nameField.setText(medicine.getName());
        categoryField.setText(medicine.getCategory());
        stockField.setText(String.valueOf(medicine.getStockQuantity()));
        priceField.setText(String.valueOf(medicine.getUnitPrice()));
        reorderField.setText(String.valueOf(medicine.getReorderLevel()));
        expiryField.setText(medicine.getExpiryDate());
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Medicine name is required.");
            return;
        }

        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            int reorder = reorderField.getText().trim().isEmpty() ? 10 : Integer.parseInt(reorderField.getText().trim());

            if (editingMedicine == null) {
                medicineDAO.add(new Medicine(0, name, categoryField.getText().trim(), stock, price, reorder,
                        expiryField.getText().trim()));
            } else {
                editingMedicine.setName(name);
                editingMedicine.setCategory(categoryField.getText().trim());
                editingMedicine.setStockQuantity(stock);
                editingMedicine.setUnitPrice(price);
                editingMedicine.setReorderLevel(reorder);
                editingMedicine.setExpiryDate(expiryField.getText().trim());
                medicineDAO.update(editingMedicine);
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dialogStage.close();
        } catch (NumberFormatException e) {
            showError("Stock, price, and reorder level must be valid numbers.");
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
