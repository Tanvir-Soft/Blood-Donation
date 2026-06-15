package com.lifelink.controller;

import com.lifelink.dao.DonorDAO;
import com.lifelink.dao.UserDAO;
import com.lifelink.daoimpl.DonorDAOImpl;
import com.lifelink.daoimpl.UserDAOImpl;
import com.lifelink.model.Donor;
import com.lifelink.model.User;
import com.lifelink.service.AuthService;
import com.lifelink.util.Constants;
import com.lifelink.util.Validator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class DonorController {

    @FXML private TextField nameField;
    @FXML private TextField ageField;
    @FXML private TextField weightField;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField cityField;
    @FXML private DatePicker lastDonationDatePicker;
    @FXML private CheckBox availabilityCheckBox;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    @FXML private TextField searchField;
    @FXML private TableView<Donor> donorTable;
    @FXML private TableColumn<Donor, Integer> colId;
    @FXML private TableColumn<Donor, String> colName;
    @FXML private TableColumn<Donor, Integer> colAge;
    @FXML private TableColumn<Donor, Double> colWeight;
    @FXML private TableColumn<Donor, String> colBloodGroup;
    @FXML private TableColumn<Donor, String> colCity;
    @FXML private TableColumn<Donor, String> colLastDonation;
    @FXML private TableColumn<Donor, String> colAvailability;

    private final DonorDAO donorDAO = new DonorDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final AuthService authService = new AuthService();
    private final ObservableList<Donor> donorList = FXCollections.observableArrayList();
    private Donor selectedDonor = null;

    @FXML
    public void initialize() {
        // Setup dropdown
        bloodGroupCombo.setItems(FXCollections.observableArrayList(Constants.BLOOD_GROUPS));

        // Setup columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colBloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        
        colLastDonation.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getLastDonationDate();
            return new SimpleStringProperty(date != null ? date.toString() : "Never");
        });
        
        colAvailability.setCellValueFactory(cellData -> {
            boolean available = cellData.getValue().isAvailable();
            return new SimpleStringProperty(available ? "ELIGIBLE" : "INELIGIBLE");
        });

        // Load data
        loadDonors();

        // Listen for Table Selection
        donorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        // Listen for Search field input
        setupSearchFilter();
        
        // Initial form state
        setEditingMode(false);
    }

    private void loadDonors() {
        donorList.clear();
        List<Donor> allDonors = donorDAO.readAll();
        donorList.addAll(allDonors);
    }

    private void setupSearchFilter() {
        FilteredList<Donor> filteredData = new FilteredList<>(donorList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(donor -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (donor.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (donor.getCity().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (donor.getBloodGroup().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; 
            });
        });

        SortedList<Donor> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(donorTable.comparatorProperty());
        donorTable.setItems(sortedData);
    }

    private void populateForm(Donor donor) {
        selectedDonor = donor;
        nameField.setText(donor.getName());
        ageField.setText(String.valueOf(donor.getAge()));
        weightField.setText(String.valueOf(donor.getWeight()));
        bloodGroupCombo.setValue(donor.getBloodGroup());
        cityField.setText(donor.getCity());
        lastDonationDatePicker.setValue(donor.getLastDonationDate());
        availabilityCheckBox.setSelected(donor.isAvailable());

        setEditingMode(true);
    }

    private void setEditingMode(boolean isEditing) {
        addButton.setDisable(isEditing);
        updateButton.setDisable(!isEditing);
        deleteButton.setDisable(!isEditing);
    }

    @FXML
    private void handleAddDonor() {
        if (!validateInputs()) return;

        String name = nameField.getText().trim();
        int age = Integer.parseInt(ageField.getText().trim());
        double weight = Double.parseDouble(weightField.getText().trim());
        String bloodGroup = bloodGroupCombo.getValue();
        String city = cityField.getText().trim();
        LocalDate lastDonation = lastDonationDatePicker.getValue();
        boolean available = availabilityCheckBox.isSelected();

        // Admin adding: Generate linked User credentials automatically
        String sanitizedName = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = sanitizedName + "_" + (System.currentTimeMillis() % 10000);
        String password = "password123";
        String email = username + "@lifelink.com";

        // Register through AuthService
        User user = authService.registerDonor(username, password, email, name, age, weight, bloodGroup, city);
        if (user != null) {
            // AuthService inserts both User and Donor, let's update availability if selected otherwise
            Donor registeredDonor = donorDAO.readByUserId(user.getId());
            if (registeredDonor != null) {
                registeredDonor.setLastDonationDate(lastDonation);
                registeredDonor.setAvailable(available);
                donorDAO.update(registeredDonor);
            }
            
            showInfoAlert("Success", "Donor Added", "Donor profile created successfully.\nDefault Credentials:\nUsername: " + username + "\nPassword: " + password);
            loadDonors();
            handleClearForm();
        } else {
            showErrorAlert("Error", "Addition Failed", "Could not create donor profile. Make sure database is running.");
        }
    }

    @FXML
    private void handleUpdateDonor() {
        if (selectedDonor == null) return;
        if (!validateInputs()) return;

        selectedDonor.setName(nameField.getText().trim());
        selectedDonor.setAge(Integer.parseInt(ageField.getText().trim()));
        selectedDonor.setWeight(Double.parseDouble(weightField.getText().trim()));
        selectedDonor.setBloodGroup(bloodGroupCombo.getValue());
        selectedDonor.setCity(cityField.getText().trim());
        selectedDonor.setLastDonationDate(lastDonationDatePicker.getValue());
        selectedDonor.setAvailable(availabilityCheckBox.isSelected());

        boolean success = donorDAO.update(selectedDonor);
        if (success) {
            showInfoAlert("Success", "Donor Updated", "Donor details updated successfully.");
            loadDonors();
            handleClearForm();
        } else {
            showErrorAlert("Error", "Update Failed", "Could not update donor in database.");
        }
    }

    @FXML
    private void handleDeleteDonor() {
        if (selectedDonor == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Donor Record?");
        confirm.setContentText("This will delete donor: " + selectedDonor.getName() + " and their linked user login. Continue?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete user (cascade will delete donor)
                boolean success = userDAO.delete(selectedDonor.getUserId());
                if (success) {
                    showInfoAlert("Success", "Donor Deleted", "Donor and linked login removed successfully.");
                    loadDonors();
                    handleClearForm();
                } else {
                    showErrorAlert("Error", "Delete Failed", "Could not remove donor profile.");
                }
            }
        });
    }

    @FXML
    private void handleClearForm() {
        nameField.clear();
        ageField.clear();
        weightField.clear();
        bloodGroupCombo.setValue(null);
        cityField.clear();
        lastDonationDatePicker.setValue(null);
        availabilityCheckBox.setSelected(true);

        selectedDonor = null;
        donorTable.getSelectionModel().clearSelection();
        setEditingMode(false);
    }

    @FXML
    private void handleRefresh() {
        loadDonors();
    }

    private boolean validateInputs() {
        String name = nameField.getText().trim();
        String ageStr = ageField.getText().trim();
        String weightStr = weightField.getText().trim();
        String bloodGroup = bloodGroupCombo.getValue();
        String city = cityField.getText().trim();

        if (name.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() || bloodGroup == null || city.isEmpty()) {
            showWarningAlert("Validation Error", "Missing Fields", "Please populate all fields marked with an asterisk (*).");
            return false;
        }

        int age;
        double weight;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            showWarningAlert("Validation Error", "Invalid Age", "Age must be an integer (e.g. 25).");
            return false;
        }

        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            showWarningAlert("Validation Error", "Invalid Weight", "Weight must be a decimal value (e.g. 70.5).");
            return false;
        }

        if (!Validator.isValidAge(age)) {
            showWarningAlert("Validation Error", "Age Restriction", "Donor age must be between 18 and 65 years.");
            return false;
        }

        if (weight < 45.0) {
            showWarningAlert("Validation Error", "Weight Restriction", "Donors must weigh at least 45 kg.");
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
