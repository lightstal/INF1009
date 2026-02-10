package io.github.INF1009_P10_Team7.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * MainMenuScene
 *
 * Controls:
 * - SPACE -> switch to GameScene
 *
 * Visual:
 * - Blue background
 */
public class MainMenuScene extends Scene {

    private Stage stage;
    private Skin skin;
    private TextButton startButton;

    public MainMenuScene(SceneManager sceneManager) {
        super(sceneManager);
    }

    @Override
    protected void onLoad() {
        // Log for testing (marker can see lifecycle)
        Gdx.app.log("Scene", "MainMenuScene loaded");

        context.getAudioController().setMusic("Music_Menu.mp3");
        Gdx.app.log("Audio Output", "MainMenu Music loaded");


        // =========== Created start button ===============
        // =========== Button created using code composer, might need to design again ============
        stage = new Stage(new ScreenViewport());

        // CRITICAL FIX: Force initial viewport update to prevent black screen
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        try {
            skin = new Skin(Gdx.files.internal("buttons/name2d.json"));

            // =================== Button created ===================
            startButton = new TextButton("START GAME", skin, "default");
            // =================== Size & Position ==================
            startButton.setSize(200, 60);
            updateButtonPosition(); // Use helper method to center button

            // ======================== Event listener for clicking button ==================
            startButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.log("UI", "Start Button Clicked");
                    // Switch scene
                    sceneManager.requestScene(new GameScene(sceneManager));
                }
            });

            // ======= to set the button
            stage.addActor(startButton);
        } catch (Exception e) {
            Gdx.app.error("UI", "Failed to load skin: buttons/name2d.json", e);
        }

        Gdx.input.setInputProcessor(stage); // ==== for user input
    }

    private void updateButtonPosition() {
        if (startButton != null) {
            startButton.setPosition(
                (stage.getViewport().getWorldWidth() - startButton.getWidth()) / 2,
                stage.getViewport().getWorldHeight() / 2
            );
        }
    }

    @Override
    protected void onUpdate(float delta) {
        // Press SPACE to go to GameScene
        stage.act(delta); // ==== handles visuals

        if (context.getInputController().isActionJustPressed("START_GAME")) {
            Gdx.app.log("Input", "Key binded to 'START_GAME' action was pressed");
            sceneManager.requestScene(new GameScene(sceneManager));
        }

    }

    @Override
    protected void onRender() {
        // Blue screen
        ScreenUtils.clear(0.2f, 0.2f, 0.8f, 1f);

        // ========== draw the button ======
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        // Resize log to show resize forwarding works
        Gdx.app.log("Scene", "MainMenuScene resize: " + width + "x" + height);

        // CRITICAL FIX: Update the stage's viewport when window is resized
        if (stage != null) {
            stage.getViewport().update(width, height, true);
            updateButtonPosition(); // Recenter button after resize
        }
    }

    @Override
    protected void onUnload() {
        // Log for testing
        Gdx.app.log("Scene", "MainMenuScene unloaded");
        dispose();
    }

    @Override
    protected void onDispose() {
        // Log for testing
        Gdx.app.log("Scene", "MainMenuScene disposed");

        // =========== dispose ===============
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        Gdx.input.setInputProcessor(null);
    }
}
