package controllers;

import database.BillingDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Billing;
import utils.AuthGuard;
import utils.Rbac;
import utils.SceneManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BillingController {

    @FXML private Label recordCountLabel;
    @FXML private Label outstandingLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Billing> billingTable;
    @FXML private TableColumn<Billing, Integer> colId;
    @FXML private TableColumn<Billing, String> colPatient;
    @FXML private TableColumn<Billing, String> colDescription;
    @FXML private TableColumn<Billing, Double> colAmount;
    @FXML private TableColumn<Billing, Double> colPaid;
    @FXML private TableColumn<Billing, Double> colBalance;
    @FXML private TableColumn<Billing, String> colStatus;
    @FXML private TableColumn<Billing, Void> colActions;
    @FXML private Button addBillButton;

    private final BillingDAO billingDAO = new BillingDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.BILLING)) {
            return;
        }

        statusFilter.setItems(FXCollections.observableArrayList("All", "UNPAID", "PARTIAL", "PAID"));
        statusFilter.setValue("All");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colActions.setCellFactory(column -> new ActionButtonsCell());

        applyFilters();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilter.setValue("All");
        applyFilters();
    }

    private void applyFilters() {
        List<Billing> bills = billingDAO.getAll();
        String keyword = searchField.getText().trim().toLowerCase();
        if (!keyword.isEmpty()) {
            bills = bills.stream()
                    .filter(b -> b.getPatientName() != null && b.getPatientName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }
        String status = statusFilter.getValue();
        if (status != null && !"All".equals(status)) {
            bills = bills.stream().filter(b -> status.equals(b.getStatus())).collect(Collectors.toList());
        }
        billingTable.setItems(FXCollections.observableArrayList(bills));
        recordCountLabel.setText(bills.size() + (bills.size() == 1 ? " record" : " records"));
        outstandingLabel.setText(String.format("GHS %.2f", billingDAO.getTotalOutstanding()));
    }

    @FXML
    private void handleAdd() {
        openBillingForm(null);
    }

    private void openBillingForm(Billing billingToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/billing_form.fxml"));
            Parent root = loader.load();
            BillingFormController formController = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(billingToEdit == null ? "New Bill" : "Edit Bill");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(SceneManager.createStyledScene(root));
            formController.setDialogStage(dialogStage);
            formController.setOnSaveCallback(this::applyFilters);
            if (billingToEdit != null) {
                formController.setBillingToEdit(billingToEdit);
            }
            dialogStage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open billing form: " + e.getMessage(), e);
        }
    }

    private void confirmAndDelete(Billing billing) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Delete this bill for " + billing.getPatientName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                billingDAO.delete(billing.getId());
                applyFilters();
            }
        });
    }

    private class ActionButtonsCell extends TableCell<Billing, Void> {
        private final Button editButton = new Button("Edit");
        private final Button deleteButton = new Button("Delete");
        private final HBox container = new HBox(6, editButton, deleteButton);

        ActionButtonsCell() {
            editButton.getStyleClass().add("btn-table-edit");
            deleteButton.getStyleClass().add("btn-table-delete");
            editButton.setOnAction(e -> openBillingForm(getTableView().getItems().get(getIndex())));
            deleteButton.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex())));
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : container);
        }
    }
}
