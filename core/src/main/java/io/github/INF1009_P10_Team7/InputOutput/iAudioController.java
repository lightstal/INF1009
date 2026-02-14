package io.github.INF1009_P10_Team7.InputOutput;

public interface iAudioController {
	void loadSound(String name, String path);
    void playSound(String name);
    void playMusic(String path);
    void pauseMusic();
    void resumeMusic();
    void stopMusic();
    void setMusicVolume(float volume);
    float getMusicVolume();
}
