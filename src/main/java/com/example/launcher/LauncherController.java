package com.example.launcher;

import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.gson.Gson;

public class LauncherController {
    @FXML
    private TextField pathToWorld;
    @FXML
    private TextField token;
    @FXML
    private TextField pathToGame;
    @FXML
    public TextField nickName;
    private Process game = null;
    private final String link_to_download = "https://cloud-api.yandex.net/v1/disk/resources/download";
    private final String link_to_upload = "https://cloud-api.yandex.net/v1/disk/resources/upload";
    private final Gson parser = new Gson();
    private Type listOfHostsType = new TypeToken<HashMap<Integer, HostInfo>>(){}.getType();
    private Integer current_host_number = -1;
    @FXML
    protected void onButtonBecomeHost() {
        if (checkTextFields()) {
            if (game == null) {
                // Скачать hosts.json
                URL downloadLink = this.getLink(this.link_to_download, "hosts.json");
                if (downloadFile("hosts.json", 1024, downloadLink)) {
                    try {
                        Reader hosts_reader = new FileReader("hosts.json");
                        HashMap<Integer, HostInfo> data = parser.fromJson(hosts_reader, this.listOfHostsType);
                        for (Map.Entry<Integer, HostInfo> it : data.entrySet()) {
                            if (this.nickName.getText().equals(it.getValue().nick_name)) {
                                this.current_host_number = it.getKey();
                                break;
                            }
                        }
                        hosts_reader.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage() + "1");
                    }
                    if (this.current_host_number == -1) {
                        System.out.println("Указанный никнейм не существует.");
                    } else {
                        System.out.println("Ваш id - " + this.current_host_number.toString());
                    }

                    // Загрузить <номер>.json
                    String file_name = this.current_host_number.toString() + ".json";
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file_name));
                        Date date_now = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                        TimestampInfo tmst = new TimestampInfo(sdf.format(date_now));
                        writer.write(this.parser.toJson(tmst));
                        writer.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage() + "QWE");
                    }
                    URL uploadLink = this.getLink(this.link_to_upload, file_name);
                    if (this.uploadFile(file_name, 1024, uploadLink)) {
                        File current_host_number_file = new File(file_name);
                        if (current_host_number_file.delete()) {
                            System.out.println("SUCCESS!");
                            // Try to download other's files
                            HashMap<Integer, LocalDate> downloads = new HashMap<Integer, LocalDate>();
                            try {
                                Reader hosts_reader = new FileReader("hosts.json");
                                HashMap<Integer, HostInfo> data = parser.fromJson(hosts_reader, this.listOfHostsType);
                                for (Map.Entry<Integer, HostInfo> it : data.entrySet()) {
                                    String other_file_name = it.getKey().toString() + ".json";
                                    downloadLink = this.getLink(this.link_to_download, other_file_name);
                                    System.out.println(other_file_name);
                                    if (downloadLink != null) {
                                        if (this.downloadFile(other_file_name, 1024, downloadLink)) {
                                            FileReader reader = new FileReader(other_file_name);
                                            TimestampInfo timestampInfo = parser.fromJson(reader, TimestampInfo.class);
                                            reader.close();
                                            DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                                            downloads.put(it.getKey(), LocalDate.parse(timestampInfo.timestamp, sdf));
                                        }
                                    }
                                }
                                hosts_reader.close();
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }

                            for (Map.Entry<Integer, LocalDate> it : downloads.entrySet()) {
                                System.out.println(it.getKey().toString() + it.getValue().toString());
                            }
                        }
                    } else {
                        System.out.println("Failed to upload!");
                    }
                }
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
                        System.out.println("Не знаю такой опреационной ситсемы!");
                    }
                }
                if (pb != null) {
                    try {
                        this.game = pb.start();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    System.out.println("Не удалось построить процесс!");
                }
            } else {
                System.out.println("Папка не существует!");
            }
        }
    }

    private boolean checkTextFields() {
        return !pathToWorld.getText().isEmpty() && !pathToGame.getText().isEmpty() && !token.getText().isEmpty() && !nickName.getText().isEmpty();
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
                System.out.println(String.format("Error: %d", status));
            }
            con.disconnect();
        } catch (Exception e) {
            System.out.println(String.format("Error: %s", e.getMessage()));
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
            System.out.println(e.getMessage() + "ASD");
            return false;
        }
        return true;
    }
    private boolean uploadFile(String file_path, int buffer_size, URL link) {
        try {
            HttpURLConnection con = (HttpURLConnection) link.openConnection();
            con.setRequestMethod("PUT");
            con.setDoOutput(true);

            BufferedOutputStream out = new BufferedOutputStream(con.getOutputStream());
            BufferedInputStream number_file = new BufferedInputStream(new BufferedInputStream(new FileInputStream(file_path)));
            byte[] buffer = new byte[buffer_size];
            int bytes_read;
            while ((bytes_read = number_file.read(buffer, 0, buffer_size)) != -1) {
                out.write(buffer, 0, bytes_read);
            }
            number_file.close();
            out.close();

            int status = con.getResponseCode();
            con.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage() + "FHF");
            return false;
        }
        return true;
    }
}