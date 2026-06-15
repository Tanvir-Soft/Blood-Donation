package com.lifelink.controller;

import com.lifelink.dao.DonationHistoryDAO;
import com.lifelink.dao.DonorDAO;
import com.lifelink.daoimpl.DonationHistoryDAOImpl;
import com.lifelink.daoimpl.DonorDAOImpl;
import com.lifelink.model.DonationHistory;
import com.lifelink.model.Donor;
import com.lifelink.service.EligibilityService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonationHistoryController {

    @FXML private ComboBox<Donor> donorCombo;
    @FXML private TextField locationField;
    @FXML private TextField unitsField;
    @FXML private DatePicker donationDatePicker;

    @FXML private Button logButton;
    @FXML private Button clearButton;

    @FXML private TextField searchField;
    @FXML private TableView<DonationHistory> historyTable;
    @FXML private TableColumn<DonationHistory, Integer> colId;
    @FXML private TableColumn<DonationHistory, String> colDonor;
    @FXML private TableColumn<DonationHistory, String> colBloodGroup;
    @FXML private TableColumn<DonationHistory, String> colDate;
    @FXML private TableColumn<DonationHistory, Integer> colUnits;
    @FXML private TableColumn<DonationHistory, String> colLocation;

    private final DonationHistoryDAO donationHistoryDAO = new DonationHistoryDAOImpl();
    private final DonorDAO donorDAO = new DonorDAOImpl();
    private final EligibilityService eligibilityService = new EligibilityService();

    private final ObservableList<DonationHistory> historyList = FXCollections.observableArrayList();
    private final Map<Integer, Donor> donorCache = new HashMap<>();

    @FXML
    public void initialize() {
        // Table Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUnits.setCellValueFactory(new PropertyValueFactory<>("unitsDonated"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        
        colDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDonationDate().toString())
        );

        colDonor.setCellValueFactory(cellData -> {
            Donor d = getDonorFromCache(cellData.getValue().getDonorId());
            return new SimpleStringProperty(d != null ? d.getName() : "Unknown");
        });

        colBloodGroup.setCellValueFactory(cellData -> {
            Donor d = getDonorFromCache(cellData.getValue().getDonorId());
            return new SimpleStringProperty(d != null ? d.getBloodGroup() : "N/A");
        });

        // Load data
        loadDonorsCombo();
        loadHistory();

        // Search Filter
        setupSearchFilter();

        // Default Date
        donationDatePicker.setValue(LocalDate.now());
    }

    private Donor getDonorFromCache(int id) {
        if (!donorCache.containsKey(id)) {
            Donor d = donorDAO.readById(id);
            if (d != null) {
                donorCache.put(id, d);
            }
        }
        return donorCache.get(id);
    }

    private void loadDonorsCombo() {
        List<Donor> donors = donorDAO.readAll();
        donorCombo.setItems(FXCollections.observableArrayList(donors));
        donorCombo.setConverter(new StringConverter<Donor>() {
            @Override
            public String toString(Donor d) {
                return d == null ? "" : d.getName() + " (" + d.getBloodGroup() + ", " + d.getCity() + ")";
            }

            @Override
            public Donor fromString(String string) {
                return null;
            }
        });
    }

    private void loadHistory() {
        historyList.clear();
        donorCache.clear();
        List<DonationHistory> list = donationHistoryDAO.readAll();
        historyList.addAll(list);
    }

    private void setupSearchFilter() {
        FilteredList<DonationHistory> filteredData = new FilteredList<>(historyList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(log -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lower = newValue.toLowerCase();
                if (log.getLocation().toLowerCase().contains(lower)) {
                    return true;
                }
                Donor d = getDonorFromCache(log.getDonorId());
                if (d != null && d.getName().toLowerCase().contains(lower)) {
                    return true;
                }
                if (d != null && d.getBloodGroup().toLowerCase().contains(lower)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<DonationHistory> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(historyTable.comparatorProperty());
        historyTable.setItems(sortedData);
    }

    @FXML
    private void handleLogDonation() {
        if (!validateInputs()) return;

        Donor donor = donorCombo.getValue();
        String location = locationField.getText().trim();
        int units = Integer.parseInt(unitsField.getText().trim());
        LocalDate donationDate = donationDatePicker.getValue();

        if (donationDate.isAfter(LocalDate.now())) {
            showWarningAlert("Validation Error", "Invalid Date", "Donation date cannot be in the future.");
            return;
        }

        // Check Eligibility Check
        boolean eligible = eligibilityService.isEligible(donor);
        if (!eligible) {
            String reason = eligibilityService.getEligibilityReason(donor);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Donor Ineligibility Warning");
            alert.setHeaderText("Donor fails eligibility checks");
            alert.setContentText("Reason: " + reason + "\n\nDo you want to FORCE log this donation anyway?");
            
            ButtonType buttonType = alert.showAndWait().orElse(ButtonType.CANCEL);
            if (buttonType != ButtonType.OK) {
                return; // Cancel logging
            }
        }

        // Log Donation
        DonationHistory log = new DonationHistory(donor.getId(), donationDate, units, location);
        boolean success = donationHistoryDAO.create(log);
        if (success) {
            // Update donor state
            donor.setLastDonationDate(donationDate);
            eligibilityService.checkAndUpdateEligibility(donor); // updates availability boolean
            donorDAO.update(donor);

            showInfoAlert("Success", "Donation Logged", "Donation history entry created. Stock updated.");
            loadHistory();
            loadDonorsCombo(); // refresh combo states
            handleClearForm();
        } else {
            showErrorAlert("Database Error", "Failed to Log", "Could not write record to database.");
        }
    }

    @FXML
    private void handleClearForm() {
        donorCombo.setValue(null);
        locationField.clear();
        unitsField.clear();
        donationDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleRefresh() {
        loadHistory();
        loadDonorsCombo();
    }

    private boolean validateInputs() {
        Donor donor = donorCombo.getValue();
        String location = locationField.getText().trim();
        String unitsStr = unitsField.getText().trim();
        LocalDate date = donationDatePicker.getValue();

        if (donor == null) {
            showWarningAlert("Validation Error", "Donor Required", "Please select a donor.");
            return false;
        }
        if (location.isEmpty()) {
            showWarningAlert("Validation Error", "Location Required", "Please enter the donation location.");
            return false;
        }
        if (unitsStr.isEmpty()) {
            showWarningAlert("Validation Error", "Units Required", "Please enter the number of units donated.");
            return false;
        }
        if (date == null) {
            showWarningAlert("Validation Error", "Date Required", "Please select the donation date.");
            return false;
        }

        try {
            int units = Integer.parseInt(unitsStr);
            if (units <= 0) {
                showWarningAlert("Validation Error", "Invalid Units", "Units donated must be a positive number.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Validation Error", "Invalid Units", "Units donated must be an integer.");
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
