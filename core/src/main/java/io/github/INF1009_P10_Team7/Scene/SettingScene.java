package io.github.INF1009_P10_Team7.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class SettingScene extends Scene {
    private BitmapFont font;
    private GlyphLayout layout;
    
    private Rectangle backBounds;
    private Rectangle sliderBounds;
    private Rectangle upBounds, downBounds, leftBounds, rightBounds;
    
    private Vector3 touchPoint;
    private boolean isHoveringBack = false;
    private String waitingForAction = null;

    public SettingScene() {
    }
    
    @Override
    public void create() {
        // Initialize Inherited Camera and Viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(V_WIDTH, V_HEIGHT, camera);
        
        font = new BitmapFont();
        font.getData().setScale(2.0f);
        layout = new GlyphLayout();
        touchPoint = new Vector3();
        
        // Define Layout using the locked Virtual Resolution (640x480)
        float centerX = V_WIDTH / 2f;
        float centerY = V_HEIGHT / 2f;

        backBounds = new Rectangle(50, 50, 150, 50);
        
        // Center the slider
        sliderBounds = new Rectangle(centerX - 150, centerY + 80, 300, 30);
        
        float startY = centerY + 10;
        float verticalGap = 60;
        float btnWidth = 260;
        float btnHeight = 45;
        float horizontalGap = 20;

        // Automatically calculate perfect centering
        float leftColX = centerX - btnWidth - (horizontalGap / 2f);
        float rightColX = centerX + (horizontalGap / 2f);

        // Top Row: UP and LEFT
        upBounds    = new Rectangle(leftColX, startY, btnWidth, btnHeight);
        leftBounds  = new Rectangle(rightColX, startY, btnWidth, btnHeight);
        
        // Bottom Row: DOWN and RIGHT
        downBounds  = new Rectangle(leftColX, startY - verticalGap, btnWidth, btnHeight);
        rightBounds = new Rectangle(rightColX, startY - verticalGap, btnWidth, btnHeight);
        
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void update(float dt) {
        
        // --- REBINDING LISTENER STATE ---
        if (waitingForAction != null) {
            for (int i = 0; i < 256; i++) {
                if (Gdx.input.isKeyJustPressed(i)) {
                    if (i != Input.Keys.ESCAPE) {
                        input.bindKey(waitingForAction, i);
                    }
                    waitingForAction = null;
                    break;
                }
            }
            return;
        }
        
        viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        
        isHoveringBack = backBounds.contains(touchPoint.x, touchPoint.y);

        // Click Handling
        if (Gdx.input.justTouched()) {
            if (isHoveringBack) {
            	navigator.popScene();
            	return;
            }
            
            if (upBounds.contains(touchPoint.x, touchPoint.y)) waitingForAction = "UP";
            if (downBounds.contains(touchPoint.x, touchPoint.y)) waitingForAction = "DOWN";
            if (leftBounds.contains(touchPoint.x, touchPoint.y)) waitingForAction = "LEFT";
            if (rightBounds.contains(touchPoint.x, touchPoint.y)) waitingForAction = "RIGHT";
        }
        
        // Slider Drag Handling
        if (Gdx.input.isTouched()) {
            if (sliderBounds.contains(touchPoint.x, touchPoint.y)) {
                float relativeX = touchPoint.x - sliderBounds.x;
                float newVolume = relativeX / sliderBounds.width;
                audio.setMusicVolume(newVolume);
            }
        }

        // Keyboard Volume Handling
        if (input.isActionJustPressed("VOLUME_UP")) {
            float currentVol = audio.getMusicVolume();
            float newVol = Math.round((currentVol + 0.1f) * 10.0f) / 10.0f;
            audio.setMusicVolume(Math.min(newVol, 1.0f));
        }

        if (input.isActionJustPressed("VOLUME_DOWN")) {
            float currentVol = audio.getMusicVolume();
            float newVol = Math.round((currentVol - 0.1f) * 10.0f) / 10.0f;
            audio.setMusicVolume(Math.max(newVol, 0.0f)); // Fixed from Math.min!
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shape) {
        ScreenUtils.clear(0.2f, 0.1f, 0.1f, 1);

        camera.update();
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        
        // Back Button
        shape.setColor(isHoveringBack ? Color.LIGHT_GRAY : Color.FIREBRICK);
        shape.rect(backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        
        // Slider
        shape.setColor(Color.DARK_GRAY);
        shape.rect(sliderBounds.x, sliderBounds.y, sliderBounds.width, sliderBounds.height);
        float currentVol = audio.getMusicVolume();
        shape.setColor(currentVol > 0.75f ? Color.LIME : Color.CYAN); 
        shape.rect(sliderBounds.x, sliderBounds.y, sliderBounds.width * currentVol, sliderBounds.height);
        
        // Bind Buttons
        drawBindRect(shape, upBounds, "UP");
        drawBindRect(shape, downBounds, "DOWN");
        drawBindRect(shape, leftBounds, "LEFT");
        drawBindRect(shape, rightBounds, "RIGHT");
        
        shape.end();

        batch.begin();
        // Title
        font.getData().setScale(3.0f);
        layout.setText(font, "SETTINGS SCREEN");
        font.draw(batch, layout, sliderBounds.x + (sliderBounds.width - layout.width) / 2, sliderBounds.y + sliderBounds.height + 120);
        
        // Slider Label 
        font.getData().setScale(1.5f);
        String volText = "VOLUME: " + Math.round(currentVol * 100) + "%";
        layout.setText(font, volText);
        font.draw(batch, layout, sliderBounds.x + (sliderBounds.width - layout.width) / 2, sliderBounds.y + sliderBounds.height + 40);

        // Back Button Text
        layout.setText(font, "BACK");
        font.draw(batch, layout, backBounds.x + (backBounds.width - layout.width)/2, backBounds.y + (backBounds.height + layout.height)/2);
        
        // Bind Text
        drawBindText(batch, upBounds, "UP");
        drawBindText(batch, downBounds, "DOWN");
        drawBindText(batch, leftBounds, "LEFT");
        drawBindText(batch, rightBounds, "RIGHT");
        batch.end();
    }
    
    private void drawBindRect(ShapeRenderer shape, Rectangle bounds, String action) {
        if (action.equals(waitingForAction)) shape.setColor(Color.GOLD);
        else shape.setColor(Color.GRAY);
        shape.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void drawBindText(SpriteBatch batch, Rectangle bounds, String action) {
    	font.getData().setScale(1.0f);
    	
        String display;
        if (action.equals(waitingForAction)) {
            display = action + ": [ PRESS ANY KEY ]";
        } else {
            display = action + ": " + Input.Keys.toString(input.getKeyCode(action));
        }
        
        layout.setText(font, display);
        font.draw(batch, layout, bounds.x + (bounds.width - layout.width)/2, bounds.y + (bounds.height + layout.height)/2);
    }
    
    @Override
    public void dispose() {
        font.dispose();
    }
}