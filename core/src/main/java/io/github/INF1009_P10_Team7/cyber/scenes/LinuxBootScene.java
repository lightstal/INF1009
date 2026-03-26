package io.github.INF1009_P10_Team7.cyber.scenes;

import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.cyber.level.TileMap;
import io.github.INF1009_P10_Team7.cyber.render.LinuxBootRenderer;

/**
 * LinuxBootScene - animated Linux kernel boot sequence.
 * Lines appear one-by-one then auto-transitions to the main menu.
 * Skippable by pressing SPACE or clicking.
 *
 * Demonstrates: clean separation between engine (Scene lifecycle) and
 * game-specific UI (cyberpunk boot aesthetic).
 */
/**
 * LinuxBootScene, simulated Linux boot sequence shown at application startup.
 *
 * <p>Displays scrolling boot-log lines to create atmosphere before
 * transitioning to {@link CyberMainMenuScene}. The sequence can be
 * skipped by pressing any key.</p>
 */
public class LinuxBootScene extends Scene {

    private final CyberSceneFactory factory;

    private LinuxBootRenderer renderer;

    private float stateTime  = 0f;
    private int   lineShown  = 0;
    private float lineTimer  = 0f;
    private static final float LINE_DELAY    = 0.065f; // seconds between lines
    private static final float HOLD_DELAY    = 1.8f;   // pause after all lines
    private boolean allShown = false;
    private float   holdTimer = 0f;

    // Boot log lines
    // Format: [colour, text] colour: 0=dim, 1=green, 2=yellow, 3=cyan, 4=white
    private static final Object[][] BOOT_LINES = {
        {3, "GRUB 2.06  GNU GRUB  version 2.06"},
        {0, ""},
        {4, "  * * *  CYBER MAZE ESCAPE  -  CTF INFILTRATION SIM  * * *"},
        {0, ""},
        {0, "[    0.000000] Linux version 6.1.0-kali1-amd64 (kali@build) #1 SMP"},
        {0, "[    0.000001] Command line: BOOT_IMAGE=/vmlinuz-6.1 root=/dev/sda1 ro quiet"},
        {0, "[    0.084312] BIOS-provided physical RAM map:"},
        {0, "[    0.084318] BIOS-e820: [mem 0x0000000000000000-0x000000000009fbff] usable"},
        {0, "[    0.084320] BIOS-e820: [mem 0x00000000000f0000-0x00000000000fffff] reserved"},
        {0, "[    0.210441] ACPI: IRQ0 used by override."},
        {0, "[    0.311784] PCI: Using configuration type 1 for base access"},
        {0, "[    0.445231] clocksource: tsc-early: mask: 0xffffffffffffffff"},
        {0, "[    0.512887] Freeing initrd memory: 28888K"},
        {0, "[    0.623114] NET: Registered PF_INET6 protocol family"},
        {0, "[    0.834991] SCSI subsystem initialized"},
        {0, "[    0.901334] EXT4-fs (sda1): mounted filesystem with ordered data mode"},
        {0, "[    1.023441] systemd[1]: systemd 252 running in system mode"},
        {1, "[  OK  ] Reached target Basic System."},
        {1, "[  OK  ] Started D-Bus System Message Bus."},
        {1, "[  OK  ] Started OpenBSD Secure Shell server daemon."},
        {0, "[    1.445123] Loading kernel module: cyber_maze_engine.ko"},
        {0, "[    1.446012] cyber_maze_engine: module loaded successfully"},
        {1, "[  OK  ] Started Network Time Synchronization."},
        {1, "[  OK  ] Reached target Network."},
        {0, "[    1.778234] Initialising CTF challenge subsystem..."},
        {2, "[  !!  ] Hunter Drone AI activated  -  stealth recommended"},
        {1, "[  OK  ] CTF-Maze-Engine v2.0 initialized"},
        {1, "[  OK  ] TerminalEmulator subsystem online"},
        {1, "[  OK  ] DroneAI state machine loaded (PatrolState / ChaseState)"},
        {1, "[  OK  ] Observer pattern: GameEventSystem ready"},
        {0, "[    2.115673] Loading level maps (40x22 grid)..."},
        {1, "[  OK  ] Level 1  -  RECON LAB loaded"},
        {1, "[  OK  ] Level 2  -  NETWORK HUB loaded"},
        {0, "[    2.345678] Randomising security tokens..."},
        {1, "[  OK  ] CTF flags generated"},
        {2, "[  !!  ] CLASSIFIED: FLAG{r3c0n_m4st3r_1337} seeded to srv-105"},
        {2, "[  !!  ] CLASSIFIED: FLAG{sql_inj3ct10n_0wn3d} seeded to db-server"},
        {2, "[  !!  ] CLASSIFIED: FLAG{h4sh_cr4ck3d_md5} seeded to auth-server"},
        {0, ""},
        {3, "||||||||||||||||||||||||||||||||||||||||||||||||||||"},
        {3, "  SYSTEM BOOT COMPLETE"},
        {3, "  WELCOME, HACKER. GOOD LUCK."},
        {3, "||||||||||||||||||||||||||||||||||||||||||||||||||||"},
        {0, ""},
        {0, "Press SPACE or click to continue..."},
    };

    public LinuxBootScene(IInputController input, IAudioController audio,
                          SceneNavigator nav, CyberSceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;
    }

    @Override
    protected void onLoad() {
        renderer = new LinuxBootRenderer(TileMap.WORLD_W, TileMap.WORLD_H);
        renderer.load();

    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;

        if (!allShown) {
            lineTimer += dt;
            if (lineTimer >= LINE_DELAY) {
                lineTimer = 0f;
                lineShown++;
                if (lineShown >= BOOT_LINES.length) allShown = true;
            }
        } else {
            holdTimer += dt;
        }

        // Skip (keyboard SPACE or mouse click)
        // Use both "just pressed" and "pressed" so holding SPACE/clicking still skips
        // even if the press happened before the first poll/update tick.
        boolean skip = input.isActionJustPressed("START_GAME")
            || input.isActionJustPressed("BOOT_SKIP")
            || input.isActionJustPressed("MENU_CLICK")
            || input.isActionJustPressed("MENU_CONFIRM")
            || input.isActionPressed("START_GAME")
            || input.isActionPressed("BOOT_SKIP")
            || input.isActionPressed("MENU_CLICK");
        if (skip) {
            nav.requestScene(factory.createMainMenuScene());
        }
        if (allShown && holdTimer >= HOLD_DELAY) {
            nav.requestScene(factory.createMainMenuScene());
        }
    }

    @Override
    protected void onRender() {
        renderer.render(stateTime, lineShown, allShown);
    }

    @Override public void resize(int w, int h) { if (renderer != null) renderer.resize(w, h); }
    @Override protected void onUnload()  {}
    @Override protected void onDispose() {
        if (renderer != null) renderer.dispose();
    }
}
