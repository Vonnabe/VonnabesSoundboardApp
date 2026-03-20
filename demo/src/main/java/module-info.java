module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires com.github.kwhat.jnativehook;
    
    opens com.example to javafx.fxml;

    exports com.example;
}
