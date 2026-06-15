package com.lifelink.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class PlaceholderController {

    @FXML
    private Label statusLabel;

    @FXML
    private Button clickButton;

    @FXML
    public void initialize() {
        System.out.println("PlaceholderController initialized.");
    }

    @FXML
    private void handleButtonClick() {
        statusLabel.setText("JavaFX is configured and running perfectly!");
        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }
}
