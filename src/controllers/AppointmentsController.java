package controllers;

import database.AppointmentDAO;
import database.DoctorDAO;
import database.PatientDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Appointment;
import models.User;
import utils.AuthGuard;
import utils.Rbac;
import utils.SceneManager;
import utils.Session;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentsController {

    @FXML private Label recordCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button addButton;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, Integer> colId;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colDate;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, Void> colActions;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.APPOINTMENTS)) {
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("appointmentTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colActions.setCellFactory(col -> new ActionCell());

        statusFilter.setItems(FXCollections.observableArrayList("All", "SCHEDULED", "COMPLETED", "CANCELLED", "NO_SHOW"));
        statusFilter.setValue("All");

        boolean canManage = Session.getCurrentUser() != null
                && (Session.getCurrentUser().isAdmin() || Session.getCurrentUser().isDoctor());
        addButton.setVisible(canManage);
        addButton.setManaged(canManage);

        applyFilters();
    }

    @FXML
    private void handleSearch() { applyFilters(); }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilter.setValue("All");
        applyFilters();
    }

    @FXML
    private void handleAdd() {
        openForm(null);
    }

    private void applyFilters() {
        User user = Session.getCurrentUser();
        List<Appointment> appointments;
        if (user.isAdmin()) {
            appointments = appointmentDAO.getAll();
        } else if (user.isDoctor() && user.getLinkedStaffId() != null) {
            appointments = appointmentDAO.getByDoctorId(user.getLinkedStaffId());
        } else {
            appointments = List.of();
        }

        String keyword = searchField.getText().trim();
        if (!keyword.isEmpty()) {
            String lower = keyword.toLowerCase();
            appointments = appointments.stream()
                    .filter(a -> a.getPatientName().toLowerCase().contains(lower)
                            || a.getDoctorName().toLowerCase().contains(lower)
                            || (a.getReason() != null && a.getReason().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }

        String status = statusFilter.getValue();
        if (status != null && !"All".equals(status)) {
            appointments = appointments.stream().filter(a -> status.equals(a.getStatus())).collect(Collectors.toList());
        }

        appointmentTable.setItems(FXCollections.observableArrayList(appointments));
        recordCountLabel.setText(appointments.size() + (appointments.size() == 1 ? " appointment" : " appointments"));
    }

    private void openForm(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/appointment_form.fxml"));
            Parent root = loader.load();
            AppointmentFormController controller = loader.getController();
            Stage dialog = new Stage();
            dialog.setTitle(appointment == null ? "Schedule Appointment" : "Edit Appointment");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(SceneManager.createStyledScene(root));
            controller.setDialogStage(dialog);
            controller.setOnSaveCallback(this::applyFilters);
            if (appointment != null) {
                controller.setAppointmentToEdit(appointment);
            }
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open appointment form: " + e.getMessage(), e);
        }
    }

    private class ActionCell extends TableCell<Appointment, Void> {
        private final Button editBtn = new Button("Edit");
        private final Button deleteBtn = new Button("Delete");
        private final HBox box = new HBox(6, editBtn, deleteBtn);

        ActionCell() {
            editBtn.getStyleClass().add("btn-table-edit");
            deleteBtn.getStyleClass().add("btn-table-delete");
            editBtn.setOnAction(e -> openForm(getTableView().getItems().get(getIndex())));
            deleteBtn.setOnAction(e -> {
                Appointment appt = getTableView().getItems().get(getIndex());
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this appointment?",
                        ButtonType.OK, ButtonType.CANCEL);
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        appointmentDAO.delete(appt.getId());
                        applyFilters();
                    }
                });
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                return;
            }
            User user = Session.getCurrentUser();
            boolean canEdit = user != null && (user.isAdmin() || user.isDoctor());
            editBtn.setVisible(canEdit);
            deleteBtn.setVisible(user != null && user.isAdmin());
            setGraphic(box);
        }
    }
}
