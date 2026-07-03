package controllers;

import database.MedicineDAO;
import database.PrescriptionDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Medicine;
import models.Prescription;
import utils.AuthGuard;
import utils.PatientAccess;
import utils.Rbac;
import utils.SceneManager;
import utils.Session;

import java.io.IOException;
import java.util.List;

public class PharmacyController {

    @FXML private TabPane pharmacyTabs;
    @FXML private Tab inventoryTab;
    @FXML private Tab prescriptionsTab;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> stockFilter;
    @FXML private ComboBox<String> rxStatusFilter;
    @FXML private Button addMedicineButton;
    @FXML private Button addPrescriptionButton;
    @FXML private TableView<Medicine> medicineTable;
    @FXML private TableColumn<Medicine, Integer> medColId;
    @FXML private TableColumn<Medicine, String> medColName;
    @FXML private TableColumn<Medicine, String> medColCategory;
    @FXML private TableColumn<Medicine, Integer> medColStock;
    @FXML private TableColumn<Medicine, Double> medColPrice;
    @FXML private TableColumn<Medicine, String> medColExpiry;
    @FXML private TableColumn<Medicine, Void> medColActions;
    @FXML private TableView<Prescription> prescriptionTable;
    @FXML private TableColumn<Prescription, Integer> rxColId;
    @FXML private TableColumn<Prescription, String> rxColPatient;
    @FXML private TableColumn<Prescription, String> rxColMedicine;
    @FXML private TableColumn<Prescription, String> rxColDosage;
    @FXML private TableColumn<Prescription, Integer> rxColQty;
    @FXML private TableColumn<Prescription, String> rxColStatus;
    @FXML private TableColumn<Prescription, Void> rxColActions;

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.PHARMACY)) {
            return;
        }

        setupMedicineTable();
        setupPrescriptionTable();
        applyPermissions();
        initFilters();
        loadMedicines();
        loadPrescriptions();
    }

    private void initFilters() {
        stockFilter.setItems(FXCollections.observableArrayList("All", "Low stock", "In stock"));
        stockFilter.setValue("All");
        rxStatusFilter.setItems(FXCollections.observableArrayList("All", "PENDING", "DISPENSED", "CANCELLED"));
        rxStatusFilter.setValue("All");
        refreshCategoryFilter();
    }

    private void refreshCategoryFilter() {
        List<String> categories = medicineDAO.getAll().stream()
                .map(Medicine::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        categories.add(0, "All");
        categoryFilter.setItems(FXCollections.observableArrayList(categories));
        categoryFilter.setValue("All");
    }

    @FXML
    private void handleApplyFilters() {
        loadMedicines();
        loadPrescriptions();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilter.setValue("All");
        stockFilter.setValue("All");
        rxStatusFilter.setValue("All");
        loadMedicines();
        loadPrescriptions();
    }

    private void applyPermissions() {
        boolean canManageInventory = Rbac.canManageMedicineInventory();
        addMedicineButton.setVisible(canManageInventory);
        addMedicineButton.setManaged(canManageInventory);
        if (!canManageInventory) {
            inventoryTab.setText("Medicine Catalog");
        }

        boolean canPrescribe = Rbac.canCreatePrescription();
        addPrescriptionButton.setVisible(canPrescribe);
        addPrescriptionButton.setManaged(canPrescribe);
    }

    private void setupMedicineTable() {
        medColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        medColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        medColCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        medColStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        medColPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        medColExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        medColActions.setCellFactory(col -> new MedicineActionCell());
    }

    private void setupPrescriptionTable() {
        rxColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        rxColPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        rxColMedicine.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        rxColDosage.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        rxColQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        rxColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        rxColActions.setCellFactory(col -> new PrescriptionActionCell());
    }

    private void loadMedicines() {
        List<Medicine> medicines = medicineDAO.getAll();
        String keyword = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (!keyword.isEmpty()) {
            medicines = medicines.stream()
                    .filter(m -> m.getName().toLowerCase().contains(keyword)
                            || (m.getCategory() != null && m.getCategory().toLowerCase().contains(keyword)))
                    .collect(java.util.stream.Collectors.toList());
        }
        String category = categoryFilter != null ? categoryFilter.getValue() : "All";
        if (category != null && !"All".equals(category)) {
            medicines = medicines.stream().filter(m -> category.equals(m.getCategory()))
                    .collect(java.util.stream.Collectors.toList());
        }
        String stock = stockFilter != null ? stockFilter.getValue() : "All";
        if ("Low stock".equals(stock)) {
            medicines = medicines.stream().filter(Medicine::isLowStock)
                    .collect(java.util.stream.Collectors.toList());
        } else if ("In stock".equals(stock)) {
            medicines = medicines.stream().filter(m -> !m.isLowStock())
                    .collect(java.util.stream.Collectors.toList());
        }
        medicineTable.setItems(FXCollections.observableArrayList(medicines));
    }

    private void loadPrescriptions() {
        List<Prescription> prescriptions;
        var user = Session.getCurrentUser();
        if (user.isAdmin()) {
            prescriptions = prescriptionDAO.getAll();
        } else if (user.isDoctor() && user.getLinkedStaffId() != null) {
            prescriptions = prescriptionDAO.getByDoctorId(user.getLinkedStaffId());
        } else if (user.isNurse()) {
            String ward = PatientAccess.nurseWard();
            prescriptions = ward != null ? prescriptionDAO.getByPatientWard(ward) : List.of();
        } else {
            prescriptions = List.of();
        }

        String keyword = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (!keyword.isEmpty()) {
            prescriptions = prescriptions.stream()
                    .filter(p -> p.getPatientName().toLowerCase().contains(keyword)
                            || p.getMedicineName().toLowerCase().contains(keyword))
                    .collect(java.util.stream.Collectors.toList());
        }
        String rxStatus = rxStatusFilter != null ? rxStatusFilter.getValue() : "All";
        if (rxStatus != null && !"All".equals(rxStatus)) {
            prescriptions = prescriptions.stream().filter(p -> rxStatus.equals(p.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        prescriptionTable.setItems(FXCollections.observableArrayList(prescriptions));
    }

    @FXML
    private void handleAddMedicine() {
        openMedicineForm(null);
    }

    @FXML
    private void handleAddPrescription() {
        openPrescriptionForm(null);
    }

    private void openMedicineForm(Medicine medicine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/medicine_form.fxml"));
            Parent root = loader.load();
            MedicineFormController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle(medicine == null ? "Add Medicine" : "Edit Medicine");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(SceneManager.createStyledScene(root));
            controller.setDialogStage(dialog);
            controller.setOnSaveCallback(this::loadMedicines);
            if (medicine != null) {
                controller.setMedicineToEdit(medicine);
            }
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open medicine form: " + e.getMessage(), e);
        }
    }

    private void openPrescriptionForm(Prescription prescription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/prescription_form.fxml"));
            Parent root = loader.load();
            PrescriptionFormController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle(prescription == null ? "New Prescription" : "Edit Prescription");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(SceneManager.createStyledScene(root));
            controller.setDialogStage(dialog);
            controller.setOnSaveCallback(this::loadPrescriptions);
            if (prescription != null) {
                controller.setPrescriptionToEdit(prescription);
            }
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open prescription form: " + e.getMessage(), e);
        }
    }

    private void dispensePrescription(Prescription prescription) {
        if (!"PENDING".equals(prescription.getStatus())) {
            showAlert("This prescription has already been processed.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Dispense Medication");
        confirm.setHeaderText(null);
        confirm.setContentText("Dispense " + prescription.getQuantity() + " units of "
                + prescription.getMedicineName() + " to " + prescription.getPatientName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    prescriptionDAO.dispense(prescription.getId(), prescription.getMedicineId(),
                            prescription.getQuantity());
                    loadPrescriptions();
                    loadMedicines();
                } catch (RuntimeException e) {
                    showAlert(e.getMessage());
                }
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Pharmacy");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class MedicineActionCell extends TableCell<Medicine, Void> {
        private final Button editBtn = new Button("Edit");
        private final Button deleteBtn = new Button("Delete");
        private final HBox box = new HBox(6, editBtn, deleteBtn);

        MedicineActionCell() {
            editBtn.getStyleClass().add("btn-table-edit");
            deleteBtn.getStyleClass().add("btn-table-delete");
            editBtn.setOnAction(e -> openMedicineForm(getTableView().getItems().get(getIndex())));
            deleteBtn.setOnAction(e -> {
                Medicine med = getTableView().getItems().get(getIndex());
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + med.getName() + "?",
                        ButtonType.OK, ButtonType.CANCEL);
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        medicineDAO.delete(med.getId());
                        loadMedicines();
                    }
                });
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || !Rbac.canManageMedicineInventory()) {
                setGraphic(null);
            } else {
                setGraphic(box);
            }
        }
    }

    private class PrescriptionActionCell extends TableCell<Prescription, Void> {
        private final Button dispenseBtn = new Button("Dispense");
        private final Button deleteBtn = new Button("Delete");
        private final HBox box = new HBox(6, dispenseBtn, deleteBtn);

        PrescriptionActionCell() {
            dispenseBtn.getStyleClass().add("btn-table-success");
            deleteBtn.getStyleClass().add("btn-table-delete");
            dispenseBtn.setOnAction(e -> dispensePrescription(getTableView().getItems().get(getIndex())));
            deleteBtn.setOnAction(e -> {
                if (!Rbac.canManageMedicineInventory()) {
                    return;
                }
                Prescription rx = getTableView().getItems().get(getIndex());
                prescriptionDAO.delete(rx.getId());
                loadPrescriptions();
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                return;
            }
            Prescription rx = getTableView().getItems().get(getIndex());
            dispenseBtn.setVisible(Rbac.canDispensePrescription() && "PENDING".equals(rx.getStatus()));
            dispenseBtn.setManaged(dispenseBtn.isVisible());
            deleteBtn.setVisible(Rbac.canManageMedicineInventory());
            deleteBtn.setManaged(deleteBtn.isVisible());
            setGraphic(box);
        }
    }
}
