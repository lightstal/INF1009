package io.github.INF1009_P10_Team7.engine.UIManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

public class UIElement {

    /**
     * <p>
     * Skin for fonts, textures for the button
     * debug to log the console
     * </p>
     */
    private final Skin skin;
    private final boolean debug;

    /**
     * <p>
     * Constructs for UIElement
     * </p>
     *
     * @param skin Use skin for button style and font
     * @param debug Use debug for logging in console
     */
    public UIElement(Skin skin, boolean debug) {
        this.skin = skin;
        this.debug = debug;
    }

    /**
     * <p>
     * Create button using TextButton with onclick listener
     * </p>
     *
     * @param text Get text for labelling
     * @param width Width for button
     * @param height Height for button
     * @param callback For click listener
     * @return Fully configured Textbutton
     */
    public TextButton createButton(String text, float width, float height, final iUIElement callback) {
        TextButton btn = new TextButton(text, skin);
        btn.setSize(width, height);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (debug)
                    Gdx.app.log("UI", "Clicked: " + text);

                if (callback != null) {
                    callback.execute();
                }
            }
        });

        return btn;
    }

    /**
     * <p>
     * Button for keybinding
     * </p>
     *
     * @param action Get action name like ("UP", "DOWN")
     * @param width Width for button
     * @param height Height for button
     * @param input IInputController to get binding
     * @return Created KeyBindingButton
     */
    public KeyBindingButton createKeyBindingButton(String action, float width, float height, IInputController input) {
        KeyBindingButton btn = new KeyBindingButton(action, skin, input);
        btn.setSize(width, height);
        return btn;
    }
}