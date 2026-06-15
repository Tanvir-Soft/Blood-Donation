package com.lifelink.controller;

import com.lifelink.dao.DonorDAO;
import com.lifelink.dao.UserDAO;
import com.lifelink.daoimpl.DonorDAOImpl;
import com.lifelink.daoimpl.UserDAOImpl;
import com.lifelink.model.Donor;
import com.lifelink.model.User;
import com.lifelink.util.Constants;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodSearchController {

    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> availabilityCombo;

    @FXML private TableView<Donor> resultsTable;
    @FXML private TableColumn<Donor, String> colName;
    @FXML private TableColumn<Donor, String> colBloodGroup;
    @FXML private TableColumn<Donor, String> colCity;
    @FXML private TableColumn<Donor, String> colContact;
    @FXML private TableColumn<Donor, String> colAvailability;

    private final DonorDAO donorDAO = new DonorDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ObservableList<Donor> originalList = FXCollections.observableArrayList();
    private final ObservableList<Donor> filteredList = FXCollections.observableArrayList();

    // Cache user emails to avoid redundant SQL queries during table loading
    private final Map<Integer, String> userEmailCache = new HashMap<>();

    @FXML
    public void initialize() {
        // Setup dropdowns
        bloodGroupCombo.setItems(FXCollections.observableArrayList(Constants.BLOOD_GROUPS));
        availabilityCombo.setItems(FXCollections.observableArrayList("Eligible / Available Only", "All Donors"));
        availabilityCombo.setValue("Eligible / Available Only");

        // Column bindings
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));

        colContact.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUserId();
            if (!userEmailCache.containsKey(userId)) {
                User u = userDAO.readById(userId);
                if (u != null) {
                    userEmailCache.put(userId, u.getEmail());
                } else {
                    userEmailCache.put(userId, "N/A");
                }
            }
            return new SimpleStringProperty(userEmailCache.get(userId));
        });

        colAvailability.setCellValueFactory(cellData -> {
            boolean isAvailable = cellData.getValue().isAvailable();
            return new SimpleStringProperty(isAvailable ? "ELIGIBLE" : "INELIGIBLE");
        });

        // Load data from DB
        loadAllDonors();
        
        // Initial search to apply default "Available Only" filter
        handleSearch();
    }

    private void loadAllDonors() {
        originalList.clear();
        userEmailCache.clear();
        List<Donor> allDonors = donorDAO.readAll();
        originalList.addAll(allDonors);
    }

    @FXML
    private void handleSearch() {
        String bloodGroupFilter = bloodGroupCombo.getValue();
        String cityFilter = cityField.getText().trim().toLowerCase();
        String availabilityFilter = availabilityCombo.getValue();

        filteredList.clear();

        for (Donor donor : originalList) {
            // 1. Blood Group Filter
            if (bloodGroupFilter != null && !donor.getBloodGroup().equalsIgnoreCase(bloodGroupFilter)) {
                continue;
            }

            // 2. City Filter
            if (!cityFilter.isEmpty() && !donor.getCity().toLowerCase().contains(cityFilter)) {
                continue;
            }

            // 3. Availability Filter
            if ("Eligible / Available Only".equals(availabilityFilter) && !donor.isAvailable()) {
                continue;
            }

            filteredList.add(donor);
        }

        resultsTable.setItems(filteredList);
    }

    @FXML
    private void handleClearFilters() {
        bloodGroupCombo.setValue(null);
        cityField.clear();
        availabilityCombo.setValue("Eligible / Available Only");
        
        handleSearch();
    }
}
