package com.example.launcher;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.File;
import java.io.IOException;

public class LauncherController {
    @FXML
    private Label log;
    @FXML
    private TextField pathToWorld;
    @FXML
    private TextField pathToGame;
    @FXML
    private TextField pathToCloud;

    @FXML
    protected void onButtonBecomeHost() {

    }
    @FXML
    protected void onButtonLaunchGame() {
        if (checkTextFields()) {
            String path_to_game_folder = pathToGame.getText();
            File game_directory = new File(path_to_game_folder);
            if (game_directory.exists() && game_directory.isDirectory()) {
                ProcessBuilder pb = null;
                switch (System.getProperty("os.name")) {
                    case "Linux": {
                        pb = new ProcessBuilder(String.format("%s/Terraria", path_to_game_folder));
                        break;
                    }
                    case "Windows": {
                        pb = new ProcessBuilder(String.format("%s/Terraria.exe", path_to_game_folder));
                        break;
                    }
                    default: {
                        log.setText("Не знаю такой опреационной ситсемы!");
                    }
                }
                if (pb != null) {
                    try {
                        Process proc = pb.start();
                    } catch (IOException e) {
                        log.setText(e.getMessage());
                    }
                } else {
                    log.setText("Не удалось построить процесс!");
                }
            } else {
                log.setText("Папка не существует!");
            }
        }
    }

    private boolean checkTextFields() {
        return !pathToWorld.getText().isEmpty() && !pathToGame.getText().isEmpty() && !pathToCloud.getText().isEmpty();
    }
}