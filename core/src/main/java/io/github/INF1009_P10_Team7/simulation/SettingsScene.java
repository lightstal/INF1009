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
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

/**
 * Settings screen with volume slider, key rebinding, RESUME, and QUIT GAME.
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
    private BitmapFont helpFont;

    /** FreeType font injected into the skin to replace the pixelated bitmap font. */
    private BitmapFont skinFont;

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
            helpFont    = FontManager.create(0.68f);
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

    private void initializeStage() {
        stage = new Stage(viewport);
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
            style.fontColor = new com.badlogic.gdx.graphics.Color(0.05f, 0.05f, 0.05f, 1f);

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
            ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT, () -> nav.popScene());
        resumeButton.setPosition(resumeBtnX, resumeBtnY);
        stage.addActor(resumeButton);

        quitButton = uiElement.createButton("QUIT GAME",
            ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT,
            () -> nav.requestScene(factory.createMainMenuScene()));
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
            if (contains(world.x, world.y, sliderX, sliderY - 12f, sliderW, sliderH + 24f)) {
                float t = (world.x - sliderX) / sliderW;
                volume01 = MathUtils.clamp(t, 0f, 1f);
            }
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
            nav.popScene();
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
        Gdx.gl.glClearColor(0.08f, 0.09f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
    }

    private void renderShapes() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.13f, 0.14f, 0.17f, 1f);
        shape.rect(panelX, panelY, panelW, panelH);
        shape.setColor(0.18f, 0.19f, 0.23f, 1f);
        shape.rect(panelX, panelY + panelH - 80f, panelW, 80f);
        shape.setColor(0.10f, 0.11f, 0.14f, 1f);
        shape.rect(sliderX, sliderY, sliderW, sliderH);
        shape.setColor(0.30f, 0.75f, 0.45f, 1f);
        shape.rect(sliderX, sliderY, sliderW * volume01, sliderH);
        float knobX = sliderX + sliderW * volume01;
        shape.setColor(0.90f, 0.90f, 0.95f, 1f);
        shape.circle(knobX, sliderY + sliderH / 2f, 12f);
        shape.end();
    }

    private void renderText() {
        batch.begin();

        titleFont.setColor(1f, 1f, 1f, 1f);
        layout.setText(titleFont, "SETTINGS");
        titleFont.draw(batch, layout,
            panelX + (panelW - layout.width) / 2f,
            panelY + panelH - 28f);

        volFont.setColor(1f, 1f, 1f, 1f);
        layout.setText(volFont, "MASTER VOLUME");
        volFont.draw(batch, layout, sliderX, sliderY + 54f);

        String percent = (int)(volume01 * 100) + "%";
        layout.setText(volFont, percent);
        volFont.draw(batch, layout, sliderX + sliderW - layout.width, sliderY + 54f);

        sectionFont.setColor(0.86f, 0.92f, 1f, 1f);
        sectionFont.draw(batch, "MOVEMENT",
            panelX + 60f, rowMovementY + KEY_BUTTON_HEIGHT + 22f);
        sectionFont.draw(batch, "GAMEPLAY / TERMINAL",
            panelX + 60f, rowGameplayY + KEY_BUTTON_HEIGHT + 22f);

        helpFont.setColor(0.6f, 0.6f, 0.65f, 1f);
        helpFont.draw(batch,
            "ESC opens settings (hardcoded). TAB closes a terminal (hardcoded).",
            panelX + 60f, panelY + 118f);
        helpFont.draw(batch,
            "ENTER confirms in menus. Arrow keys adjust volume.",
            panelX + 60f, panelY + 94f);

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
        if (helpFont    != null) helpFont.dispose();
        // skinFont is owned by the Skin and disposed with it
        if (skin  != null) skin.dispose();
        if (stage != null) stage.dispose();
    }
}
