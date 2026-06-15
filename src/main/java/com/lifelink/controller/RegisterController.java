package com.lifelink.controller;

import com.lifelink.model.User;
import com.lifelink.service.AuthService;
import com.lifelink.util.Constants;
import com.lifelink.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for RegisterView.fxml.
 * Handles Donor and Blood Seeker registration with full field validation.
 */
public class RegisterController {

    // ---- Common Fields ----
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;

    // ---- Role Toggle ----
    @FXML private ToggleButton donorToggle;
    @FXML private ToggleButton seekerToggle;
    @FXML private ToggleGroup roleGroup;

    // ---- Donor-only Fields ----
    @FXML private VBox donorFieldsBox;
    @FXML private TextField ageField;
    @FXML private TextField weightField;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField cityField;

    // ---- Buttons & Links ----
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;

    private final AuthService authService = new AuthService();

    // -------------------------------------------------------
    // Initialization
    // -------------------------------------------------------
    @FXML
    public void initialize() {
        // Populate blood group dropdown
        bloodGroupCombo.setItems(FXCollections.observableArrayList(Constants.BLOOD_GROUPS));

        // Ensure donor fields visible by default (Donor toggle selected)
        donorFieldsBox.setVisible(true);
        donorFieldsBox.setManaged(true);

        // Ensure toggle group coherence
        if (roleGroup != null) {
            roleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    // Prevent deselection — re-select old
                    if (oldVal != null) oldVal.setSelected(true);
                }
            });
        }
    }

    // -------------------------------------------------------
    // Role Toggle Handler
    // -------------------------------------------------------
    @FXML
    private void handleRoleToggle() {
        boolean isDonor = donorToggle.isSelected();

        donorFieldsBox.setVisible(isDonor);
        donorFieldsBox.setManaged(isDonor);

        // Update button styles for active/inactive state
        if (isDonor) {
            donorToggle.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-font-size: 12; "
                    + "-fx-background-radius: 8 0 0 8; -fx-cursor: hand; -fx-padding: 10;");
            seekerToggle.setStyle("-fx-background-color: white; -fx-text-fill: #E53935; "
                    + "-fx-font-weight: bold; -fx-font-size: 12; "
                    + "-fx-border-color: #E53935; -fx-border-width: 1 1 1 0; "
                    + "-fx-background-radius: 0 8 8 0; -fx-cursor: hand; -fx-padding: 10;");
        } else {
            seekerToggle.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-font-size: 12; "
                    + "-fx-background-radius: 0 8 8 0; -fx-cursor: hand; -fx-padding: 10;");
            donorToggle.setStyle("-fx-background-color: white; -fx-text-fill: #E53935; "
                    + "-fx-font-weight: bold; -fx-font-size: 12; "
                    + "-fx-border-color: #E53935; -fx-border-width: 1 1 1 1; "
                    + "-fx-background-radius: 8 0 0 8; -fx-cursor: hand; -fx-padding: 10;");
        }
    }

    // -------------------------------------------------------
    // Register Handler
    // -------------------------------------------------------
    @FXML
    private void handleRegister() {
        // 1. Gather common input
        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String phone    = phoneField.getText().trim();

        // 2. Validate common fields
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Missing Required Fields",
                    "Please fill in all required fields marked with *.");
            return;
        }
        if (!Validator.isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Invalid Email",
                    "Please enter a valid email address (e.g. name@domain.com).");
            emailField.requestFocus();
            return;
        }
        if (!Validator.isValidPassword(password)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Weak Password",
                    "Password must be at least 6 characters long.");
            passwordField.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Passwords Do Not Match",
                    "The password and confirm password fields must match.");
            confirmPasswordField.clear();
            confirmPasswordField.requestFocus();
            return;
        }
        if (!Validator.isValidPhone(phone)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Invalid Phone Number",
                    "Please enter a valid phone number (7 to 15 digits).");
            phoneField.requestFocus();
            return;
        }

        // 3. Determine role and register
        boolean isDonor = donorToggle.isSelected();
        User registeredUser = null;

        if (isDonor) {
            // Validate donor-specific fields
            String ageText    = ageField.getText().trim();
            String weightText = weightField.getText().trim();
            String bloodGroup = bloodGroupCombo.getValue();
            String city       = cityField.getText().trim();

            if (ageText.isEmpty() || weightText.isEmpty() || bloodGroup == null || city.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error",
                        "Missing Donor Details",
                        "Please fill in all donor fields: Age, Weight, Blood Group, and City.");
                return;
            }

            int age;
            double weight;
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error",
                        "Invalid Age", "Age must be a whole number (e.g. 25).");
                ageField.requestFocus();
                return;
            }
            try {
                weight = Double.parseDouble(weightText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error",
                        "Invalid Weight", "Weight must be a number (e.g. 65.5).");
                weightField.requestFocus();
                return;
            }

            if (!Validator.isValidAge(age)) {
                showAlert(Alert.AlertType.WARNING, "Validation Error",
                        "Age Out of Range",
                        "Donors must be between 18 and 65 years old.");
                ageField.requestFocus();
                return;
            }
            if (weight < 45) {
                showAlert(Alert.AlertType.WARNING, "Validation Error",
                        "Weight Too Low",
                        "Donors must weigh at least 45 kg to be eligible.");
                weightField.requestFocus();
                return;
            }

            registeredUser = authService.registerDonor(
                    username, password, email, fullName, age, weight, bloodGroup, city);

        } else {
            // Register as Seeker
            registeredUser = authService.registerSeeker(username, password, email);
        }

        // 4. Handle result
        if (registeredUser != null) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                    "Welcome to LifeLink, " + fullName + "!",
                    "Your account has been created successfully.\n"
                    + "You can now sign in with your username: " + username);
            navigateToLogin();
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed",
                    "Could Not Create Account",
                    "The username or email may already be in use.\n"
                    + "Please try a different username or email address.");
        }
    }

    // -------------------------------------------------------
    // Back to Login
    // -------------------------------------------------------
    @FXML
    private void handleBackToLogin() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/lifelink/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 580);
            if (getClass().getResource("/com/lifelink/css/style.css") != null) {
                scene.getStylesheets().add(
                        getClass().getResource("/com/lifelink/css/style.css").toExternalForm());
            }
            stage.setTitle("LifeLink - Sign In");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Failed to load LoginView.fxml: " + e.getMessage());
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
