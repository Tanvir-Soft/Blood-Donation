package com.lifelink.controller;

import com.lifelink.dao.HospitalDAO;
import com.lifelink.dao.RequestDAO;
import com.lifelink.daoimpl.HospitalDAOImpl;
import com.lifelink.daoimpl.RequestDAOImpl;
import com.lifelink.model.BloodRequest;
import com.lifelink.model.Hospital;
import com.lifelink.util.Constants;
import com.lifelink.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class BloodRequestController {

    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private ComboBox<Hospital> hospitalCombo;
    @FXML private TextField unitsField;
    @FXML private ComboBox<String> urgencyCombo;
    @FXML private TextField contactField;

    @FXML private Button submitButton;
    @FXML private Button cancelButton;

    private final HospitalDAO hospitalDAO = new HospitalDAOImpl();
    private final RequestDAO requestDAO = new RequestDAOImpl();

    @FXML
    public void initialize() {
        // Setup Blood Group Combo
        bloodGroupCombo.setItems(FXCollections.observableArrayList(Constants.BLOOD_GROUPS));

        // Setup Urgency Combo
        urgencyCombo.setItems(FXCollections.observableArrayList("Critical", "High", "Normal"));
        urgencyCombo.setValue("Normal");

        // Load Hospitals and Setup String Converter
        List<Hospital> hospitals = hospitalDAO.readAll();
        hospitalCombo.setItems(FXCollections.observableArrayList(hospitals));
        hospitalCombo.setConverter(new StringConverter<Hospital>() {
            @Override
            public String toString(Hospital h) {
                return h == null ? "" : h.getName() + " (" + h.getAddress() + ")";
            }

            @Override
            public Hospital fromString(String string) {
                return null; // Not needed for read-only ComboBox
            }
        });
    }

    @FXML
    private void handleSubmit() {
        if (!validateInputs()) return;

        String bloodGroup = bloodGroupCombo.getValue();
        Hospital selectedHospital = hospitalCombo.getValue();
        int units = Integer.parseInt(unitsField.getText().trim());
        String urgency = urgencyCombo.getValue();
        String contact = contactField.getText().trim();

        // Create Blood Request entity
        BloodRequest req = new BloodRequest();
        req.setSeekerId(null); // Created by Admin/Hospital
        req.setHospitalId(selectedHospital.getId());
        req.setBloodGroup(bloodGroup);
        req.setUnitsRequested(units);
        req.setPriority(urgency);
        req.setRequestDate(LocalDate.now());
        req.setStatus(Constants.STATUS_PENDING);

        boolean success = requestDAO.create(req);
        if (success) {
            showInfoAlert("Success", "Request Logged", "Emergency blood request for " + units + " units of " + bloodGroup + " has been broadcasted.");
            goBack();
        } else {
            showErrorAlert("Database Error", "Submission Failed", "Failed to save blood request record to database. Please check connection.");
        }
    }

    @FXML
    private void handleCancel() {
        goBack();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lifelink/view/RequestManagement.fxml"));
            Parent view = loader.load();
            StackPane contentArea = (StackPane) cancelButton.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could Not Go Back", "Failed to load RequestManagement.fxml: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        String bg = bloodGroupCombo.getValue();
        Hospital h = hospitalCombo.getValue();
        String unitsStr = unitsField.getText().trim();
        String contact = contactField.getText().trim();

        if (bg == null) {
            showWarningAlert("Validation Error", "Blood Group Required", "Please select a required blood group.");
            return false;
        }
        if (h == null) {
            showWarningAlert("Validation Error", "Hospital Required", "Please select the receiving hospital.");
            return false;
        }
        if (unitsStr.isEmpty()) {
            showWarningAlert("Validation Error", "Units Required", "Please enter the number of blood units needed.");
            return false;
        }
        
        int units;
        try {
            units = Integer.parseInt(unitsStr);
        } catch (NumberFormatException e) {
            showWarningAlert("Validation Error", "Invalid Units", "Units requested must be a whole number.");
            return false;
        }

        if (units <= 0) {
            showWarningAlert("Validation Error", "Invalid Units", "Units requested must be at least 1 unit.");
            return false;
        }

        if (contact.isEmpty()) {
            showWarningAlert("Validation Error", "Contact Number Required", "Please input a contact number.");
            return false;
        }

        if (!Validator.isValidPhone(contact)) {
            showWarningAlert("Validation Error", "Invalid Contact Number", "Please enter a valid phone number (7 to 15 digits).");
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
