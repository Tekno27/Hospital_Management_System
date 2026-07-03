package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import utils.SceneManager;

public class SplashController {

    @FXML private ProgressBar loadingBar;
    @FXML private Label statusLabel;
    @FXML private VBox splashContent;

    private static final String[] STATUS_MESSAGES = {
            "Connecting to database...",
            "Loading patient records...",
            "Preparing pharmacy module...",
            "Configuring laboratory...",
            "Applying security policies...",
            "Welcome to MedCore HMS"
    };

    @FXML
    public void initialize() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), splashContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        Timeline timeline = new Timeline();
        int steps = 100;
        double stepDuration = 2400.0 / steps;

        for (int i = 0; i <= steps; i++) {
            final double progress = i / (double) steps;
            KeyFrame frame = new KeyFrame(Duration.millis(i * stepDuration), event -> {
                loadingBar.setProgress(progress);
                int messageIndex = Math.min(
                        STATUS_MESSAGES.length - 1,
                        (int) (progress * STATUS_MESSAGES.length)
                );
                statusLabel.setText(STATUS_MESSAGES[messageIndex]);
            });
            timeline.getKeyFrames().add(frame);
        }

        timeline.setOnFinished(event -> SceneManager.switchTo("/views/login.fxml"));
        timeline.play();
    }
}
