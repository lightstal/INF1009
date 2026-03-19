package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;
import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;

public class CyberMainMenuScene extends Scene {

    private final CyberSceneFactory factory;

    private ShapeRenderer  sr;
    private SpriteBatch    batch;
    private GlyphLayout    layout;
    private OrthographicCamera camera;
    private StretchViewport viewport;

    // Each font generated at its native size — no bitmap stretching
    private BitmapFont titleFont;   // old scale 2.5
    private BitmapFont subFont;     // old scale 0.85
    private BitmapFont dotFont;     // old scale 0.7
    private BitmapFont rainFont;    // old scale 0.8
    private BitmapFont briefFont;   // old scale 0.9
    private BitmapFont startFont;   // old scale 1.05

    private float stateTime = 0f;
    // matrix rain columns
    private static final int RAIN_COLS = 50;
    private final float[] rainY    = new float[RAIN_COLS];
    private final float[] rainX    = new float[RAIN_COLS];
    private final float[] rainSpeed= new float[RAIN_COLS];
    private final char[]  rainChar = new char[RAIN_COLS];

    private static final String CHARS = "01アイウエオカキクケコABCDEFGHIJKLMNOPQRSTUVWXYZ@#$%&";

    public CyberMainMenuScene(IInputController input, IAudioController audio,
                              SceneNavigator nav, CyberSceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;
    }

    @Override
    protected void onLoad() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(TileMap.WORLD_W, TileMap.WORLD_H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(TileMap.WORLD_W/2f, TileMap.WORLD_H/2f, 0);
        camera.update();

        sr     = new ShapeRenderer();
        batch  = new SpriteBatch();
        layout = new GlyphLayout();

        // Pre-generate each font at its exact needed size via FreeType
        titleFont = FontManager.createBold(2.5f);
        subFont   = FontManager.create(0.85f);
        dotFont   = FontManager.create(0.7f);
        rainFont  = FontManager.create(0.8f);
        briefFont = FontManager.create(0.9f);
        startFont = FontManager.createBold(1.05f);

        // Init rain columns
        for (int i=0;i<RAIN_COLS;i++) {
            rainX[i]     = (float)(i * (TileMap.WORLD_W / RAIN_COLS));
            rainY[i]     = (float)(Math.random() * TileMap.WORLD_H);
            rainSpeed[i] = 60f + (float)(Math.random() * 120f);
            rainChar[i]  = CHARS.charAt((int)(Math.random() * CHARS.length()));
        }

        audio.setMusic("Music_Menu.mp3");
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;

        // Update rain
        for (int i=0;i<RAIN_COLS;i++) {
            rainY[i] -= rainSpeed[i] * dt;
            if (rainY[i] < -16f) {
                rainY[i] = TileMap.WORLD_H + 16f;
                rainChar[i] = CHARS.charAt((int)(Math.random() * CHARS.length()));
            }
        }

        if (input.isActionJustPressed("START_GAME") || Gdx.input.justTouched()) {
            nav.requestScene(factory.createLevelSelectScene());
        }
    }

