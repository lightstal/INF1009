package io.github.INF1009_P10_Team7.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainMenuScene extends Scene {
    private BitmapFont font;
    private GlyphLayout layout;
    private Rectangle playBounds;
    private Rectangle settingsBounds;
    private Vector3 touchPoint;
    private boolean isHoveringPlay = false;
    private boolean isHoveringSettings = false;

    public MainMenuScene() {
    }

    @Override
    public void create() {
    	Gdx.app.log("MainMenuScene", "Initializing Main Menu...");
    	
    	camera = new OrthographicCamera();
        viewport = new FitViewport(V_WIDTH, V_HEIGHT, camera);
        
        font = new BitmapFont();
        font.getData().setScale(2.0f);
        layout = new GlyphLayout();
        touchPoint = new Vector3();

        float centerX = V_WIDTH / 2f;
        float centerY = V_HEIGHT / 2f;
        
        playBounds = new Rectangle(centerX - 100, centerY + 20, 200, 60);
        settingsBounds = new Rectangle(centerX - 100, centerY - 80, 200, 60);
        
        audio.playMusic("Music_Menu.mp3");
        
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.app.log("MainMenuScene", "Main Menu Initialized Successfully");
    }

    @Override
    public void update(float dt) {
        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        
        isHoveringPlay = playBounds.contains(touchPoint.x, touchPoint.y);
        isHoveringSettings = settingsBounds.contains(touchPoint.x, touchPoint.y);

        if (Gdx.input.justTouched()) {
        	if (isHoveringPlay) {
                Gdx.app.log("MainMenuScene", "PLAY button clicked. Transitioning to GameScene...");
                navigator.setScene(new GameScene());
            } else if (isHoveringSettings) {
                Gdx.app.log("MainMenuScene", "SETTINGS button clicked. Pushing SettingScene...");
                navigator.pushScene(new SettingScene());
            }
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shape) {
    	ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(isHoveringPlay ? Color.LIME : Color.FOREST);
        shape.rect(playBounds.x, playBounds.y, playBounds.width, playBounds.height);
        shape.setColor(isHoveringSettings ? Color.GRAY : Color.DARK_GRAY);
        shape.rect(settingsBounds.x, settingsBounds.y, settingsBounds.width, settingsBounds.height);
        shape.end();

        batch.begin();
        layout.setText(font, "PLAY");
        font.draw(batch, layout, playBounds.x + (playBounds.width - layout.width) / 2, playBounds.y + (playBounds.height + layout.height) / 2);
        layout.setText(font, "SETTINGS");
        font.draw(batch, layout, settingsBounds.x + (settingsBounds.width - layout.width) / 2, settingsBounds.y + (settingsBounds.height + layout.height) / 2);
        batch.end();
    }
    
    @Override
    public void dispose() {
    	Gdx.app.log("MainMenuScene", "Disposing MainMenuScene Resources...");
        font.dispose();
    }
}