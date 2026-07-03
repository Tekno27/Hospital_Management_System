package controllers;

import database.DoctorDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Doctor;
import utils.AuthGuard;
import utils.Rbac;
import utils.SceneManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorController {

    @FXML private Label recordCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private TableView<Doctor> doctorTable;
    @FXML private TableColumn<Doctor, Integer> colId;
    @FXML private TableColumn<Doctor, String> colName;
    @FXML private TableColumn<Doctor, String> colSpecialization;
    @FXML private TableColumn<Doctor, String> colDepartment;
    @FXML private TableColumn<Doctor, String> colPhone;
    @FXML private TableColumn<Doctor, String> colEmail;
    @FXML private TableColumn<Doctor, Void> colActions;
    @FXML private Button addDoctorButton;

    private final DoctorDAO doctorDAO = new DoctorDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.DOCTORS)) {
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colActions.setCellFactory(column -> new ActionButtonsCell());

        loadDepartmentFilter();
        applyFilters();
    }

    private void loadDepartmentFilter() {
        List<String> departments = doctorDAO.getAll().stream()
                .map(Doctor::getDepartment)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        ObservableList<String> items = FXCollections.observableArrayList("All");
        items.addAll(departments);
        departmentFilter.setItems(items);
        departmentFilter.setValue("All");
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        departmentFilter.setValue("All");
        applyFilters();
    }

    private void applyFilters() {
        List<Doctor> doctors = doctorDAO.getAll();
        String keyword = searchField.getText().trim().toLowerCase();
        if (!keyword.isEmpty()) {
            doctors = doctors.stream()
                    .filter(d -> contains(d.getFullName(), keyword)
                            || contains(d.getDepartment(), keyword)
                            || contains(d.getSpecialization(), keyword))
                    .collect(Collectors.toList());
        }
        String dept = departmentFilter.getValue();
        if (dept != null && !"All".equals(dept)) {
            doctors = doctors.stream().filter(d -> dept.equals(d.getDepartment())).collect(Collectors.toList());
        }
        doctorTable.setItems(FXCollections.observableArrayList(doctors));
        recordCountLabel.setText(doctors.size() + (doctors.size() == 1 ? " record" : " records"));
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    @FXML
    private void handleAdd() {
        openDoctorForm(null);
    }

    private void openDoctorForm(Doctor doctorToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/doctor_form.fxml"));
            Parent root = loader.load();
            DoctorFormController formController = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(doctorToEdit == null ? "Add Doctor" : "Edit Doctor");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(SceneManager.createStyledScene(root));
            formController.setDialogStage(dialogStage);
            formController.setOnSaveCallback(() -> {
                loadDepartmentFilter();
                applyFilters();
            });
            if (doctorToEdit != null) {
                formController.setDoctorToEdit(doctorToEdit);
            }
            dialogStage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open doctor form: " + e.getMessage(), e);
        }
    }

    private void confirmAndDelete(Doctor doctor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Doctor");
        confirm.setContentText("Delete Dr. " + doctor.getFullName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                doctorDAO.delete(doctor.getId());
                loadDepartmentFilter();
                applyFilters();
            }
        });
    }

    private class ActionButtonsCell extends TableCell<Doctor, Void> {
        private final Button editButton = new Button("Edit");
        private final Button deleteButton = new Button("Delete");
        private final HBox container = new HBox(6, editButton, deleteButton);

        ActionButtonsCell() {
            editButton.getStyleClass().add("btn-table-edit");
            deleteButton.getStyleClass().add("btn-table-delete");
            editButton.setOnAction(e -> openDoctorForm(getTableView().getItems().get(getIndex())));
            deleteButton.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex())));
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : container);
        }
    }
}
