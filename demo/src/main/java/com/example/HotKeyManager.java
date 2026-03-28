package com.example;



import java.io.File;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
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

    //private Slider volumeSlider;

//public void setVolumeSlider(Slider slider) {
    //this.volumeSlider = slider;
//}

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
                engine.play(soundToPlay, 0.5); // Play with default volume (0.5)
            }
        }
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}
}

class PlaySound {
public void play(String fileName, double volume) { // Add volume parameter (0.0 to 1.0)
    try {
        File file = new File("sounds/" + fileName);
        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        Clip clip = AudioSystem.getClip();
        clip.open(stream);

        // VOLUME LOGIC
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            // Convert 0.0-1.0 slider value to Decibels (Java Sound uses dB)
            float dB = (float) (Math.log(volume != 0 ? volume : 0.0001) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }

        clip.start();
        // Close clip when done
        clip.addLineListener(e -> { if (e.getType() == LineEvent.Type.STOP) clip.close(); });

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}