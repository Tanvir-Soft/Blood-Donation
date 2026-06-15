package com.lifelink.controller;

import com.lifelink.model.Donor;
import com.lifelink.service.EligibilityService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class EligibilityController {

    @FXML private TextField ageField;
    @FXML private TextField weightField;
    @FXML private DatePicker lastDonationDatePicker;

    @FXML private VBox statusCard;
    @FXML private Label statusHeader;
    @FXML private Label statusReason;

    private final EligibilityService eligibilityService = new EligibilityService();

    @FXML
    public void initialize() {
        // Initially hide status card
        statusCard.setVisible(false);
        statusCard.setManaged(false);
    }

    @FXML
    private void handleCheckEligibility() {
        if (!validateInputs()) return;

        int age = Integer.parseInt(ageField.getText().trim());
        double weight = Double.parseDouble(weightField.getText().trim());
        LocalDate lastDonation = lastDonationDatePicker.getValue();

        // Build a mock Donor object to run through business logic
        Donor mockDonor = new Donor();
        mockDonor.setAge(age);
        mockDonor.setWeight(weight);
        mockDonor.setLastDonationDate(lastDonation);
        mockDonor.setAvailable(true);

        boolean eligible = eligibilityService.isEligible(mockDonor);
        String reason = eligibilityService.getEligibilityReason(mockDonor);

        // Update card style class dynamically
        statusCard.getStyleClass().removeAll("status-eligible", "status-ineligible");
        statusHeader.getStyleClass().removeAll("status-eligible-text", "status-ineligible-text");

        if (eligible) {
            statusCard.getStyleClass().add("status-eligible");
            statusHeader.getStyleClass().add("status-eligible-text");
            statusHeader.setText("✅ ELIGIBLE FOR BLOOD DONATION");
            statusReason.setText("The donor meets all health requirements (Age 18-60, Weight >= 50kg, and at least 90 days have elapsed since their last donation).");
        } else {
            statusCard.getStyleClass().add("status-ineligible");
            statusHeader.getStyleClass().add("status-ineligible-text");
            statusHeader.setText("❌ NOT ELIGIBLE");
            statusReason.setText("Restriction Reason: " + reason);
        }

        statusCard.setVisible(true);
        statusCard.setManaged(true);
    }

    @FXML
    private void handleClear() {
        ageField.clear();
        weightField.clear();
        lastDonationDatePicker.setValue(null);

        statusCard.setVisible(false);
        statusCard.setManaged(false);
    }

    private boolean validateInputs() {
        String ageStr = ageField.getText().trim();
        String weightStr = weightField.getText().trim();

        if (ageStr.isEmpty() || weightStr.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Missing Fields");
            alert.setContentText("Age and weight fields are required to calculate eligibility.");
            alert.showAndWait();
            return false;
        }

        try {
            Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Age");
            alert.setContentText("Please enter age as a whole number.");
            alert.showAndWait();
            return false;
        }

        try {
            Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Weight");
            alert.setContentText("Please enter weight as a valid decimal number.");
            alert.showAndWait();
            return false;
        }

        return true;
    }
}
