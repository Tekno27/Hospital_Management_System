package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.AppSettings;
import utils.AuthGuard;
import utils.Rbac;

public class SettingsController {

    @FXML private TextField hospitalNameField;
    @FXML private CheckBox emailNotificationsCheck;
    @FXML private CheckBox compactTablesCheck;
    @FXML private CheckBox showChartsCheck;
    @FXML private CheckBox autoRefreshCheck;
    @FXML private Label saveStatusLabel;

    @FXML
    public void initialize() {
        if (!AuthGuard.requireModule(Rbac.Module.SETTINGS)) return;

        AppSettings.load();
        hospitalNameField.setText(AppSettings.get("hospitalName"));
        emailNotificationsCheck.setSelected(AppSettings.getBoolean("emailNotifications"));
        compactTablesCheck.setSelected(AppSettings.getBoolean("compactTables"));
        showChartsCheck.setSelected(AppSettings.getBoolean("showCharts"));
        autoRefreshCheck.setSelected(AppSettings.getBoolean("autoRefreshDashboard"));
    }

    @FXML
    private void handleSave() {
        AppSettings.set("hospitalName", hospitalNameField.getText().trim());
        AppSettings.setBoolean("emailNotifications", emailNotificationsCheck.isSelected());
        AppSettings.setBoolean("compactTables", compactTablesCheck.isSelected());
        AppSettings.setBoolean("showCharts", showChartsCheck.isSelected());
        AppSettings.setBoolean("autoRefreshDashboard", autoRefreshCheck.isSelected());
        AppSettings.save();
        saveStatusLabel.setText("Settings saved successfully.");
        saveStatusLabel.setVisible(true);
    }

    @FXML
    private void handleReset() {
        hospitalNameField.setText("MedCore HMS");
        emailNotificationsCheck.setSelected(true);
        compactTablesCheck.setSelected(false);
        showChartsCheck.setSelected(true);
        autoRefreshCheck.setSelected(true);
        saveStatusLabel.setText("Reset to defaults — click Save to apply.");
        saveStatusLabel.setVisible(true);
    }
}
