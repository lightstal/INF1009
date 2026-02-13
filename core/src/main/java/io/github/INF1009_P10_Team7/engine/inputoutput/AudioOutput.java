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
    private MusicState musicState;

    // Volume controls (0.0 to 1.0)
    private float musicVolume = 0.4f;
    private float sfxVolume = 1.0f;

    // Sound effect cache
    private final Map<String, Sound> soundCache = new HashMap<>();

    public AudioOutput() {
        this.currentMusic = null;
        this.musicState = MusicState.STOPPED;
    }

    public void setMusic(String audioPath) {
        stopMusic();
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(audioPath));
        currentMusic.setVolume(musicVolume);
        currentMusic.setLooping(true);
        currentMusic.play();
        musicState = MusicState.PLAYING;
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
        musicState = MusicState.STOPPED;
    }

    public void pauseMusic() {
        if (currentMusic != null && musicState == MusicState.PLAYING) {
            currentMusic.pause();
            musicState = MusicState.PAUSED;
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && musicState == MusicState.PAUSED) {
            currentMusic.play();
            musicState = MusicState.PLAYING;
        }
    }

    public void playSound(String audioPath) {
        Sound sound = soundCache.get(audioPath);
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(audioPath));
            soundCache.put(audioPath, sound);
        }
        sound.play(sfxVolume);
    }

    public float getMusicVolume() { return musicVolume; }

    public void setMusicVolume(float volume) {
        musicVolume = clamp01(volume);
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
        Gdx.app.log("AudioOutput", "Music volume set to: " + (int)(musicVolume * 100) + "%");
    }

    public float getSoundVolume() { return sfxVolume; }

    public void setSoundVolume(float volume) {
        sfxVolume = clamp01(volume);
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

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
