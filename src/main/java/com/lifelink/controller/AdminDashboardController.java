package com.lifelink.controller;

import com.lifelink.service.DashboardService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.Map;

public class AdminDashboardController {

    @FXML private Label welcomeText;

    // Injected nested controllers
    @FXML private DashboardCardController usersCardController;
    @FXML private DashboardCardController donorsCardController;
    @FXML private DashboardCardController eligibleCardController;
    @FXML private DashboardCardController requestsCardController;
    @FXML private DashboardCardController hospitalsCardController;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        loadMetrics();
    }

    public void loadMetrics() {
        Map<String, Object> metrics = dashboardService.getAdminMetrics();

        // Populate cards: Title, Value, Icon, Accent Color, Background Color
        if (usersCardController != null) {
            usersCardController.setData(
                "Total Users", 
                String.valueOf(metrics.getOrDefault("totalUsers", 0)), 
                "👤", 
                "#424242", 
                "#E0E0E0"
            );
        }
        if (donorsCardController != null) {
            donorsCardController.setData(
                "Total Donors", 
                String.valueOf(metrics.getOrDefault("totalDonors", 0)), 
                "🩸", 
                "#E53935", 
                "#FFEBEE"
            );
        }
        if (eligibleCardController != null) {
            eligibleCardController.setData(
                "Available Donors", 
                String.valueOf(metrics.getOrDefault("availableDonors", 0)), 
                "✅", 
                "#43A047", 
                "#E8F5E9"
            );
        }
        if (requestsCardController != null) {
            requestsCardController.setData(
                "Total Requests", 
                String.valueOf(metrics.getOrDefault("totalRequests", 0)), 
                "📋", 
                "#FB8C00", 
                "#FFF3E0"
            );
        }
        if (hospitalsCardController != null) {
            hospitalsCardController.setData(
                "Registered Hospitals", 
                String.valueOf(metrics.getOrDefault("totalHospitals", 0)), 
                "🏥", 
                "#1E88E5", 
                "#E3F2FD"
            );
        }
    }
}
