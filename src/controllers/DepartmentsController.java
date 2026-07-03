package controllers;

import database.DepartmentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Department;
import utils.AuthGuard;
import utils.Rbac;
import utils.SceneManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DepartmentsController {

    @FXML private Label recordCountLabel;
    @FXML private TextField searchField;
    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, Integer> colId;
    @FXML private TableColumn<Department, String> colName;
    @FXML private TableColumn<Department, String> colFloor;
    @FXML private TableColumn<Department, String> colHead;
    @FXML private TableColumn<Department, Integer> colBeds;
    @FXML private TableColumn<Department, Integer> colDoctors;
    @FXML private TableColumn<Department, Integer> colPatients;
    @FXML private TableColumn<Department, String> colPhone;
    @FXML private TableColumn<Department, Void> colActions;

    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.DEPARTMENTS)) return;

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));
        colHead.setCellValueFactory(new PropertyValueFactory<>("headDoctorName"));
        colBeds.setCellValueFactory(new PropertyValueFactory<>("bedCapacity"));
        colDoctors.setCellValueFactory(new PropertyValueFactory<>("doctorCount"));
        colPatients.setCellValueFactory(new PropertyValueFactory<>("patientCount"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colActions.setCellFactory(col -> new ActionCell());

        applyFilters();
    }

    @FXML private void handleSearch() { applyFilters(); }

    @FXML private void handleClearFilters() {
        searchField.clear();
        applyFilters();
    }

    @FXML private void handleAdd() { openForm(null); }

    private void applyFilters() {
        String keyword = searchField.getText().trim();
        List<Department> departments = keyword.isEmpty()
                ? departmentDAO.getAll()
                : departmentDAO.search(keyword);
        departmentTable.setItems(FXCollections.observableArrayList(departments));
        recordCountLabel.setText(departments.size() + (departments.size() == 1 ? " department" : " departments"));
    }

    private void openForm(Department department) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/department_form.fxml"));
            Parent root = loader.load();
            DepartmentFormController controller = loader.getController();
            Stage dialog = new Stage();
            dialog.setTitle(department == null ? "Add Department" : "Edit Department");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(SceneManager.createStyledScene(root));
            controller.setDialogStage(dialog);
            controller.setOnSaveCallback(this::applyFilters);
            if (department != null) controller.setDepartmentToEdit(department);
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open department form: " + e.getMessage(), e);
        }
    }

    private class ActionCell extends TableCell<Department, Void> {
        private final Button editBtn = new Button("Edit");
        private final Button deleteBtn = new Button("Delete");
        private final HBox box = new HBox(6, editBtn, deleteBtn);

        ActionCell() {
            editBtn.getStyleClass().add("btn-table-edit");
            deleteBtn.getStyleClass().add("btn-table-delete");
            editBtn.setOnAction(e -> openForm(getTableView().getItems().get(getIndex())));
            deleteBtn.setOnAction(e -> {
                Department dep = getTableView().getItems().get(getIndex());
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + dep.getName() + "?",
                        ButtonType.OK, ButtonType.CANCEL);
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        departmentDAO.delete(dep.getId());
                        applyFilters();
                    }
                });
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : box);
        }
    }
}
