package com.lifelink.controller;

import com.lifelink.dao.HospitalDAO;
import com.lifelink.dao.UserDAO;
import com.lifelink.daoimpl.HospitalDAOImpl;
import com.lifelink.daoimpl.UserDAOImpl;
import com.lifelink.model.Hospital;
import com.lifelink.model.User;
import com.lifelink.service.AuthService;
import com.lifelink.util.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class HospitalController {

    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField contactField;
    @FXML private TextField emailField;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    @FXML private TextField searchField;
    @FXML private TableView<Hospital> hospitalTable;
    @FXML private TableColumn<Hospital, Integer> colId;
    @FXML private TableColumn<Hospital, String> colName;
    @FXML private TableColumn<Hospital, String> colAddress;
    @FXML private TableColumn<Hospital, String> colContact;
    @FXML private TableColumn<Hospital, String> colEmail;

    private final HospitalDAO hospitalDAO = new HospitalDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final AuthService authService = new AuthService();
    private final ObservableList<Hospital> hospitalList = FXCollections.observableArrayList();
    private Hospital selectedHospital = null;

    @FXML
    public void initialize() {
        // Bind Table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNo"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Load data
        loadHospitals();

        // Listen for selection changes
        hospitalTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        // Search Filter
        setupSearchFilter();

        // Button States
        setEditingMode(false);
    }

    private void loadHospitals() {
        hospitalList.clear();
        List<Hospital> all = hospitalDAO.readAll();
        hospitalList.addAll(all);
    }

    private void setupSearchFilter() {
        FilteredList<Hospital> filteredData = new FilteredList<>(hospitalList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(h -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lower = newValue.toLowerCase();
                if (h.getName().toLowerCase().contains(lower)) {
                    return true;
                } else if (h.getAddress().toLowerCase().contains(lower)) {
                    return true;
                } else if (h.getEmail().toLowerCase().contains(lower)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Hospital> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(hospitalTable.comparatorProperty());
        hospitalTable.setItems(sortedData);
    }

    private void populateForm(Hospital h) {
        selectedHospital = h;
        nameField.setText(h.getName());
        addressField.setText(h.getAddress());
        contactField.setText(h.getContactNo());
        emailField.setText(h.getEmail());

        setEditingMode(true);
    }

    private void setEditingMode(boolean isEditing) {
        addButton.setDisable(isEditing);
        updateButton.setDisable(!isEditing);
        deleteButton.setDisable(!isEditing);
    }

    @FXML
    private void handleAddHospital() {
        if (!validateInputs()) return;

        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();

        // Auto-generate credentials for hospital user login
        String sanitizedName = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = sanitizedName + "_hosp_" + (System.currentTimeMillis() % 1000);
        String password = "hospital123";

        // Register hospital
        User user = authService.registerHospital(username, password, email, name, address, contact);
        if (user != null) {
            showInfoAlert("Success", "Hospital Created", "Hospital profile and system user login generated successfully.\nUsername: " + username + "\nPassword: " + password);
            loadHospitals();
            handleClearForm();
        } else {
            showErrorAlert("Error", "Addition Failed", "Could not insert hospital. Make sure email/username is unique.");
        }
    }

    @FXML
    private void handleUpdateHospital() {
        if (selectedHospital == null) return;
        if (!validateInputs()) return;

        String oldEmail = selectedHospital.getEmail();
        selectedHospital.setName(nameField.getText().trim());
        selectedHospital.setAddress(addressField.getText().trim());
        selectedHospital.setContactNo(contactField.getText().trim());
        selectedHospital.setEmail(emailField.getText().trim());

        if (hospitalDAO.update(selectedHospital)) {
            // Check if email changed; if so, update corresponding user email too
            if (!oldEmail.equalsIgnoreCase(selectedHospital.getEmail())) {
                User user = userDAO.readByUsername(oldEmail); // wait, username might equal oldEmail or we can query users
                // Find user in DB by email
                try (java.sql.Connection conn = com.lifelink.database.DBConnection.getConnection();
                     java.sql.PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET email = ? WHERE email = ?")) {
                    pstmt.setString(1, selectedHospital.getEmail());
                    pstmt.setString(2, oldEmail);
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.err.println("Could not synchronize user email: " + e.getMessage());
                }
            }

            showInfoAlert("Success", "Hospital Updated", "Hospital profile updated successfully.");
            loadHospitals();
            handleClearForm();
        } else {
            showErrorAlert("Error", "Update Failed", "Could not write hospital update to database.");
        }
    }

    @FXML
    private void handleDeleteHospital() {
        if (selectedHospital == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Hospital?");
        confirm.setContentText("This will delete: " + selectedHospital.getName() + " and its corresponding user account. Continue?");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                // Delete user account first to maintain system integrity, then hospital
                try (java.sql.Connection conn = com.lifelink.database.DBConnection.getConnection();
                     java.sql.PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE email = ?")) {
                    pstmt.setString(1, selectedHospital.getEmail());
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.err.println("Error removing linked user account: " + e.getMessage());
                }

                if (hospitalDAO.delete(selectedHospital.getId())) {
                    showInfoAlert("Success", "Hospital Deleted", "Hospital profile removed successfully.");
                    loadHospitals();
                    handleClearForm();
                } else {
                    showErrorAlert("Error", "Delete Failed", "Could not remove hospital record.");
                }
            }
        });
    }

    @FXML
    private void handleClearForm() {
        nameField.clear();
        addressField.clear();
        contactField.clear();
        emailField.clear();

        selectedHospital = null;
        hospitalTable.getSelectionModel().clearSelection();
        setEditingMode(false);
    }

    @FXML
    private void handleRefresh() {
        loadHospitals();
    }

    private boolean validateInputs() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || address.isEmpty() || contact.isEmpty() || email.isEmpty()) {
            showWarningAlert("Validation Error", "Missing Fields", "Please complete all fields marked with an asterisk (*).");
            return false;
        }

        if (!Validator.isValidEmail(email)) {
            showWarningAlert("Validation Error", "Invalid Email", "Please enter a valid email format.");
            return false;
        }

        if (!Validator.isValidPhone(contact)) {
            showWarningAlert("Validation Error", "Invalid Phone", "Please input a valid phone format.");
            return false;
        }

        return true;
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
