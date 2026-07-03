import database.DatabaseConnection;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import utils.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);
        stage.setTitle("MedCore HMS");
        stage.setResizable(true);

        try {
            DatabaseConnection.initialize();
        } catch (RuntimeException e) {
            showStartupError(stage, e);
            return;
        }

        SceneManager.switchTo("/views/splash.fxml");
    }

    private void showStartupError(Stage stage, RuntimeException e) {
        System.err.println("Startup failed: " + e.getMessage());
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Startup Error");
        alert.setHeaderText("Could not initialize the database");
        alert.setContentText(e.getMessage() + "\n\nDatabase file: " + DatabaseConnection.getDatabasePath());
        alert.showAndWait();
        stage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
