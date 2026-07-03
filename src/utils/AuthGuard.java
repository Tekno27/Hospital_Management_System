package utils;

import javafx.scene.control.Alert;

public final class AuthGuard {

    private AuthGuard() {
    }

    public static boolean requireLogin() {
        if (Session.isLoggedIn()) {
            return true;
        }
        SceneManager.switchTo("/views/login.fxml");
        return false;
    }

    public static boolean requireModule(Rbac.Module module) {
        if (!requireLogin()) {
            return false;
        }
        if (Rbac.canAccess(module)) {
            return true;
        }
        showAccessDenied();
        SceneManager.loadDashboard();
        return false;
    }

    private static void showAccessDenied() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText(null);
        alert.setContentText("You do not have permission to access that section.");
        alert.showAndWait();
    }
}
