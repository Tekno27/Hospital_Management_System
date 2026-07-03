package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static Stage primaryStage;
    private static String stylesheetUrl;

    private SceneManager() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void switchTo(String fxmlPath) {
        loadRoot(fxmlPath);
    }

    public static <T> T switchToAndGetController(String fxmlPath) {
        return loadRoot(fxmlPath);
    }

    public static void openMainApp(String initialContentFxml, String navKey) {
        controllers.AppShellController shell = loadRoot("/views/app_shell.fxml");
        shell.loadContent(initialContentFxml, navKey);
    }

    public static void loadInShell(String contentFxml, String navKey) {
        controllers.AppShellController shell = controllers.AppShellController.getInstance();
        if (shell == null) {
            openMainApp(contentFxml, navKey);
            return;
        }
        shell.loadContent(contentFxml, navKey);
    }

    public static void loadDashboard() {
        loadInShell("/views/dashboard_content.fxml", "dashboard");
    }

    public static Scene createStyledScene(Parent root) {
        Scene scene = new Scene(root);
        applyStylesheet(scene);
        return scene;
    }

    private static <T> T loadRoot(String fxmlPath) {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage has not been set. Call setPrimaryStage() first.");
        }

        URL resourceUrl = resolveFxmlUrl(fxmlPath);
        try {
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Scene currentScene = primaryStage.getScene();
            if (currentScene == null) {
                Scene scene = new Scene(root, 1180, 720);
                applyStylesheet(scene);
                primaryStage.setScene(scene);
            } else {
                currentScene.setRoot(root);
                applyStylesheet(currentScene);
            }
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(640);
            primaryStage.show();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load screen " + fxmlPath + " from " + resourceUrl + ": " + e.getMessage(), e);
        }
    }

    private static void applyStylesheet(Scene scene) {
        String css = getStylesheetUrl();
        if (css != null && !scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    private static String getStylesheetUrl() {
        if (stylesheetUrl == null) {
            URL url = SceneManager.class.getResource("/styles/app.css");
            if (url == null) {
                url = SceneManager.class.getClassLoader().getResource("styles/app.css");
            }
            if (url != null) {
                stylesheetUrl = url.toExternalForm();
            }
        }
        return stylesheetUrl;
    }

    private static URL resolveFxmlUrl(String fxmlPath) {
        String classpathPath = fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;

        URL url = SceneManager.class.getResource(classpathPath);
        if (url == null) {
            String loaderPath = classpathPath.startsWith("/") ? classpathPath.substring(1) : classpathPath;
            url = SceneManager.class.getClassLoader().getResource(loaderPath);
        }
        if (url == null) {
            throw new RuntimeException(
                    "Screen file not found: " + fxmlPath
                            + ". Rebuild the project (Build > Rebuild Project) and run again.");
        }
        return url;
    }
}
