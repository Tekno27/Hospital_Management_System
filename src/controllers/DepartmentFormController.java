package controllers;

import database.DepartmentDAO;
import database.DoctorDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Department;
import models.Doctor;

public class DepartmentFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextField floorField;
    @FXML private ComboBox<Doctor> headDoctorCombo;
    @FXML private TextField bedCapacityField;
    @FXML private TextField phoneField;
    @FXML private TextArea descriptionArea;
    @FXML private Label formErrorLabel;

    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private Department editing;
    private Runnable onSaveCallback;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        headDoctorCombo.setItems(FXCollections.observableArrayList(doctorDAO.getAll()));
        bedCapacityField.setText("20");
    }

    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public void setOnSaveCallback(Runnable callback) { this.onSaveCallback = callback; }

    public void setDepartmentToEdit(Department department) {
        this.editing = department;
        formTitleLabel.setText("Edit Department");
        nameField.setText(department.getName());
        floorField.setText(department.getFloor());
        if (department.getHeadDoctorId() != null) {
            headDoctorCombo.getItems().stream()
                    .filter(d -> d.getId() == department.getHeadDoctorId())
                    .findFirst().ifPresent(headDoctorCombo::setValue);
        }
        bedCapacityField.setText(String.valueOf(department.getBedCapacity()));
        phoneField.setText(department.getPhone());
        descriptionArea.setText(department.getDescription());
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty()) { showError("Department name is required."); return; }
        int beds;
        try {
            beds = Integer.parseInt(bedCapacityField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Bed capacity must be a number.");
            return;
        }

        Department dep = editing != null ? editing : new Department(0, null, null, null, null, 20, null, null, 0, 0);
        dep.setName(nameField.getText().trim());
        dep.setFloor(floorField.getText().trim());
        dep.setHeadDoctorId(headDoctorCombo.getValue() != null ? headDoctorCombo.getValue().getId() : null);
        dep.setBedCapacity(beds);
        dep.setPhone(phoneField.getText().trim());
        dep.setDescription(descriptionArea.getText().trim());

        if (editing != null) {
            departmentDAO.update(dep);
        } else {
            departmentDAO.add(dep);
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
