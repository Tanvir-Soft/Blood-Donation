package com.lifelink;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load Login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lifelink/view/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 580);

            // Add CSS styling if available
            if (getClass().getResource("/com/lifelink/css/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/com/lifelink/css/style.css").toExternalForm());
            }

            primaryStage.setTitle("LifeLink - Sign In");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load login view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
