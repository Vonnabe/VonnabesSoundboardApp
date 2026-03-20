package com.example;



import java.io.File;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javafx.application.Platform;


public class HotKeyManager implements NativeKeyListener {

    private HashMap<Integer, String> hotkeyMap = new HashMap<>();
    private boolean isAssigning = false;
    private String currentSoundFile = "";
    private PlaySound engine = new PlaySound(); 

    public void init() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            System.err.println("Native Hook Error: " + ex.getMessage());
        }
    }

    public void startListening(String fileName) {
        this.isAssigning = true;
        this.currentSoundFile = fileName;
        System.out.println("LISTENING: Press a key to bind to " + fileName);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();

        if (isAssigning) {

            hotkeyMap.put(keyCode, currentSoundFile); 
            String keyName = NativeKeyEvent.getKeyText(keyCode);
            System.out.println("SUCCESS: " + keyName + " is now linked to " + currentSoundFile);
            
            isAssigning = false; 

            Platform.runLater(() -> {
            });
        } else {

            if (hotkeyMap.containsKey(keyCode)) {
                String soundToPlay = hotkeyMap.get(keyCode);
                System.out.println("HOTKEY TRIGGERED: Playing " + soundToPlay);
                engine.play(soundToPlay);
            }
        }
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}
}

class PlaySound {
    public void play(String fileName) {
        try {
            if (!fileName.toLowerCase().endsWith(".wav")) {
                fileName += ".wav";
            }

            File file = new File("sounds/" + fileName);
            
            if (!file.exists()) {
                System.err.println("FILE MISSING: " + file.getAbsolutePath());
                return;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            clip.start();
            
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (Exception e) {
            System.err.println("Playback Error: " + e.getMessage());
        }
    }
}