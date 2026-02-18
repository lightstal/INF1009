package io.github.INF1009_P10_Team7.engine.inputoutput;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Handles all audio operations for the engine (no event system).
 *
 * - Background music: streamed (one at a time)
 * - Sound effects: cached short clips
 */
public class AudioOutput {

    private Music currentMusic;

    // Volume controls (0.0 to 1.0)
    private float musicVolume = 0.4f;
    private float sfxVolume = 1.0f;

    // Sound effect cache
    private final Map<String, Sound> soundCache = new HashMap<>();

    public AudioOutput() {
        this.currentMusic = null;
    }

    public void setMusic(String audioPath) {
        stopMusic();
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(audioPath));
        currentMusic.setVolume(musicVolume);
        currentMusic.setLooping(true);
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    public void playSound(String audioPath) {
        if (sfxVolume <= 0f) {
            Gdx.app.log("AudioOutput", audioPath + " skipped (volume is 0).");
            return;
        }
        Sound sound = soundCache.get(audioPath);
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(audioPath));
            soundCache.put(audioPath, sound);
        }
        sound.play(sfxVolume);
        Gdx.app.log("AudioOutput", audioPath + " Sound played.");
    }

    public float getMusicVolume() { return musicVolume; }

    public void setMusicVolume(float volume) {
        musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
        Gdx.app.log("AudioOutput", "Music volume set to: " + (int)(musicVolume * 100) + "%");
    }

    public float getSoundVolume() { return sfxVolume; }

    public void setSoundVolume(float volume) {
        sfxVolume = Math.max(0f, Math.min(1f, volume));
        Gdx.app.log("AudioOutput", "SFX volume set to: " + (int)(sfxVolume * 100) + "%");
    }

    public void dispose() {
        stopMusic();
        for (Sound s : soundCache.values()) {
            s.dispose();
        }
        soundCache.clear();
        Gdx.app.log("AudioOutput", "AudioOutput disposed");
    }
}
