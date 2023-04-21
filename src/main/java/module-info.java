module com.example.launcher {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;

    opens com.example.launcher to javafx.fxml;
    exports com.example.launcher;
}