package io.github.INF1009_P10_Team7.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.INF1009_P10_Team7.engine.events.EventType;
import io.github.INF1009_P10_Team7.engine.events.GameEvent;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneManager;

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

    public MainMenuScene(SceneManager sceneManager) {
        super(sceneManager);
    }

    @Override
    protected void onLoad() {
        // Log for testing (marker can see lifecycle)
        Gdx.app.log("Scene", "MainMenuScene loaded");
        
        GameEvent musicEvent = new GameEvent(EventType.PLAY_MUSIC).add("file_path", "Music_Menu.mp3");
        context.getEventBus().publish(musicEvent);
        Gdx.app.log("Audio Output", "MainMenu Music loaded");


        // =========== Created start button ===============
        // =========== Button created using code composer, might need to design again ============
        stage = new Stage(new ScreenViewport());
        
        try {
		    skin = new Skin(Gdx.files.internal("buttons/name2d.json"));
		
		    // =================== Button created ===================
		    TextButton startButton = new TextButton("START GAME", skin, "default");
		    // =================== Size & Position ==================
		    startButton.setSize(200, 60);
		    startButton.setPosition(
		            (Gdx.graphics.getWidth() - startButton.getWidth()) / 2, // Center X
		            Gdx.graphics.getHeight() / 2 // Center Y
		    );
		
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
