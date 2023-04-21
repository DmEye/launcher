package com.example.launcher;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LauncherController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onButtonClick() {
        welcomeText.setText("Welcome to launcher of Synchronization System of Terraria Worlds based on JavaFX!");
    }
}