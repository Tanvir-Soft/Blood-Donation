package com.lifelink.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardCardController {

    @FXML private StackPane iconBg;
    @FXML private Label iconLabel;
    @FXML private Label titleLabel;
    @FXML private Label valueLabel;

    public void setData(String title, String value, String icon, String colorHex, String bgHex) {
        titleLabel.setText(title);
        valueLabel.setText(value);
        iconLabel.setText(icon);
        valueLabel.setStyle("-fx-text-fill: " + colorHex + ";");
        iconBg.setStyle("-fx-background-color: " + bgHex + "; -fx-background-radius: 20px;");
    }
}
