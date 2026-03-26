package io.github.INF1009_P10_Team7.simulation;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.engine.UIManagement.KeyBindingButton;
import io.github.INF1009_P10_Team7.engine.UIManagement.UIElement;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.engine.render.FontManager;

/**
 * SettingsScene — in-game settings overlay for adjusting audio volumes
 * and rebinding keyboard/mouse controls.
 *
 * <p>Pushed on top of the current scene (not replacing it) so the game
 * world is preserved underneath. Overrides {@link #blocksWorldUpdate()}
 * to pause movement and collision while the settings are open.</p>
 *
 * <p>Uses {@link io.github.INF1009_P10_Team7.engine.UIManagement.KeyBindingButton}
 * widgets to allow interactive key rebinding. Carefully manages the LibGDX 
 * InputProcessor to ensure control is returned to the global input manager 
 * upon closing.</p>
 */
public class SettingsScene extends Scene {

    private final SceneFactory factory;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shape;
    private SpriteBatch batch;
    private GlyphLayout layout;
    private float volume01;

    private BitmapFont titleFont;
    private BitmapFont volFont;
    private BitmapFont sectionFont;

    /** FreeType font injected into the skin to replace the pixelated bitmap font. */
    private BitmapFont skinFont;

    private float   stateTime     = 0f;
    private boolean sliderDragging = false;

    private float panelX, panelY, panelW, panelH;
    private float sliderX, sliderY, sliderW, sliderH;
    private float resumeBtnX, resumeBtnY;
    private float quitBtnX, quitBtnY;

    private static final float VW = 1280f;
    private static final float VH = 704f;

    private Skin skin;
    private TextButton resumeButton;
    private TextButton quitButton;
    private Stage stage;
    private UIElement uiElement;
    private List<KeyBindingButton> keyBindingButtons;

    /** 
     * Stores the global input processor before Settings steals it, 
     * so it can be restored when the menu closes. 
     */
    private com.badlogic.gdx.InputProcessor previousProcessor;

    private static final float ACTION_BUTTON_WIDTH  = 280f;
    private static final float ACTION_BUTTON_HEIGHT = 54f;
    private static final float ACTION_BUTTON_GAP    = 40f;

    private static final float KEY_BUTTON_HEIGHT = 36f;
    private static final float KEY_HORIZONTAL_GAP = 10f;
    private static final float KEY_VERTICAL_GAP = 82f;

    /**
     * Font scale for key binding labels on the skin font.
     * Skin font is generated at 72 px (same base as old PressStart2P).
     * 0.15 × 72 = ~10.8 virtual px per glyph height.
     */
    private static final float KEY_FONT_SCALE = 0.15f;

    private static final float KEY_MARGIN = 50f;

    private float rowMovementY;
    private float rowGameplayY;

    private static final class BindingSpec {
        final String action;
        final String label;
        BindingSpec(String action, String label) {
            this.action = action;
            this.label = label;
        }
    }

    private static final BindingSpec[] MOVEMENT = {
        new BindingSpec("UP",    "Up"),
        new BindingSpec("DOWN",  "Down"),
        new BindingSpec("LEFT",  "Left"),
        new BindingSpec("RIGHT", "Right")
    };

    private static final BindingSpec[] GAMEPLAY = {
        new BindingSpec("INTERACT", "Interact"),
        new BindingSpec("HELP",     "Ping")
    };

    public SettingsScene(IInputController input, IAudioController audio,
                         SceneNavigator nav, SceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;
    }

    @Override
    protected void onLoad() {
        initializeCamera();
        initializeRendering();
        initializeVolume();
        recalcUI();
        initializeStage();
        initializeUI();
        Gdx.app.log("Scene", "SettingsScene loaded");
    }

    private void initializeCamera() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(VW, VH, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(VW / 2f, VH / 2f, 0);
        camera.update();
    }

    private void initializeRendering() {
        if (shape == null) {
            shape  = new ShapeRenderer();
            batch  = new SpriteBatch();
            layout = new GlyphLayout();

            titleFont   = FontManager.createBold(2.0f);
            volFont     = FontManager.create(1.15f);
            sectionFont = FontManager.create(0.85f);
        }
    }

    private void initializeVolume() {
        volume01 = audio.getMusicVolume();
    }

    private void recalcUI() {
        panelW = 1120f;
        panelH = 620f;
        panelX = (VW - panelW) / 2f;
        panelY = (VH - panelH) / 2f;

        sliderW = panelW - 120f;
        sliderH = 16f;
        sliderX = panelX + 60f;
        sliderY = panelY + panelH - 126f;

        rowMovementY = panelY + panelH - 244f;
        rowGameplayY = rowMovementY - KEY_VERTICAL_GAP;

        float totalBtnW = ACTION_BUTTON_WIDTH * 2 + ACTION_BUTTON_GAP;
        resumeBtnX = panelX + (panelW - totalBtnW) / 2f;
        resumeBtnY = panelY + 26f;
        quitBtnX   = resumeBtnX + ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP;
        quitBtnY   = resumeBtnY;
    }

