package io.github.INF1009_P10_Team7.engine.inputoutput;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class UIElement {
    // private static Skin skin;

    private final Skin skin;
    private final boolean debugLogging;

    public UIElement(Skin skin, boolean debugLogging) {
        this.skin = skin;
        this.debugLogging = debugLogging;
    }

    public TextButton createButton(String text, float width, float height, iUIElement action) {
        if (text == null) {
            throw new IllegalArgumentException("Button text cannot be null");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Button dimensions must be positive");
        }
        
        TextButton button = new TextButton(text, skin, "default");
        button.setSize(width, height);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Log only if debug is enabled
                if (debugLogging) {
                    Gdx.app.log("UI", text + " Button Clicked");
                }

                // Execute action if provided
                if (action != null) {
                    action.execute();
                }
            }
        });

        return button;
    }
}