    @Override
    protected void onRender() {
        viewport.apply();
        camera.update();

        Gdx.gl.glClearColor(0f, 0f, 0.01f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Background
        sr.setColor(0f, 0f, 0.02f, 1f);
        sr.rect(0, 0, TileMap.WORLD_W, TileMap.WORLD_H);

        // Hex grid background
        float gridAlpha = 0.06f;
        sr.setColor(0f, 0.7f, 0.4f, gridAlpha);
        float hexR = 22f;
        float hexW = hexR * 1.732f;
        float hexH = hexR * 2f;
        for (float hx = 0; hx < TileMap.WORLD_W + hexW; hx += hexW) {
            for (float hy = -hexH/2f; hy < TileMap.WORLD_H + hexH; hy += hexH * 0.75f) {
                float offX = ((int)(hy/hexH) % 2 == 0) ? 0 : hexW/2f;
                drawHexOutline(sr, hx + offX, hy, hexR * 0.9f);
            }
        }

        sr.end();

        // Matrix rain
        batch.begin();
        for (int i=0;i<RAIN_COLS;i++) {
            float lifeAlpha = 0.12f + 0.08f * (float)Math.sin(stateTime * 2f + i);
            rainFont.setColor(0f, lifeAlpha * 2.5f, lifeAlpha, 1f);
            rainFont.draw(batch, String.valueOf(rainChar[i]), rainX[i], rainY[i]);
            // leading bright char
            rainFont.setColor(0f, 1f, 0.5f, 0.6f);
            rainFont.draw(batch, String.valueOf(CHARS.charAt(((int)(stateTime*20f)+i*3)%CHARS.length())),
                rainX[i], rainY[i] + 16f);
        }
        batch.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Central dark panel
        float panW = 560f, panH = 280f;
        float panX = TileMap.WORLD_W/2f - panW/2f;
        float panY = TileMap.WORLD_H/2f - panH/2f;
        sr.setColor(0f, 0f, 0f, 0.75f);
        sr.rect(panX - 2, panY - 2, panW + 4, panH + 4);
        sr.setColor(0.01f, 0.02f, 0.04f, 0.92f);
        sr.rect(panX, panY, panW, panH);

        // Scanline over panel
        sr.setColor(0f, 0f, 0f, 0.06f);
        for (float sy = panY; sy < panY + panH; sy += 3f) sr.rect(panX, sy, panW, 1.5f);

        // Corner brackets
        float bLen = 18f, bThick = 2f;
        sr.setColor(0f, 0.9f, 0.5f, 0.9f);
        sr.rect(panX, panY+panH-bThick, bLen, bThick);
        sr.rect(panX, panY+panH-bLen, bThick, bLen);
        sr.rect(panX+panW-bLen, panY+panH-bThick, bLen, bThick);
        sr.rect(panX+panW-bThick, panY+panH-bLen, bThick, bLen);
        sr.rect(panX, panY, bLen, bThick);
        sr.rect(panX, panY, bThick, bLen);
        sr.rect(panX+panW-bLen, panY, bLen, bThick);
        sr.rect(panX+panW-bThick, panY, bThick, bLen);

        // Pulsing top border line
        float glow = 0.5f + 0.5f * (float)Math.sin(stateTime * 2.5f);
        sr.setColor(0f, glow, 0.4f, 0.8f);
        sr.rect(panX, panY+panH-2, panW, 2);
        sr.rect(panX, panY, panW, 2);

        sr.end();

        // Text — each font is pre-generated at its native size, no scaling
        batch.begin();

        // Title
        float titlePulse = 0.7f + 0.3f * (float)Math.sin(stateTime * 2f);
        titleFont.setColor(0f, titlePulse, titlePulse * 0.55f, 1f);
        String title = "CYBER MAZE ESCAPE";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, TileMap.WORLD_W/2f - layout.width/2f, panY + panH - 20f);

        // Subtitle
        subFont.setColor(0.3f, 0.6f, 0.7f, 0.8f);
        String sub = "[ WHITE-HAT INFILTRATION SIMULATION ]";
        layout.setText(subFont, sub);
        subFont.draw(batch, sub, TileMap.WORLD_W/2f - layout.width/2f, panY + panH - 52f);

        // Divider dots
        dotFont.setColor(0f, 0.4f, 0.25f, 0.7f);
        dotFont.draw(batch, "· · · · · · · · · · · · · · · · · · · · · · · · · · ·",
            panX + 20f, panY + panH - 72f);

        // Mission brief
        String[] brief = {
            "Three servers. Three CTF challenges. One exit.",
            "Hack each terminal without triggering the Hunter Drone.",
            "",
            "WASD   -  move          [E]  -  access terminal",
            "TAB    -  panic-close terminal",
            "↑↓ arrows  -  command history inside terminal"
        };
        float by = panY + panH - 100f;
        for (String line : brief) {
            if (line.isEmpty()) { by -= 8f; continue; }
            briefFont.setColor(line.startsWith("W") || line.startsWith("T") || line.startsWith("↑")
                ? new Color(0.55f,0.55f,0.6f,1f) : new Color(0.75f,0.85f,0.75f,1f));
            briefFont.draw(batch, line, panX + 30f, by);
            by -= 18f;
        }

        // Press start blink
        boolean blink = (int)(stateTime * 2f) % 2 == 0;
        startFont.setColor(blink ? new Color(0f,1f,0.5f,1f) : new Color(0f,0.5f,0.3f,0.7f));
        String ps = " -  PRESS [SPACE] OR CLICK TO START  - ";
        layout.setText(startFont, ps);
        startFont.draw(batch, ps, TileMap.WORLD_W/2f - layout.width/2f, panY + 24f);

        batch.end();
    }

    private void drawHexOutline(ShapeRenderer sr, float cx, float cy, float r) {
        int sides = 6;
        for (int i=0;i<sides;i++) {
            double a1 = 2*Math.PI*i/sides - Math.PI/6;
            double a2 = 2*Math.PI*(i+1)/sides - Math.PI/6;
            float x1=(float)Math.cos(a1)*r+cx, y1=(float)Math.sin(a1)*r+cy;
            float x2=(float)Math.cos(a2)*r+cx, y2=(float)Math.sin(a2)*r+cy;
            sr.rectLine(x1, y1, x2, y2, 0.8f);
        }
    }

    @Override public void resize(int w, int h) { if (viewport != null) viewport.update(w, h, true); }
    @Override protected void onUnload() {}
    @Override protected void onDispose() {
        if (sr!=null) sr.dispose();
        if (batch!=null) batch.dispose();
        if (titleFont!=null) titleFont.dispose();
        if (subFont!=null) subFont.dispose();
        if (dotFont!=null) dotFont.dispose();
        if (rainFont!=null) rainFont.dispose();
        if (briefFont!=null) briefFont.dispose();
        if (startFont!=null) startFont.dispose();
    }
}
