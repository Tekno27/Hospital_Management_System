package controllers;

import database.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.User;
import utils.SceneManager;
import utils.Session;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordButton;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UserDAO userDAO = new UserDAO();
    private boolean passwordVisible;

    @FXML
    public void initialize() {
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        visiblePasswordField.setOnAction(e -> handleLogin());

        passwordField.textProperty().addListener((obs, o, n) -> {
            if (!passwordVisible) {
                visiblePasswordField.setText(n);
            }
        });
        visiblePasswordField.textProperty().addListener((obs, o, n) -> {
            if (passwordVisible) {
                passwordField.setText(n);
            }
        });

        if (loginButton != null) {
            loginButton.setDefaultButton(true);
        }

        usernameField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                usernameField.requestFocus();
            }
        });
    }

    @FXML
    private void handleTogglePassword() {
        passwordVisible = !passwordVisible;
        String current = passwordVisible ? passwordField.getText() : visiblePasswordField.getText();
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        visiblePasswordField.setVisible(passwordVisible);
        visiblePasswordField.setManaged(passwordVisible);
        if (passwordVisible) {
            visiblePasswordField.setText(current);
            visiblePasswordField.requestFocus();
            visiblePasswordField.positionCaret(visiblePasswordField.getText().length());
        } else {
            passwordField.setText(current);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
        togglePasswordButton.setText(passwordVisible ? "🙈" : "👁");
    }

    @FXML
    private void handleLogin() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String username = usernameField.getText().trim();
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

        if (username.isEmpty()) {
            showError("Please enter your username.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            (passwordVisible ? visiblePasswordField : passwordField).requestFocus();
            return;
        }

        User user;
        try {
            user = userDAO.authenticate(username, password);
        } catch (RuntimeException e) {
            showError("Database error: " + e.getMessage());
            return;
        }

        if (user == null) {
            showError("Invalid username or password.");
            passwordField.clear();
            visiblePasswordField.clear();
            (passwordVisible ? visiblePasswordField : passwordField).requestFocus();
            return;
        }

        try {
            Session.setCurrentUser(user);
            SceneManager.openMainApp("/views/dashboard_content.fxml", "dashboard");
        } catch (RuntimeException e) {
            Session.clear();
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            showError("Signed in, but the app could not load: " + cause.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
