package com.example.launcher;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.*;
import com.google.gson.Gson;

public class LauncherController {
    @FXML
    private Label log;
    @FXML
    private TextField pathToWorld;
    @FXML
    private TextField token;
    @FXML
    private TextField pathToGame;
    private Process game = null;
    private final String link_to_download = "https://cloud-api.yandex.net/v1/disk/resources/download";
    private final String link_to_upload = "https://cloud-api.yandex.net/v1/disk/resources/upload";
    private final Gson parser = new Gson();
    @FXML
    protected void onButtonBecomeHost() {
        if (checkTextFields()) {
            if (game == null) {
                // Скачать hosts.json
                URL downloadLink = this.getLink(this.link_to_download, "hosts.json");
                if (downloadFile(" ./hosts.json", 1024, downloadLink)) {
                    log.setText("SUCCESS!");
                } else {
                    log.setText("Error: can not download the \"hosts.json\" file.");
                }

                // Загрузить файл
            }
        }
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
                        this.game = pb.start();
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
        return !pathToWorld.getText().isEmpty() && !pathToGame.getText().isEmpty() && !token.getText().isEmpty();
    }

    private URL getLink(String base_link, String file) {
        URL url2 = null;
        try {
            URL url = new URL(base_link + "?path=%2FПриложения%2FLauncher%2F" + file);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", " OAuth " + token.getText());

            con.connect();

            int status = con.getResponseCode();
            Reader streamReader;
            if (status == HttpURLConnection.HTTP_OK) {
                streamReader = new InputStreamReader(con.getInputStream());
                BufferedReader in = new BufferedReader(streamReader);
                String input_line;
                while ((input_line = in.readLine()) != null) {
                    break;
                }
                in.close();
                Link l = parser.fromJson(input_line, Link.class);
                url2 = new URL(l.href);
            } else {
                log.setText(String.format("Error: %d", status));
            }
            con.disconnect();
        } catch (Exception e) {
            log.setText(String.format("Error: %s", e.getMessage()));
        }
        return url2;
    }
    private boolean downloadFile(String file_name, int buffer_size, URL link) {
        try {
            BufferedInputStream in = new BufferedInputStream(link.openStream());
            FileOutputStream file_hosts = new FileOutputStream(file_name);
            byte[] buffer = new byte[buffer_size];
            int bytes_read;
            while ((bytes_read = in.read(buffer, 0, buffer_size)) != -1) {
                file_hosts.write(buffer, 0, bytes_read);
            }
            in.close();
            file_hosts.close();
        } catch (IOException e) {
            log.setText(e.getMessage());
            return false;
        }
        return true;
    }
}