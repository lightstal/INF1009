package io.github.INF1009_P10_Team7.engine.UIManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

/**
 * KeyBindingButton — a UI button that displays and allows rebinding of a
 * named input action.
 *
 * <p>Shows the current key name for an action and, when clicked, listens for
 * the next key or mouse button pressed to update the binding via
 * {@link io.github.INF1009_P10_Team7.engine.inputoutput.IInputController#listenForNextKey}.
 * Used by {@link io.github.INF1009_P10_Team7.simulation.SettingsScene}.</p>
 */
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
     * @param action      The logical action name (e.g., "UP", "JUMP").
     * @param displayName The text displayed on the button (e.g., "Move Up").
     * @param skin        The UI Skin for styling.
     * @param ctrl        The central Input Controller.
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

                controller.listenForNextKey((deviceID, localCode) -> {
                	controller.bindInput(actionName, deviceID, localCode);

                    refreshLabel();
                    listening = false;

                    if (getStage() != null) {
                        Gdx.input.setInputProcessor(getStage());
                    }
                });
            }
        });
    }

    /**
     * Updates the button text to reflect the currently bound key.
     */
    private void refreshLabel() {
        String currentKey = controller.getKeyName(actionName);
        setText(displayName + ": " + shortKey(currentKey == null ? "?" : currentKey));
    }


    /**
     * Shortens common key names so they fit nicely on the UI buttons.
     */
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