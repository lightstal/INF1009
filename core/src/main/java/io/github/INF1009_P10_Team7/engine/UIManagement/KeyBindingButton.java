package io.github.INF1009_P10_Team7.engine.UIManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

public class KeyBindingButton extends TextButton {

    private final String actionName;
    private final String displayName;
    private final IInputController controller;
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
    public KeyBindingButton(String action, String displayName, Skin skin, IInputController ctrl) {
        super("", skin);
        this.actionName = action;
        this.displayName = displayName;
        this.controller = ctrl;
        this.listening = false;

        getLabel().setFontScale(0.30f);
        getLabel().setWrap(false);
        pad(4f, 8f, 4f, 8f);

        refreshLabel();

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listening) return;

                listening = true;
                setText(displayName + ": ...");

                controller.listenForNextKey(k -> {
                    if (k >= 300) {
                        controller.bindMouseButton(actionName, k - 300);
                    } else {
                        controller.bindKey(actionName, k);
                    }

                    refreshLabel();
                    listening = false;

                    if (getStage() != null) {
                        Gdx.input.setInputProcessor(getStage());
                    }
                });
            }
        });
    }

    private void refreshLabel() {
        String currentKey = controller.getKeyName(actionName);
        setText(displayName + ": " + shortKey(currentKey == null ? "?" : currentKey));
    }


    private String shortKey(String key) {
        switch (key) {
            case "Escape": return "ESC";
            case "Enter": return "ENTER";
            case "Backspace": return "BKSP";
            case "Space": return "SPACE";
            case "Left": return "LEFT";
            case "Right": return "RIGHT";
            case "Up": return "UP";
            case "Down": return "DOWN";
            case "Control Left": return "CTRL";
            case "Control Right": return "CTRL";
            default: return key;
        }
    }
}