    /**
     * Initializes the LibGDX Stage and temporarily stores the global input
     * processor before overtaking the input stream.
     */
    private void initializeStage() {
        stage = new Stage(viewport);
        previousProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(stage);
    }

    private void initializeUI() {
        try {
            skin = new Skin(Gdx.files.internal("buttons/name2d.json"));

            // Replace the pixelated PressStart2P bitmap font with a crisp
            // FreeType font at the same 72 px base size.
            skinFont = FontManager.createForSkin(72);

            // Must update the actual TextButtonStyle font reference —
            // skin.add() alone won't retroactively update loaded styles.
            com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle style =
                skin.get("default", com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle.class);
            style.font = skinFont;
            style.fontColor      = new com.badlogic.gdx.graphics.Color(0f,    0.82f, 0.42f, 1f); // normal: cyber green
            style.overFontColor  = new com.badlogic.gdx.graphics.Color(0.78f, 1f,    0.86f, 1f); // hover:  bright mint
            style.downFontColor  = new com.badlogic.gdx.graphics.Color(0.03f, 0.07f, 0.04f, 1f); // press:  near-black

            uiElement = new UIElement(skin, true);
            createActionButtons();
            createKeyBindings();
            Gdx.app.log("SettingsScene", "UI initialized with FreeType skin font");
        } catch (Exception e) {
            Gdx.app.error("SettingsScene", "UI Load Error", e);
        }
    }

    private void createActionButtons() {
        resumeButton = uiElement.createButton("RESUME",
            ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT, () -> {
                restoreInputProcessor();
                nav.popScene();
            });
        resumeButton.getLabel().setFontScale(0.30f);
        resumeButton.setPosition(resumeBtnX, resumeBtnY);
        stage.addActor(resumeButton);

        quitButton = uiElement.createButton("RETURN MENU",
            ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT,
            () -> {
                restoreInputProcessor();
                nav.requestScene(factory.createMainMenuScene());
            });
        quitButton.getLabel().setFontScale(0.30f);
        quitButton.setPosition(quitBtnX, quitBtnY);
        stage.addActor(quitButton);
    }

    private void createKeyBindings() {
        keyBindingButtons = new java.util.ArrayList<>();
        addBindingRow(MOVEMENT, rowMovementY);
        addBindingRow(GAMEPLAY, rowGameplayY);
    }

    private void addBindingRow(BindingSpec[] specs, float y) {
        float availableW = panelW - KEY_MARGIN * 2f;
        float buttonW = (availableW - (specs.length - 1) * KEY_HORIZONTAL_GAP) / specs.length;
        buttonW = Math.min(buttonW, 250f);
        float totalW = specs.length * buttonW + (specs.length - 1) * KEY_HORIZONTAL_GAP;
        float startX = panelX + (panelW - totalW) / 2f;
        for (int i = 0; i < specs.length; i++) {
            float x = startX + i * (buttonW + KEY_HORIZONTAL_GAP);
            addKeyBinding(specs[i].action, specs[i].label, x, y, buttonW);
        }
    }

    private void addKeyBinding(String action, String label,
                               float x, float y, float width) {
        KeyBindingButton btn = uiElement.createKeyBindingButton(
            action, label, width, KEY_BUTTON_HEIGHT, input);
        btn.setPosition(x, y);
        btn.getLabel().setFontScale(KEY_FONT_SCALE);
        stage.addActor(btn);
        keyBindingButtons.add(btn);
    }

