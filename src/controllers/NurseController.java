package controllers;

import database.NurseDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Nurse;
import utils.AuthGuard;
import utils.Rbac;

import java.util.List;
import java.util.stream.Collectors;

public class NurseController {

    @FXML private Label recordCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> shiftFilter;
    @FXML private TableView<Nurse> nurseTable;
    @FXML private TableColumn<Nurse, Integer> colId;
    @FXML private TableColumn<Nurse, String> colName;
    @FXML private TableColumn<Nurse, String> colWard;
    @FXML private TableColumn<Nurse, String> colShift;
    @FXML private TableColumn<Nurse, String> colPhone;
    @FXML private TableColumn<Nurse, String> colEmail;

    private final NurseDAO nurseDAO = new NurseDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.NURSES)) {
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colWard.setCellValueFactory(new PropertyValueFactory<>("ward"));
        colShift.setCellValueFactory(new PropertyValueFactory<>("shift"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        shiftFilter.setItems(FXCollections.observableArrayList("All", "MORNING", "AFTERNOON", "NIGHT"));
        shiftFilter.setValue("All");
        applyFilters();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        shiftFilter.setValue("All");
        applyFilters();
    }

    private void applyFilters() {
        List<Nurse> nurses;
        String keyword = searchField.getText().trim();
        nurses = keyword.isEmpty() ? nurseDAO.getAll() : nurseDAO.search(keyword);

        String shift = shiftFilter.getValue();
        if (shift != null && !"All".equals(shift)) {
            nurses = nurses.stream().filter(n -> shift.equals(n.getShift())).collect(Collectors.toList());
        }

        nurseTable.setItems(FXCollections.observableArrayList(nurses));
        recordCountLabel.setText(nurses.size() + (nurses.size() == 1 ? " record" : " records"));
    }
}
