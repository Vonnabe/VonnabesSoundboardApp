package com.example;

import java.io.File;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javafx.application.Platform;

public class HotKeyManager implements NativeKeyListener {

    private HashMap<Integer, File> hotkeyMap = new HashMap<>();

    private boolean isAssigning = false;
    private String currentSoundFile = "";

    public void init() {
        try {
            // Register the global hook
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
        }
    }

    public void startListening(String fileName) {
        this.isAssigning = true;
        this.currentSoundFile = fileName;
        System.out.println("Listening for key for: " + fileName);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (isAssigning) {
            String keyName = NativeKeyEvent.getKeyText(e.getKeyCode());
            System.out.println("Assigned " + keyName + " to " + currentSoundFile);
            
            isAssigning = false; // Reset state
            
            // If you need to update JavaFX UI, use Platform.runLater
            Platform.runLater(() -> {
                // Update your button text here if needed
            });
        }
    }

    // Required empty methods to satisfy the interface
    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}

public class PlaySound {
    private Clip clip;

    public void play(String fileName) {
        try {
            // Check if fileName already has .wav, if not, add it
            if (!fileName.toLowerCase().endsWith(".wav")) {
                fileName += ".wav";
            }

            File file = new File("sounds/" + fileName);
            
            if (!file.exists()) {
                System.err.println("ERROR: File not found at " + file.getAbsolutePath());
                return;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(stream);
            clip.start();
            
        } catch (Exception e) {
            System.err.println("Playback Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
}