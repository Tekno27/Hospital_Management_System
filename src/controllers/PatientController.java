package controllers;

import database.PatientDAO;
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
import models.Patient;
import utils.AuthGuard;
import utils.PatientAccess;
import utils.Rbac;
import utils.SceneManager;
import utils.Session;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PatientController {

    @FXML private Label recordCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> genderFilter;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, String> colGender;
    @FXML private TableColumn<Patient, String> colPhone;
    @FXML private TableColumn<Patient, String> colBloodGroup;
    @FXML private TableColumn<Patient, String> colDoctor;
    @FXML private TableColumn<Patient, String> colWard;
    @FXML private TableColumn<Patient, String> colStatus;
    @FXML private TableColumn<Patient, Void> colActions;
    @FXML private Button addPatientButton;

    private final PatientDAO patientDAO = new PatientDAO();
    private String activeWardFilter;

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.PATIENTS)) {
            return;
        }

        statusFilter.setItems(FXCollections.observableArrayList("All", "ADMITTED", "DISCHARGED", "OUTPATIENT"));
        statusFilter.setValue("All");
        genderFilter.setItems(FXCollections.observableArrayList("All", "MALE", "FEMALE", "OTHER"));
        genderFilter.setValue("All");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colBloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("assignedDoctorName"));
        colWard.setCellValueFactory(new PropertyValueFactory<>("ward"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colActions.setCellFactory(column -> new ActionButtonsCell());

        applyPermissions();
        applyFilters();

        String searchKeyword = Session.consumeSearchKeyword();
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            searchField.setText(searchKeyword);
        }
        String wardFromNav = Session.consumeWardFilter();
        if (wardFromNav != null && !wardFromNav.isEmpty()) {
            activeWardFilter = wardFromNav;
        }
        applyFilters();
    }

    private void applyPermissions() {
        boolean canAdd = Rbac.canCreatePatients();
        if (addPatientButton != null) {
            addPatientButton.setVisible(canAdd);
            addPatientButton.setManaged(canAdd);
        }
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilter.setValue("All");
        genderFilter.setValue("All");
        activeWardFilter = null;
        applyFilters();
    }

    private void applyFilters() {
        String keyword = searchField.getText().trim();
        List<Patient> patients = keyword.isEmpty()
                ? PatientAccess.getVisiblePatients()
                : PatientAccess.searchVisiblePatients(keyword);

        String status = statusFilter.getValue();
        if (status != null && !"All".equals(status)) {
            patients = patients.stream().filter(p -> status.equals(p.getStatus())).collect(Collectors.toList());
        }

        String gender = genderFilter.getValue();
        if (gender != null && !"All".equals(gender)) {
            patients = patients.stream().filter(p -> gender.equals(p.getGender())).collect(Collectors.toList());
        }

        if (activeWardFilter != null && !activeWardFilter.isEmpty()) {
            patients = patients.stream().filter(p -> activeWardFilter.equals(p.getWard())).collect(Collectors.toList());
        }

        ObservableList<Patient> data = FXCollections.observableArrayList(patients);
        patientTable.setItems(data);
        if (recordCountLabel != null) {
            recordCountLabel.setText(data.size() + (data.size() == 1 ? " record" : " records"));
        }
    }

    @FXML
    private void handleAdd() {
        openPatientForm(null);
    }

    private void openPatientForm(Patient patientToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/patient_form.fxml"));
            Parent root = loader.load();
            PatientFormController formController = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(patientToEdit == null ? "Add Patient" : "Edit Patient");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(SceneManager.createStyledScene(root));
            formController.setDialogStage(dialogStage);
            formController.setOnSaveCallback(this::applyFilters);
            if (patientToEdit != null) {
                formController.setPatientToEdit(patientToEdit);
            }
            dialogStage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open patient form: " + e.getMessage(), e);
        }
    }

    private void confirmAndDelete(Patient patient) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Patient");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete " + patient.getFullName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                patientDAO.delete(patient.getId());
                applyFilters();
            }
        });
    }

    private class ActionButtonsCell extends TableCell<Patient, Void> {
        private final Button editButton = new Button("Edit");
        private final Button deleteButton = new Button("Delete");
        private final HBox container = new HBox(6, editButton, deleteButton);

        ActionButtonsCell() {
            editButton.getStyleClass().add("btn-table-edit");
            deleteButton.getStyleClass().add("btn-table-delete");
            editButton.setOnAction(event -> openPatientForm(getTableView().getItems().get(getIndex())));
            deleteButton.setOnAction(event -> confirmAndDelete(getTableView().getItems().get(getIndex())));
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                return;
            }
            deleteButton.setVisible(Rbac.canDeletePatients());
            deleteButton.setManaged(deleteButton.isVisible());
            setGraphic(container);
        }
    }
}