    @Override
    protected void onUpdate(float delta) {
        stateTime += delta;
        handleKeyboardShortcuts();

        float previousVolume = volume01;

        if (input.isActionPressed("MENU_LEFT")  || input.isActionPressed("LEFT"))
            volume01 -= delta * 0.6f;
        if (input.isActionPressed("MENU_RIGHT") || input.isActionPressed("RIGHT"))
            volume01 += delta * 0.6f;
        volume01 = MathUtils.clamp(volume01, 0f, 1f);

        if (Gdx.input.justTouched()) {
            Vector2 world = viewport.unproject(
                new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            sliderDragging = contains(world.x, world.y, sliderX, sliderY - 12f, sliderW, sliderH + 24f);
        }
        if (!Gdx.input.isTouched()) {
            sliderDragging = false;
        }
        if (sliderDragging) {
            Vector2 world = viewport.unproject(
                new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            float t = (world.x - sliderX) / sliderW;
            volume01 = MathUtils.clamp(t, 0f, 1f);
        }

        if (Math.abs(volume01 - previousVolume) > 0.001f) {
            audio.setMusicVolume(volume01);
            audio.setSoundVolume(volume01);
        }
    }

    private void handleKeyboardShortcuts() {
        if (input.isActionJustPressed("MENU_BACK")
                || input.isActionJustPressed("MENU_CONFIRM")
                || input.isActionJustPressed("SETTINGS")) {
            restoreInputProcessor();
            nav.popScene();
        }
    }

    /** Restores the global input processor that was active before Settings opened. */
    private void restoreInputProcessor() {
        if (previousProcessor != null) {
            Gdx.input.setInputProcessor(previousProcessor);
        }
    }

    private boolean contains(float px, float py,
                             float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    @Override
    protected void onRender() {
        if (camera == null || viewport == null) return;
        prepareRender();
        renderShapes();
        renderText();
        renderStage();
    }

    private void prepareRender() {
        viewport.apply();
        camera.update();
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
    }

    private void renderShapes() {
        float pulse = 0.5f + 0.5f * MathUtils.sin(stateTime * 2.0f);

        shape.begin(ShapeRenderer.ShapeType.Filled);

        // ── Background dot grid ──────────────────────────────────────────────
        shape.setColor(0.07f, 0.10f, 0.15f, 1f);
        for (float gx = 0; gx <= VW; gx += 40f) {
            for (float gy = 0; gy <= VH; gy += 40f) {
                shape.rect(gx - 1f, gy - 1f, 2f, 2f);
            }
        }

        // ── Panel outer glow (multi-layer) ───────────────────────────────────
        shape.setColor(0f, 0.78f, 0.40f, 0.10f + 0.05f * pulse);
        shape.rect(panelX - 8, panelY - 8, panelW + 16, panelH + 16);
        shape.setColor(0f, 0.68f, 0.34f, 0.20f + 0.06f * pulse);
        shape.rect(panelX - 4, panelY - 4, panelW + 8, panelH + 8);
        shape.setColor(0f, 0.58f, 0.28f, 0.32f);
        shape.rect(panelX - 2, panelY - 2, panelW + 4, panelH + 4);

        // ── Panel body ───────────────────────────────────────────────────────
        shape.setColor(0.07f, 0.09f, 0.13f, 1f);
        shape.rect(panelX, panelY, panelW, panelH);

        // ── Title bar ────────────────────────────────────────────────────────
        shape.setColor(0f, 0.16f, 0.10f, 1f);
        shape.rect(panelX, panelY + panelH - 80f, panelW, 80f);
        shape.setColor(0f, 0.82f, 0.42f, 0.90f);
        shape.rect(panelX, panelY + panelH - 80f, panelW, 2f);
        shape.setColor(0f, 0.82f, 0.42f, 0.55f + 0.20f * pulse);
        shape.rect(panelX, panelY + panelH - 2f, panelW, 2f);

        // ── Panel side borders ───────────────────────────────────────────────
        shape.setColor(0f, 0.52f, 0.26f, 0.48f);
        shape.rect(panelX, panelY, 2f, panelH - 80f);
        shape.rect(panelX + panelW - 2f, panelY, 2f, panelH - 80f);
        shape.setColor(0f, 0.42f, 0.20f, 0.40f);
        shape.rect(panelX, panelY, panelW, 2f);

        // ── Decorative corner brackets ───────────────────────────────────────
        float br = 14f, bt = 2f;
        float bAlpha = 0.55f + 0.20f * pulse;
        shape.setColor(0f, 0.82f, 0.42f, bAlpha);
        // content top-left
        shape.rect(panelX + 10f, panelY + panelH - 88f - br, bt, br);
        shape.rect(panelX + 10f, panelY + panelH - 88f - bt, br, bt);
        // content bottom-right
        shape.rect(panelX + panelW - 10f - bt, panelY + 10f, bt, br);
        shape.rect(panelX + panelW - 10f - br, panelY + 10f, br, bt);
        // title top-left
        shape.rect(panelX + 10f, panelY + panelH - 10f - br, bt, br);
        shape.rect(panelX + 10f, panelY + panelH - 10f - bt, br, bt);
        // title top-right
        shape.rect(panelX + panelW - 10f - bt, panelY + panelH - 10f - br, bt, br);
        shape.rect(panelX + panelW - 10f - br, panelY + panelH - 10f - bt, br, bt);

        // ── Volume section label accent bar ──────────────────────────────────
        shape.setColor(0f, 0.82f, 0.42f, 0.80f);
        shape.rect(panelX + 40f, sliderY + 22f, 3f, 14f);

        // ── Volume slider (redesigned) ───────────────────────────────────────
        shape.setColor(0.04f, 0.07f, 0.10f, 1f);
        shape.rect(sliderX, sliderY - 1f, sliderW, sliderH + 2f);
        shape.setColor(0.08f, 0.12f, 0.17f, 1f);
        shape.rect(sliderX + 1f, sliderY, sliderW - 2f, sliderH);
        if (volume01 > 0.005f) {
            shape.setColor(0f, 0.75f, 0.38f, 0.30f);
            shape.rect(sliderX, sliderY - 3f, sliderW * volume01, sliderH + 6f);
            shape.setColor(0f, 0.78f, 0.40f, 1f);
            shape.rect(sliderX, sliderY, sliderW * volume01, sliderH);
        }
        shape.setColor(0f, 0.42f, 0.20f, 0.45f);
        for (int t = 1; t < 10; t++) {
            float tx = sliderX + sliderW * (t / 10f);
            shape.rect(tx, sliderY + sliderH * 0.2f, 1f, sliderH * 0.6f);
        }
        float knobX = sliderX + sliderW * volume01;
        float knobCY = sliderY + sliderH / 2f;
        shape.setColor(0f, 0.82f, 0.42f, 0.30f + 0.12f * pulse);
        shape.circle(knobX, knobCY, 16f, 24);
        shape.setColor(0.10f, 0.16f, 0.22f, 1f);
        shape.circle(knobX, knobCY, 12f, 24);
        shape.setColor(0f, 0.82f, 0.42f, 0.85f + 0.10f * pulse);
        shape.circle(knobX, knobCY, 6f, 16);
        shape.setColor(0.80f, 1f, 0.88f, 0.85f);
        shape.circle(knobX, knobCY, 3f, 12);

        // ── Section separator after slider ───────────────────────────────────
        shape.setColor(0f, 0.38f, 0.20f, 0.40f);
        shape.rect(panelX + 40f, sliderY - 20f, panelW - 80f, 1f);

        // ── Section accent bars + separators (keybinding rows) ───────────────
        float movLabelY = rowMovementY + KEY_BUTTON_HEIGHT;
        shape.setColor(0f, 0.82f, 0.42f, 0.80f);
        shape.rect(panelX + 40f, movLabelY + 8f, 3f, 14f);
        shape.setColor(0f, 0.38f, 0.20f, 0.40f);
        shape.rect(panelX + 40f, movLabelY + 36f, panelW - 80f, 1f);

        float gpLabelY = rowGameplayY + KEY_BUTTON_HEIGHT;
        shape.setColor(0f, 0.82f, 0.42f, 0.80f);
        shape.rect(panelX + 40f, gpLabelY + 8f, 3f, 14f);
        shape.setColor(0f, 0.38f, 0.20f, 0.40f);
        shape.rect(panelX + 40f, gpLabelY + 36f, panelW - 80f, 1f);

        // ── Separator above action buttons ───────────────────────────────────
        shape.setColor(0f, 0.38f, 0.20f, 0.40f);
        shape.rect(panelX + 40f, panelY + 104f, panelW - 80f, 1f);

        shape.end();
    }

    private void renderText() {
        batch.begin();

        // ── Title ─────────────────────────────────────────────────────────────
        titleFont.setColor(0f, 0.88f, 0.48f, 1f);
        layout.setText(titleFont, "SETTINGS");
        titleFont.draw(batch, layout,
            panelX + (panelW - layout.width) / 2f,
            panelY + panelH - 26f);

        // ── Volume ────────────────────────────────────────────────────────────
        volFont.setColor(0.78f, 0.94f, 0.84f, 1f);
        layout.setText(volFont, "MASTER VOLUME");
        volFont.draw(batch, layout, sliderX + 12f, sliderY + 36f);

        String percent = (int)(volume01 * 100) + "%";
        volFont.setColor(0f, 0.82f, 0.42f, 1f);
        layout.setText(volFont, percent);
        volFont.draw(batch, layout, sliderX + sliderW - layout.width, sliderY + 36f);

        // ── Section labels ────────────────────────────────────────────────────
        sectionFont.setColor(0.72f, 0.92f, 0.80f, 1f);
        sectionFont.draw(batch, "MOVEMENT",
            panelX + 52f, rowMovementY + KEY_BUTTON_HEIGHT + 22f);
        sectionFont.draw(batch, "GAMEPLAY",
            panelX + 52f, rowGameplayY + KEY_BUTTON_HEIGHT + 22f);

        batch.end();
    }

    private void renderStage() {
        if (stage != null) {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
        recalcUI();
        if (stage != null) {
            stage.clear();
            createActionButtons();
            createKeyBindings();
        }
    }

    @Override public boolean blocksWorldUpdate() { return true; }
    @Override protected void onUnload() {}

    @Override
    protected void onDispose() {
        if (shape != null) shape.dispose();
        if (batch != null) batch.dispose();
        if (titleFont   != null) titleFont.dispose();
        if (volFont     != null) volFont.dispose();
        if (sectionFont != null) sectionFont.dispose();
        if (skin  != null) skin.dispose();
        if (stage != null) stage.dispose();
    }
}