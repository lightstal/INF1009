package io.github.INF1009_P10_Team7.InputOutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

public class AudioOutput {
    private final Map<String, Sound> soundCache = new HashMap<>();
    private Music currentMusic;
    private float musicVolume = 0.5f;

    public void loadSound(String name, String path) {
        soundCache.put(name, Gdx.audio.newSound(Gdx.files.internal(path)));
    }

    public void playSound(String name) {
        if (soundCache.containsKey(name)) {
            soundCache.get(name).play();
        }
    }

    public void playMusic(String path) {
        stopMusic();
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentMusic.setLooping(true);
        currentMusic.setVolume(musicVolume);
        currentMusic.play();
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) currentMusic.pause();
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(this.musicVolume);
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void dispose() {
        stopMusic();
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
    }
}