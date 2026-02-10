package io.github.INF1009_P10_Team7.engine.inputoutput;

public interface AudioController {
    // Getters (Retrieve Values)
    float getMusicVolume();
    float getSoundVolume();

    // Setters (Direct Control)
    void setMusicVolume(float volume);
    void setSoundVolume(float volume);
    
    // Actions
    void setMusic(String filePath);
    void stopMusic();
    void playSound(String filePath);
}
