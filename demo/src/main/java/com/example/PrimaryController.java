package com.example;


import java.io.File;

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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
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
    private Button importBtn;
    @FXML
    private Slider volumeSlider;
    @FXML
    ListView<File> soundList;
    @FXML 
    private ComboBox<String> outputAudioDevices;
    private Clip audioClip;
    

@FXML
public void initialize() {
    try {
        loadAudioOutputDevices();
        volumeSlider.setMin(0.0);
        volumeSlider.setMax(1.0);
        volumeSlider.setValue(0.75);
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
    fileChooser.setTitle("Select Audio Track");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac")
    );
    Stage stage = (Stage) importBtn.getScene().getWindow();
    File file = fileChooser.showOpenDialog(stage);

    if (file != null) {
        soundList.getItems().add(file);
        System.out.println("Imported: " + file.getName());
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

    
}
