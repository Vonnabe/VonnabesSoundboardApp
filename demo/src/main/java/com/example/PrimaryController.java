package com.example;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;





public class PrimaryController {

    @FXML
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
    private Button deleteTrackBtn;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button importBtn;
    @FXML
    private Slider volumeSlider;
    @FXML
    ListView<File> soundList;
    @FXML 
    private ComboBox<String> outputAudioDevices;
    private Clip audioClip;

    @FXML
    private void deleteTrackFromList(){
        File selected = soundList.getSelectionModel().getSelectedItem();
        if(selected != null){
            soundList.getItems().remove(selected);
            if(selected.exists()){
                selected.delete();
                System.out.println("Deleted: " + selected.getName());
            } else {
                System.out.println("File not found for deletion: " + selected.getAbsolutePath());
            }
        }
    }
    

@FXML
public void initialize() {
    try {
        soundList.setCellFactory(param -> new javafx.scene.control.ListCell<File>() {
    @Override
    protected void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);

        if (empty || file == null) {
            setGraphic(null);
            setText(null);
        } else {
            javafx.scene.control.Label fileName = new javafx.scene.control.Label(file.getName());
            fileName.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(fileName, Priority.ALWAYS);

            Button rowPlayBtn = new Button("▶");
            rowPlayBtn.setOnAction(e -> {
                soundList.getSelectionModel().select(file);
                handlePlay();
            });

            Button rowDeleteBtn = new Button("X");
            rowDeleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
            rowDeleteBtn.setOnAction(e -> getListView().getItems().remove(file));

            javafx.scene.control.Label moveHandle = new javafx.scene.control.Label("Ξ");
            moveHandle.setStyle("-fx-cursor: hand; -fx-padding: 0 5 0 5; -fx-font-weight: bold;");

            HBox hBox = new HBox(10, fileName, rowPlayBtn, rowDeleteBtn, moveHandle);
            hBox.setAlignment(Pos.CENTER_LEFT);
            setGraphic(hBox);
            setText(null);

            setOnDragDetected(event -> {
                if (getItem() == null) return;
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(getIndex())); 
                db.setContent(content);
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIdx = Integer.parseInt(db.getString());
                    int targetIdx = getIndex();

                    File draggedItem = getListView().getItems().remove(draggedIdx);
                    getListView().getItems().add(targetIdx, draggedItem);
                    getListView().getSelectionModel().select(targetIdx);
                    
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }
                event.consume();
            });
        }
    }
});
        loadAudioOutputDevices();
        volumeSlider.setMin(0.0);
        volumeSlider.setMax(1.0);
        volumeSlider.setValue(0.75);
        File folder = new File("sounds");
        if (folder.exists() && folder.isDirectory()) {
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        if (files != null) {
            soundList.getItems().addAll(files);
        }
    }
        soundList.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2) { 
            File selected = soundList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                System.out.println("Double-clicked: " + selected.getName());
                handlePlay(); 
            }
        }
    });

    } catch (Exception e) {
        e.printStackTrace(); 
    }
}

@FXML
private void handlePlay() {
    File selected = soundList.getSelectionModel().getSelectedItem();
    String selectedMixerName = outputAudioDevices.getSelectionModel().getSelectedItem();

    if (selected == null || selectedMixerName == null) return;

    try {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        Mixer targetMixer = null;
        for (Mixer.Info info : mixerInfos) {
            if (info.getName().equals(selectedMixerName)) {
                targetMixer = AudioSystem.getMixer(info);
                break;
            }
        }
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(selected);
        AudioFormat format = audioStream.getFormat();
        DataLine.Info dataLineInfo = new DataLine.Info(Clip.class, format);
        if (audioClip != null && audioClip.isOpen()) audioClip.close();
        
        audioClip = (Clip) targetMixer.getLine(dataLineInfo);
        audioClip.open(audioStream);
        FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log(volumeSlider.getValue()) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);

        audioClip.start();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void loadAudioOutputDevices() {
        ObservableList<String> deviceNames = FXCollections.observableArrayList();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.getSourceLineInfo().length > 0) {
                deviceNames.add(info.getName());
            }
        }
        outputAudioDevices.setItems(deviceNames);
        if (!deviceNames.isEmpty()) {
            outputAudioDevices.getSelectionModel().select(0);
        }
    }

    @FXML
private void handleImport() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV Files", "*.wav"));
    File sourceFile = fileChooser.showOpenDialog(importBtn.getScene().getWindow());
    if (sourceFile != null) {
        try {
            File directory = new File("sounds");
            if (!directory.exists()) {
                directory.mkdir();
            }
            File destinationFile = new File(directory, sourceFile.getName());
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            soundList.getItems().add(destinationFile);
            
            System.out.println("Saved to app data: " + destinationFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Failed to save file: " + e.getMessage());
        }
    }
}

@FXML
private void handleStop() {
    if (audioClip != null) {
        if (audioClip.isRunning()) {
            audioClip.stop();
            System.out.println("Audio Stopped.");
        }
        audioClip.setFramePosition(0); 
    } else {
        System.out.println("Nothing is currently playing.");
    }
}

    @FXML
    public void cancelButton(ActionEvent event) {
        Stage stage = (Stage) exitBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void refreshMenu(){
        soundList.refresh();
        outputAudioDevices.getItems().clear();
        loadAudioOutputDevices();
        audioClip.close();
    }
    
}


