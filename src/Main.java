import javafx.application.Application;
import javafx.stage.Stage;
import utils.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        SceneManager.setPrimaryStage(stage);

        stage.setTitle("MedCore HMS");
        stage.setResizable(true);

        SceneManager.switchTo("/views/splash.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}