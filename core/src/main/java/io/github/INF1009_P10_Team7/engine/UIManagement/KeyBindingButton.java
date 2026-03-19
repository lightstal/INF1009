package io.github.INF1009_P10_Team7.engine.UIManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

public class KeyBindingButton extends TextButton {

    
    private String actionName;
    private IInputController controller;
    private boolean listening;

    /**
     * <p>
     * Constructs for KeyBindingButton
     * </p>
     *
     * @param action Get action name ("UP" , "DOWN")
     * @param skin For UI Skin
     * @param ctrl Input controller for binding
     */
    public KeyBindingButton(String action, Skin skin, IInputController ctrl) {
        super(action, skin);
        this.actionName = action;
        this.controller = ctrl;
        this.listening = false;

        // Concat action with text by correct key
        String currentKey = controller.getKeyName(actionName);
        setText(actionName + ": " + (currentKey == null ? "?" : currentKey));

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Prevention of multiple clicking
                if (listening) return;

                listening = true;
                setText(actionName + ":Press Key");

                // Listening for the next key
                controller.listenForNextKey(k -> {
                    controller.bindKey(actionName, k);
                    
                    // Refresh the button for next key
                    String newKey = controller.getKeyName(actionName);
                    setText(actionName + ": " + (newKey == null ? "?" : newKey));
                    
                    listening = false;

                    // To return back to the correct state
                    if (getStage() != null) {
                        Gdx.input.setInputProcessor(getStage());
                    }
                });
            }
        });
    }
}