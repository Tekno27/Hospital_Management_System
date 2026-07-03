package controllers;

import database.WardDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import models.Ward;
import utils.AuthGuard;
import utils.PatientAccess;
import utils.Rbac;
import utils.SceneManager;
import utils.Session;

import java.util.List;
import java.util.stream.Collectors;

public class WardController {

    @FXML private Label recordCountLabel;
    @FXML private Label summaryLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private TableView<Ward> wardTable;
    @FXML private TableColumn<Ward, String> colName;
    @FXML private TableColumn<Ward, String> colDepartment;
    @FXML private TableColumn<Ward, String> colFloor;
    @FXML private TableColumn<Ward, Integer> colCapacity;
    @FXML private TableColumn<Ward, Integer> colOccupied;
    @FXML private TableColumn<Ward, Integer> colAvailable;
    @FXML private TableColumn<Ward, Integer> colNurses;
    @FXML private TableColumn<Ward, Void> colOccupancy;
    @FXML private TableColumn<Ward, Void> colActions;

    private final WardDAO wardDAO = new WardDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.WARD)) return;

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("bedCapacity"));
        colOccupied.setCellValueFactory(new PropertyValueFactory<>("occupied"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));
        colNurses.setCellValueFactory(new PropertyValueFactory<>("nurseCount"));
        colOccupancy.setCellFactory(col -> new OccupancyCell());
        colActions.setCellFactory(col -> new ViewPatientsCell());

        List<String> departments = wardDAO.getAllWithStats().stream()
                .map(Ward::getDepartment)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        departments.add(0, "All");
        departmentFilter.setItems(FXCollections.observableArrayList(departments));
        departmentFilter.setValue("All");

        applyFilters();
    }

    @FXML private void handleSearch() { applyFilters(); }

    @FXML private void handleClearFilters() {
        searchField.clear();
        departmentFilter.setValue("All");
        applyFilters();
    }

    private void applyFilters() {
        List<Ward> wards;
        String keyword = searchField.getText().trim();
        wards = keyword.isEmpty() ? wardDAO.getAllWithStats() : wardDAO.search(keyword);

        String dept = departmentFilter.getValue();
        if (dept != null && !"All".equals(dept)) {
            wards = wards.stream().filter(w -> dept.equals(w.getDepartment())).collect(Collectors.toList());
        }

        var user = Session.getCurrentUser();
        if (user != null && user.isNurse()) {
            String nurseWard = PatientAccess.nurseWard();
            if (nurseWard != null) {
                wards = wards.stream().filter(w -> nurseWard.equals(w.getName())).collect(Collectors.toList());
            }
        }

        wardTable.setItems(FXCollections.observableArrayList(wards));
        recordCountLabel.setText(wards.size() + (wards.size() == 1 ? " ward" : " wards"));

        int totalBeds = wards.stream().mapToInt(Ward::getBedCapacity).sum();
        int totalOccupied = wards.stream().mapToInt(Ward::getOccupied).sum();
        if (summaryLabel != null) {
            summaryLabel.setText(totalOccupied + " / " + totalBeds + " beds occupied");
        }
    }

    private class OccupancyCell extends TableCell<Ward, Void> {
        private final ProgressBar bar = new ProgressBar(0);
        private final HBox box = new HBox(8);

        OccupancyCell() {
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.setPrefHeight(8);
            HBox.setHgrow(bar, Priority.ALWAYS);
            Label pct = new Label();
            pct.getStyleClass().add("occupancy-count");
            box.getChildren().addAll(bar, pct);
            box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                return;
            }
            Ward ward = getTableView().getItems().get(getIndex());
            bar.setProgress(ward.getOccupancyRate() / 100.0);
            ((Label) box.getChildren().get(1)).setText(String.format("%.0f%%", ward.getOccupancyRate()));
            setGraphic(box);
        }
    }

    private class ViewPatientsCell extends TableCell<Ward, Void> {
        private final Button viewBtn = new Button("View Patients");

        ViewPatientsCell() {
            viewBtn.getStyleClass().add("btn-table-edit");
            viewBtn.setOnAction(e -> {
                Ward ward = getTableView().getItems().get(getIndex());
                Session.setWardFilter(ward.getName());
                SceneManager.loadInShell("/views/patients.fxml", "patients");
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : viewBtn);
        }
    }
}
