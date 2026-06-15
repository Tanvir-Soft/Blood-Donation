package com.lifelink.controller;

import com.lifelink.service.AvailabilityService;
import com.lifelink.util.Constants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Map;

public class BloodStockController {

    @FXML private TableView<BloodStockItem> stockTable;
    @FXML private TableColumn<BloodStockItem, String> colBloodGroup;
    @FXML private TableColumn<BloodStockItem, Integer> colUnits;
    @FXML private TableColumn<BloodStockItem, String> colAlertLevel;

    private final AvailabilityService availabilityService = new AvailabilityService();
    private final ObservableList<BloodStockItem> stockDataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Table Columns
        colBloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        colUnits.setCellValueFactory(new PropertyValueFactory<>("units"));
        colAlertLevel.setCellValueFactory(new PropertyValueFactory<>("alertLevel"));

        // Load data
        loadStockData();
    }

    private void loadStockData() {
        stockDataList.clear();
        Map<String, Integer> stockMap = availabilityService.getAllBloodStock();

        for (String bg : Constants.BLOOD_GROUPS) {
            int units = stockMap.getOrDefault(bg, 0);
            String alertLevel;

            if (units < 2) {
                alertLevel = "🔴 CRITICAL STOCK";
            } else if (units < 5) {
                alertLevel = "⚠️ LOW STOCK WARNING";
            } else {
                alertLevel = "🟢 SUFFICIENT STOCK";
            }

            stockDataList.add(new BloodStockItem(bg, units, alertLevel));
        }

        stockTable.setItems(stockDataList);
    }

    @FXML
    private void handleRefresh() {
        loadStockData();
    }

    // Inner class helper to represent row item data
    public static class BloodStockItem {
        private final String bloodGroup;
        private final int units;
        private final String alertLevel;

        public BloodStockItem(String bloodGroup, int units, String alertLevel) {
            this.bloodGroup = bloodGroup;
            this.units = units;
            this.alertLevel = alertLevel;
        }

        public String getBloodGroup() {
            return bloodGroup;
        }

        public int getUnits() {
            return units;
        }

        public String getAlertLevel() {
            return alertLevel;
        }
    }
}
