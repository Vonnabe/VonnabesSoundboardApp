package com.example;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;






public class PrimaryController {

    @FXML
    private Stage stage;
    private Scene scene;
    private Parent root;

    private MediaPlayer mediaPlayer;
    private File selectedFile;

    @FXML
    private Button exitBtn;
    @FXML
    private Button playBtn;
    @FXML
    private Button stopBtn;
    @FXML
    private Button importBtn;
    @FXML
    private Slider volumeSlider;
    @FXML
    ListView<File> soundList;
    @FXML
    Label label;

    @FXML
public void initialize() {
    volumeSlider.setValue(0.75);
    volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(newValue.doubleValue());
        }
    });
}

@FXML
private void handlePlay() {
    // 1. Ask the soundList: "Which file is the user clicking on right now?"
    File selected = soundList.getSelectionModel().getSelectedItem();

    // 2. Check if they actually selected something
    if (selected == null) {
        System.out.println("No file selected in the list! Click a file first.");
        return; 
    }

    // 3. Stop previous sound if it's playing
    if (mediaPlayer != null) {
        mediaPlayer.stop();
    }

    try {
        // 4. Create and play the sound from the selection
        Media hit = new Media(selected.toURI().toString());
        mediaPlayer = new MediaPlayer(hit);
        
        // Sync with your volume slider
        mediaPlayer.setVolume(volumeSlider.getValue());
        
        mediaPlayer.play();
        System.out.println("Now playing: " + selected.getName());
        
    } catch (Exception e) {
        System.err.println("Could not play file: " + e.getMessage());
    }
}

    @FXML
private void handleImport() {
    FileChooser fileChooser = new FileChooser();
    
    // Set extension filters
    fileChooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav")
    );
    Stage stage = (Stage) playBtn.getScene().getWindow(); 
    
    File selectedFile = fileChooser.showOpenDialog(stage);

    if (selectedFile != null) {
        soundList.getItems().add(selectedFile);
    }
}

    @FXML
    private void handleStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            System.out.println("Stopped.");
        }
    }

    @FXML
    public void cancelButton(ActionEvent event) {
        Stage stage = (Stage) exitBtn.getScene().getWindow();
        stage.close();
    }

    
}
