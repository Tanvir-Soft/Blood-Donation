package com.lifelink.controller;

import com.lifelink.model.User;
import com.lifelink.service.DashboardService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Controller for DashboardView.fxml.
 * Manages sidebar navigation, header info, and content area swapping.
 */
public class DashboardController implements UserAwareController {

    // ---- Header elements ----
    @FXML private Label pageTitle;
    @FXML private Label pageBreadcrumb;
    @FXML private Label userInitialLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleHeaderLabel;
    @FXML private Label userRoleLabel;

    // ---- Content Area ----
    @FXML private StackPane contentArea;

    // ---- Sidebar Navigation Buttons ----
    @FXML private Button navDashboard;
    @FXML private Button navDonors;
    @FXML private Button navSearch;
    @FXML private Button navRequests;
    @FXML private Button navHospitals;
    @FXML private Button navHistory;
    @FXML private Button navEligibility;
    @FXML private Button navStock;
    @FXML private Button navLogout;

    private User currentUser;
    private Button activeNavButton;
    private final DashboardService dashboardService = new DashboardService();

    // -------------------------------------------------------
    // Initialization
    // -------------------------------------------------------
    @FXML
    public void initialize() {
        activeNavButton = navDashboard;
        loadDashboardHome();
    }

    @Override
    public void initializeUser(User user) {
        this.currentUser = user;
        updateHeaderInfo();
        loadDashboardHome();
    }

    private void updateHeaderInfo() {
        if (currentUser == null) return;

        String name = currentUser.getUsername();
        String role = currentUser.getRole();

        userNameLabel.setText(name);
        userRoleHeaderLabel.setText(role);
        userInitialLabel.setText(name.substring(0, 1).toUpperCase());

        switch (role.toUpperCase()) {
            case "ADMIN":
                userRoleLabel.setText("Admin Dashboard");
                break;
            case "DONOR":
                userRoleLabel.setText("Donor Dashboard");
                break;
            case "HOSPITAL":
                userRoleLabel.setText("Hospital Dashboard");
                break;
            default:
                userRoleLabel.setText("System Dashboard");
                break;
        }
    }

    // -------------------------------------------------------
    // Sidebar Navigation Handlers
    // -------------------------------------------------------

    @FXML
    private void handleNavDashboard() {
        setActiveNav(navDashboard);
        setPageHeader("Dashboard", "LifeLink › Dashboard");
        loadDashboardHome();
    }

    @FXML
    private void handleNavDonors() {
        setActiveNav(navDonors);
        setPageHeader("Donor Management", "LifeLink › Donors");
        loadContentFxml("/com/lifelink/view/DonorManagement.fxml");
    }

    @FXML
    private void handleNavSearch() {
        setActiveNav(navSearch);
        setPageHeader("Search Blood", "LifeLink › Search Blood");
        loadContentFxml("/com/lifelink/view/BloodSearch.fxml");
    }

    @FXML
    private void handleNavRequests() {
        setActiveNav(navRequests);
        setPageHeader("Request Management", "LifeLink › Requests");
        loadContentFxml("/com/lifelink/view/RequestManagement.fxml");
    }

    @FXML
    private void handleNavHospitals() {
        setActiveNav(navHospitals);
        setPageHeader("Hospital Management", "LifeLink › Hospitals");
        loadContentFxml("/com/lifelink/view/HospitalManagement.fxml");
    }

    @FXML
    private void handleNavHistory() {
        setActiveNav(navHistory);
        setPageHeader("Donation History", "LifeLink › History");
        loadContentFxml("/com/lifelink/view/DonationHistory.fxml");
    }

    @FXML
    private void handleNavEligibility() {
        setActiveNav(navEligibility);
        setPageHeader("Eligibility Checker", "LifeLink › Eligibility");
        loadContentFxml("/com/lifelink/view/EligibilityChecker.fxml");
    }

    @FXML
    private void handleNavStock() {
        setActiveNav(navStock);
        setPageHeader("Blood Stock Monitor", "LifeLink › Blood Stock");
        loadContentFxml("/com/lifelink/view/BloodStock.fxml");
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/lifelink/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) navLogout.getScene().getWindow();
            Scene scene = new Scene(root, 900, 580);
            if (getClass().getResource("/com/lifelink/css/style.css") != null) {
                scene.getStylesheets().add(
                        getClass().getResource("/com/lifelink/css/style.css").toExternalForm());
            }
            stage.setTitle("LifeLink - Sign In");
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Error navigating to login: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Navigation Helpers
    // -------------------------------------------------------

    private void setActiveNav(Button button) {
        // Reset old active button
        if (activeNavButton != null) {
            activeNavButton.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #B0B7C3; "
                    + "-fx-font-size: 12; -fx-alignment: CENTER_LEFT; "
                    + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 14;");
        }
        // Set new active button
        button.setStyle(
                "-fx-background-color: #E53935; -fx-text-fill: white; "
                + "-fx-font-size: 12; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 14;");
        activeNavButton = button;
    }

    private void setPageHeader(String title, String breadcrumb) {
        pageTitle.setText(title);
        pageBreadcrumb.setText(breadcrumb);
    }

    // -------------------------------------------------------
    // Content Loading
    // -------------------------------------------------------

    private void loadContentFxml(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (Exception e) {
            System.err.println("Could not load content: " + fxmlPath + " — " + e.getMessage());
            showPlaceholder(fxmlPath);
        }
    }

    private void showPlaceholder(String fxmlPath) {
        VBox placeholder = new VBox(12);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        placeholder.setPadding(new Insets(40));

        Label icon = new Label("🚧");
        icon.setFont(Font.font(48));

        Label title = new Label("Coming Soon");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(javafx.scene.paint.Color.web("#424242"));

        Label msg = new Label("This module is under construction.\nIt will be available in an upcoming phase.");
        msg.setTextFill(javafx.scene.paint.Color.web("#757575"));
        msg.setFont(Font.font(13));
        msg.setWrapText(true);
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        placeholder.getChildren().addAll(icon, title, msg);
        contentArea.getChildren().setAll(placeholder);
    }

    // -------------------------------------------------------
    // Admin Dashboard Home Content (Phase 5)
    // -------------------------------------------------------

    private void loadDashboardHome() {
        loadContentFxml("/com/lifelink/view/AdminDashboard.fxml");
    }

    // -------------------------------------------------------
    // UI Component Factories
    // -------------------------------------------------------

    private VBox createStatCard(String title, String value, String emoji, String accentColor, String bgColor) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        // Top row: emoji icon in colored circle + title
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(40, 40);
        iconCircle.setMinSize(40, 40);
        iconCircle.setMaxSize(40, 40);
        iconCircle.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 20;");
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(18));
        iconCircle.getChildren().add(emojiLabel);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(javafx.scene.paint.Color.web("#757575"));

        HBox topRow = new HBox(10, iconCircle, titleLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Value
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setTextFill(javafx.scene.paint.Color.web(accentColor));

        card.getChildren().addAll(topRow, valueLabel);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private Button createQuickAction(String text, String emoji, String color,
                                     javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(emoji + "  " + text);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: " + color + "; "
                + "-fx-font-weight: bold; -fx-font-size: 12; "
                + "-fx-border-color: " + color + "; -fx-border-radius: 8; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 12 20;");
        btn.setOnAction(handler);
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }
}
