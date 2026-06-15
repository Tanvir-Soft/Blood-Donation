package com.lifelink.controller;

import com.lifelink.model.User;
import com.lifelink.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for LoginView.fxml.
 * Handles user authentication, validation, and navigation to registration or dashboards.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Hyperlink forgotPasswordLink;

    private final AuthService authService = new AuthService();

    // -------------------------------------------------------
    // Login Handler
    // -------------------------------------------------------
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // --- Validation ---
        if (username.isEmpty() && password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Missing Credentials",
                    "Please enter your username and password to sign in.");
            usernameField.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Username Required",
                    "Please enter your username.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Password Required",
                    "Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        // --- Authentication ---
        User user = authService.login(username, password);
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Login Failed",
                    "Invalid Credentials",
                    "The username or password you entered is incorrect.\nPlease try again.");
            passwordField.clear();
            passwordField.requestFocus();
            return;
        }

        // --- Success: Navigate based on role ---
        System.out.println("[LoginController] Login successful. User: " + user.getUsername() + " | Role: " + user.getRole());
        navigateToDashboard(user);
    }

    // -------------------------------------------------------
    // Register Handler
    // -------------------------------------------------------
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/lifelink/view/RegisterView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root, 950, 620);
            if (getClass().getResource("/com/lifelink/css/style.css") != null) {
                scene.getStylesheets().add(
                        getClass().getResource("/com/lifelink/css/style.css").toExternalForm());
            }
            stage.setTitle("LifeLink - Create Account");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Failed to load RegisterView.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could Not Open Registration",
                    "Registration screen is not available yet. Please try again later.");
        }
    }

    // -------------------------------------------------------
    // Forgot Password Handler
    // -------------------------------------------------------
    @FXML
    private void handleForgotPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password",
                "Password Recovery",
                "Please contact the system administrator to reset your password.\n\nEmail: admin@lifelink.com");
    }

    // -------------------------------------------------------
    // Navigation based on user role
    // -------------------------------------------------------
    private void navigateToDashboard(User user) {
        String fxmlFile;
        String title;

        switch (user.getRole().toUpperCase()) {
            case "ADMIN":
                fxmlFile = "/com/lifelink/view/DashboardView.fxml";
                title = "LifeLink - Admin Dashboard";
                break;
            case "DONOR":
                fxmlFile = "/com/lifelink/view/DashboardView.fxml";
                title = "LifeLink - Donor Dashboard";
                break;
            case "HOSPITAL":
                fxmlFile = "/com/lifelink/view/DashboardView.fxml";
                title = "LifeLink - Hospital Dashboard";
                break;
            default:
                fxmlFile = "/com/lifelink/view/DashboardView.fxml";
                title = "LifeLink - Dashboard";
                break;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Pass user context to dashboard controller if it implements UserAware
            Object controller = loader.getController();
            if (controller instanceof UserAwareController userAware) {
                userAware.initializeUser(user);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 720);
            if (getClass().getResource("/com/lifelink/css/style.css") != null) {
                scene.getStylesheets().add(
                        getClass().getResource("/com/lifelink/css/style.css").toExternalForm());
            }
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Dashboard not yet created: " + e.getMessage());
            // Dashboard not yet built — show a welcome alert for now
            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Welcome, " + user.getUsername() + "!",
                    "You are logged in as: " + user.getRole()
                    + "\n\nThe dashboard for your role will be available in the next phase.");
        }
    }

    // -------------------------------------------------------
    // Reusable Alert Helper
    // -------------------------------------------------------
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
