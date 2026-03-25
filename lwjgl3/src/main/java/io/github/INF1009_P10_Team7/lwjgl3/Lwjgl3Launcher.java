package io.github.INF1009_P10_Team7.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.INF1009_P10_Team7.simulation.Part1SimulationApp;


/**
 * Lwjgl3Launcher — desktop entry point for the application.
 *
 * <p>Configures the LWJGL3 backend (window title, size, FPS) and
 * launches the LibGDX application with {@code Part1SimulationApp} as
 * the application listener. {@link StartupHelper} handles JVM restarts
 * needed for macOS ARM support.</p>
 */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.


        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Cyber Maze Escape — CTF Infiltration Sim");


        configuration.useVsync(true);

        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);


        configuration.setWindowedMode(1280, 720);
        configuration.setResizable(true);


        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");


        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        new Lwjgl3Application(new Part1SimulationApp(), configuration);
    }
}
