package controllers;

import database.LabOrderDAO;
import database.LabTestDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.LabOrder;
import models.LabTest;
import utils.AuthGuard;
import utils.PatientAccess;
import utils.Rbac;
import utils.SceneManager;
import utils.Session;

import java.io.IOException;
import java.util.List;

public class LaboratoryController {

    @FXML private TabPane labTabs;
    @FXML private Tab catalogTab;
    @FXML private Button addTestButton;
    @FXML private Button addOrderButton;
    @FXML private TableView<LabTest> testTable;
    @FXML private TableColumn<LabTest, Integer> testColId;
    @FXML private TableColumn<LabTest, String> testColName;
    @FXML private TableColumn<LabTest, String> testColCategory;
    @FXML private TableColumn<LabTest, Double> testColPrice;
    @FXML private TableColumn<LabTest, String> testColRange;
    @FXML private TableColumn<LabTest, Void> testColActions;
    @FXML private TableView<LabOrder> orderTable;
    @FXML private TableColumn<LabOrder, Integer> orderColId;
    @FXML private TableColumn<LabOrder, String> orderColPatient;
    @FXML private TableColumn<LabOrder, String> orderColTest;
    @FXML private TableColumn<LabOrder, String> orderColDoctor;
    @FXML private TableColumn<LabOrder, String> orderColStatus;
    @FXML private TableColumn<LabOrder, String> orderColResult;
    @FXML private TableColumn<LabOrder, Void> orderColActions;

    private final LabTestDAO labTestDAO = new LabTestDAO();
    private final LabOrderDAO labOrderDAO = new LabOrderDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.LABORATORY)) {
            return;
        }

        setupTestTable();
        setupOrderTable();
        applyPermissions();
        loadTests();
        loadOrders();
    }

    private void applyPermissions() {
        boolean canManageCatalog = Rbac.canManageLabCatalog();
        addTestButton.setVisible(canManageCatalog);
        addTestButton.setManaged(canManageCatalog);
        if (!canManageCatalog) {
            catalogTab.setText("Test Catalog");
        }

        boolean canOrder = Rbac.canOrderLabTest();
        addOrderButton.setVisible(canOrder);
        addOrderButton.setManaged(canOrder);
    }

    private void setupTestTable() {
        testColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        testColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        testColCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        testColPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        testColRange.setCellValueFactory(new PropertyValueFactory<>("normalRange"));
        testColActions.setCellFactory(col -> new TestActionCell());
    }

    private void setupOrderTable() {
        orderColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderColPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        orderColTest.setCellValueFactory(new PropertyValueFactory<>("testName"));
        orderColDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        orderColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderColResult.setCellValueFactory(new PropertyValueFactory<>("resultValue"));
        orderColActions.setCellFactory(col -> new OrderActionCell());
    }

    private void loadTests() {
        testTable.setItems(FXCollections.observableArrayList(labTestDAO.getAll()));
    }

    private void loadOrders() {
        List<LabOrder> orders;
        var user = Session.getCurrentUser();
        if (user.isAdmin()) {
            orders = labOrderDAO.getAll();
        } else if (user.isDoctor() && user.getLinkedStaffId() != null) {
            orders = labOrderDAO.getByDoctorId(user.getLinkedStaffId());
        } else if (user.isNurse()) {
            String ward = PatientAccess.nurseWard();
            orders = ward != null ? labOrderDAO.getByPatientWard(ward) : List.of();
        } else {
            orders = List.of();
        }
        orderTable.setItems(FXCollections.observableArrayList(orders));
    }

    @FXML
    private void handleAddTest() {
        openTestForm(null);
    }

    @FXML
    private void handleAddOrder() {
        openOrderForm(null);
    }

    private void openTestForm(LabTest test) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/lab_test_form.fxml"));
            Parent root = loader.load();
            LabTestFormController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle(test == null ? "Add Lab Test" : "Edit Lab Test");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(SceneManager.createStyledScene(root));
            controller.setDialogStage(dialog);
            controller.setOnSaveCallback(this::loadTests);
            if (test != null) {
                controller.setTestToEdit(test);
            }
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open lab test form: " + e.getMessage(), e);
        }
    }

    private void openOrderForm(LabOrder order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/lab_order_form.fxml"));
            Parent root = loader.load();
            LabOrderFormController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle(order == null ? "New Lab Order" : "Update Lab Order");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(SceneManager.createStyledScene(root));
            controller.setDialogStage(dialog);
            controller.setOnSaveCallback(this::loadOrders);
            if (order != null) {
                controller.setOrderToEdit(order);
            }
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open lab order form: " + e.getMessage(), e);
        }
    }

    private class TestActionCell extends TableCell<LabTest, Void> {
        private final Button editBtn = new Button("Edit");
        private final Button deleteBtn = new Button("Delete");
        private final HBox box = new HBox(6, editBtn, deleteBtn);

        TestActionCell() {
            editBtn.getStyleClass().add("btn-table-edit");
            deleteBtn.getStyleClass().add("btn-table-delete");
            editBtn.setOnAction(e -> openTestForm(getTableView().getItems().get(getIndex())));
            deleteBtn.setOnAction(e -> {
                LabTest test = getTableView().getItems().get(getIndex());
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + test.getName() + "?",
                        ButtonType.OK, ButtonType.CANCEL);
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        labTestDAO.delete(test.getId());
                        loadTests();
                    }
                });
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty || !Rbac.canManageLabCatalog() ? null : box);
        }
    }

    private class OrderActionCell extends TableCell<LabOrder, Void> {
        private final Button updateBtn = new Button("Update");
        private final Button deleteBtn = new Button("Delete");
        private final HBox box = new HBox(6, updateBtn, deleteBtn);

        OrderActionCell() {
            updateBtn.getStyleClass().add("btn-table-edit");
            deleteBtn.getStyleClass().add("btn-table-delete");
            updateBtn.setOnAction(e -> openOrderForm(getTableView().getItems().get(getIndex())));
            deleteBtn.setOnAction(e -> {
                if (!Rbac.canManageLabCatalog()) {
                    return;
                }
                labOrderDAO.delete(getTableView().getItems().get(getIndex()).getId());
                loadOrders();
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                return;
            }
            updateBtn.setVisible(Rbac.canUpdateLabOrder());
            updateBtn.setManaged(updateBtn.isVisible());
            deleteBtn.setVisible(Rbac.canManageLabCatalog());
            deleteBtn.setManaged(deleteBtn.isVisible());
            setGraphic(box);
        }
    }
}
