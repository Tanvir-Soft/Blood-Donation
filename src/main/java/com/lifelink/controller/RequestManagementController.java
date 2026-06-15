package com.lifelink.controller;

import com.lifelink.dao.HospitalDAO;
import com.lifelink.dao.RequestDAO;
import com.lifelink.dao.UserDAO;
import com.lifelink.daoimpl.HospitalDAOImpl;
import com.lifelink.daoimpl.RequestDAOImpl;
import com.lifelink.daoimpl.UserDAOImpl;
import com.lifelink.model.BloodRequest;
import com.lifelink.model.Hospital;
import com.lifelink.model.User;
import com.lifelink.service.AvailabilityService;
import com.lifelink.util.Constants;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestManagementController {

    @FXML private TableView<BloodRequest> requestsTable;
    @FXML private TableColumn<BloodRequest, Integer> colId;
    @FXML private TableColumn<BloodRequest, String> colRequester;
    @FXML private TableColumn<BloodRequest, String> colBloodGroup;
    @FXML private TableColumn<BloodRequest, Integer> colUnits;
    @FXML private TableColumn<BloodRequest, String> colPriority;
    @FXML private TableColumn<BloodRequest, String> colDate;
    @FXML private TableColumn<BloodRequest, String> colStatus;

    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button fulfillButton;
    @FXML private Button deleteButton;
    @FXML private Button newRequestButton;
    @FXML private TextField searchField;

    private final RequestDAO requestDAO = new RequestDAOImpl();
    private final HospitalDAO hospitalDAO = new HospitalDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final AvailabilityService availabilityService = new AvailabilityService();

    private final ObservableList<BloodRequest> requestList = FXCollections.observableArrayList();
    private final Map<Integer, String> requesterNameCache = new HashMap<>();
    private BloodRequest selectedRequest = null;

    @FXML
    public void initialize() {
        // Table Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        colUnits.setCellValueFactory(new PropertyValueFactory<>("unitsRequested"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        
        colDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRequestDate().toString())
        );

        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toUpperCase())
        );

        colRequester.setCellValueFactory(cellData -> {
            BloodRequest req = cellData.getValue();
            String name = "System Seeker";
            if (req.getHospitalId() != null) {
                int hId = req.getHospitalId();
                if (!requesterNameCache.containsKey(hId)) {
                    Hospital h = hospitalDAO.readById(hId);
                    requesterNameCache.put(hId, h != null ? "🏥 " + h.getName() : "🏥 Hospital #" + hId);
                }
                name = requesterNameCache.get(hId);
            } else if (req.getSeekerId() != null) {
                int uId = req.getSeekerId();
                User u = userDAO.readById(uId);
                name = u != null ? "👤 Seeker: " + u.getUsername() : "👤 Seeker #" + uId;
            }
            return new SimpleStringProperty(name);
        });

        // Load data
        loadRequests();

        // Listeners
        requestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedRequest = newVal;
            updateButtonStates();
        });

        setupSearchFilter();
        updateButtonStates();
    }

    private void loadRequests() {
        requestList.clear();
        requesterNameCache.clear();
        List<BloodRequest> list = requestDAO.readAll();
        requestList.addAll(list);
    }

    private void setupSearchFilter() {
        FilteredList<BloodRequest> filteredData = new FilteredList<>(requestList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(req -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lower = newValue.toLowerCase();
                if (req.getBloodGroup().toLowerCase().contains(lower)) {
                    return true;
                }
                if (req.getStatus().toLowerCase().contains(lower)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<BloodRequest> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(requestsTable.comparatorProperty());
        requestsTable.setItems(sortedData);
    }

    private void updateButtonStates() {
        boolean disabled = (selectedRequest == null);
        approveButton.setDisable(disabled || !selectedRequest.getStatus().equalsIgnoreCase(Constants.STATUS_PENDING));
        rejectButton.setDisable(disabled || !selectedRequest.getStatus().equalsIgnoreCase(Constants.STATUS_PENDING));
        fulfillButton.setDisable(disabled || !selectedRequest.getStatus().equalsIgnoreCase(Constants.STATUS_APPROVED));
        deleteButton.setDisable(disabled);
    }

    @FXML
    private void handleNewRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lifelink/view/BloodRequest.fxml"));
            Parent view = loader.load();
            StackPane contentArea = (StackPane) newRequestButton.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could Not Load View", "Failed to open BloodRequest.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleApproveRequest() {
        if (selectedRequest == null) return;

        // Check stock levels
        int stock = availabilityService.getBloodStock(selectedRequest.getBloodGroup());
        if (stock < selectedRequest.getUnitsRequested()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Low Blood Stock Alert");
            confirm.setHeaderText("Insufficient Stock in System");
            confirm.setContentText("Requested: " + selectedRequest.getUnitsRequested() + " units of " 
                + selectedRequest.getBloodGroup() + "\nCurrently available: " + stock + " units.\n\nDo you want to approve this request anyway?");
            
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    executeStatusUpdate(Constants.STATUS_APPROVED);
                }
            });
        } else {
            executeStatusUpdate(Constants.STATUS_APPROVED);
        }
    }

    @FXML
    private void handleRejectRequest() {
        if (selectedRequest == null) return;
        executeStatusUpdate(Constants.STATUS_REJECTED);
    }

    @FXML
    private void handleFulfillRequest() {
        if (selectedRequest == null) return;
        executeStatusUpdate(Constants.STATUS_COMPLETED);
    }

    private void executeStatusUpdate(String status) {
        selectedRequest.setStatus(status);
        if (requestDAO.update(selectedRequest)) {
            showInfoAlert("Success", "Request Status Updated", "Blood request status updated to: " + status);
            loadRequests();
            requestsTable.getSelectionModel().clearSelection();
            selectedRequest = null;
            updateButtonStates();
        } else {
            showErrorAlert("Error", "Update Failed", "Failed to update status in the database.");
        }
    }

    @FXML
    private void handleDeleteRequest() {
        if (selectedRequest == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Request?");
        confirm.setContentText("Are you sure you want to permanently delete blood request ID: " + selectedRequest.getId() + "?");
        
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                if (requestDAO.delete(selectedRequest.getId())) {
                    showInfoAlert("Success", "Request Deleted", "The record has been successfully removed.");
                    loadRequests();
                    requestsTable.getSelectionModel().clearSelection();
                    selectedRequest = null;
                    updateButtonStates();
                } else {
                    showErrorAlert("Error", "Delete Failed", "Failed to remove the request record.");
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadRequests();
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
