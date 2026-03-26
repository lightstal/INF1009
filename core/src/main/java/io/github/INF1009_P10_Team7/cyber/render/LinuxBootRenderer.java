package io.github.INF1009_P10_Team7.cyber.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.INF1009_P10_Team7.engine.render.FontManager;

/**
 * LibGDX-backed renderer for {@code cyber.scenes.LinuxBootScene}.
 *
 * <p>Kept in engine so cyber code contains no LibGDX imports/calls.</p>
 */
public class LinuxBootRenderer {

    // Format: [colourId, text]
    // colourId: 0=dim, 1=green, 2=yellow, 3=cyan, 4=white
    private static final Object[][] BOOT_LINES = new Object[][]{
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

    private final float worldW;
    private final float worldH;

    private ShapeRenderer sr;
    private OrthographicCamera camera;
    private StretchViewport viewport;
    private BitmapFont font;
    private SpriteBatch batch;

    public LinuxBootRenderer(float worldW, float worldH) {
        this.worldW = worldW;
        this.worldH = worldH;
    }

    public void load() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(worldW, worldH, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        camera.position.set(worldW / 2f, worldH / 2f, 0);
        camera.update();

        sr = new ShapeRenderer();
        font = FontManager.create(0.88f);
        batch = new SpriteBatch();
    }

    public void resize(int w, int h) {
        if (viewport != null) viewport.update(w, h, true);
        if (camera != null) {
            camera.position.set(worldW / 2f, worldH / 2f, 0);
            camera.update();
        }
    }

    public void render(float stateTime, int lineShown, boolean allShown) {
        if (sr == null || font == null || camera == null || viewport == null) return;

        viewport.apply();
        camera.update();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Subtle scanlines
        sr.begin(ShapeType.Filled);
        sr.setColor(0f, 0.01f, 0f, 1f);
        sr.rect(0, 0, worldW, worldH);
        sr.setColor(0f, 0f, 0f, 0.04f);
        for (float y = 0; y < worldH; y += 3f) sr.rect(0, y, worldW, 1.5f);
        sr.end();

        // Text lines (bottom-up scrolling feel)
        float lineH = 20f;
        float startY = worldH - 20f;
        int maxVisible = (int)(worldH / lineH) - 1;
        int fromLine = Math.max(0, lineShown - maxVisible);
        int toLine = Math.min(lineShown, BOOT_LINES.length);

        sr.begin(ShapeType.Filled);
        // no extra quads; scanlines already done
        sr.end();

        batch.begin();
        for (int i = fromLine; i < toLine; i++) {
            int colourId = (Integer) BOOT_LINES[i][0];
            String text = (String) BOOT_LINES[i][1];
            float y = startY - (i - fromLine) * lineH;

            Color col;
            switch (colourId) {
                case 1:  col = new Color(0.1f, 0.9f, 0.25f, 1f); break;  // green OK
                case 2:  col = new Color(1f, 0.85f, 0.1f, 1f); break;    // yellow warn
                case 3:  col = new Color(0.2f, 0.8f, 1f, 1f); break;     // cyan
                case 4:  col = new Color(1f, 1f, 1f, 1f); break;        // white
                default: col = new Color(0.35f, 0.48f, 0.38f, 1f); break; // dim green
            }

            // Blinking cursor on last shown line
            String display = (i == lineShown - 1 && !allShown)
                ? text + (((int)(stateTime * 3f)) % 2 == 0 ? "|" : " ")
                : text;

            font.setColor(col);
            font.draw(batch, display, 14f, y);
        }
        batch.end();
    }

    public void dispose() {
        try {
            if (sr != null) sr.dispose();
            if (font != null) font.dispose();
            if (batch != null) batch.dispose();
        } finally {
            sr = null;
            font = null;
            camera = null;
            viewport = null;
            batch = null;
        }
    }
}

