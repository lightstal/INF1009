package io.github.INF1009_P10_Team7.engine.UIManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

/**
 * Specialized button for key binding configuration.
 */
public class KeyBindingButton extends TextButton {

    // Encapsulated state
    private final String actionName;
    private final IInputController inputController;
    private boolean isWaiting;

    // Visual constants for different states
    private static final String NORMAL_STYLE = "default";
    private static final String WAITING_TEXT_FORMAT = "%s: [ PRESS ANY KEY ]";
    private static final String NORMAL_TEXT_FORMAT = "%s: %s";

  
    // Creates a key binding button that shows "ACTION: KEY" format.  
    public KeyBindingButton(String actionName, Skin skin, IInputController inputController) {
        super(getFormattedDisplay(actionName, inputController, false), skin, NORMAL_STYLE);

        // Input validation (defensive programming)
        if (actionName == null || actionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Action name cannot be null or empty");
        }
        if (inputController == null) {
            throw new IllegalArgumentException("InputController cannot be null");
        }

        this.actionName = actionName;
        this.inputController = inputController;
        this.isWaiting = false;

        setupClickBehavior();
    }

    
    // Configures the button's click behavior for rebinding. 
    private void setupClickBehavior() {
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Only start rebinding if not already waiting
                if (!isWaiting) {
                    startRebinding();
                }
            }
        });
    }


    // Initiates the key rebinding process.
    private void startRebinding() {
        // Update internal state
        isWaiting = true;

        // Update visual feedback to show "ACTION: [ PRESS ANY KEY ]"
        setText(String.format(WAITING_TEXT_FORMAT, actionName));

        Gdx.app.log("KeyBindingButton",
                "Waiting for new key binding for action: " + actionName);

        // Save current Stage reference
        final Stage currentStage = getStage();

        // Listen for next key press (Observer pattern - callback)
        inputController.listenForNextKey(keycode -> {
            onKeyReceived(keycode);

            // CRITICAL FIX: Restore Stage as input processor
            if (currentStage != null) {
                Gdx.input.setInputProcessor(currentStage);
                Gdx.app.log("KeyBindingButton", "Input processor restored to Stage");
            }
        });
    }

  
    // Callback invoked when a new key is pressed during rebinding.
    private void onKeyReceived(int keycode) {
        // Bind the new key (delegated to IInputController - Strategy pattern)
        inputController.bindKey(actionName, keycode);

        Gdx.app.log("KeyBindingButton",
                String.format("Rebound '%s' to keycode %d (%s)",
                        actionName, keycode, inputController.getKeyName(actionName)));

        // Update display with new key name
        updateDisplay();

        // Reset waiting state
        isWaiting = false;
    }

    
    // Updates the button text to show "ACTION: KEY" format.
    public void updateDisplay() {
        String keyName = inputController.getKeyName(actionName);
        String displayText = String.format(NORMAL_TEXT_FORMAT, actionName, 
                                          keyName != null ? keyName : "???");
        setText(displayText);

        // Reset to normal style
        setStyle(getSkin().get(NORMAL_STYLE, TextButtonStyle.class));
    }


    // Gets the formatted display text for initial creation.
    private static String getFormattedDisplay(String actionName, IInputController input, boolean waiting) {
        if (waiting) {
            return String.format(WAITING_TEXT_FORMAT, actionName);
        }
        
        try {
            String keyName = input.getKeyName(actionName);
            return String.format(NORMAL_TEXT_FORMAT, actionName, 
                               keyName != null ? keyName : "Unbound");
        } catch (Exception e) {
            Gdx.app.error("KeyBindingButton",
                    "Failed to get key name for action: " + actionName, e);
            return String.format(NORMAL_TEXT_FORMAT, actionName, "???");
        }
    }


    // Gets the action name this button controls.
    public String getActionName() {
        return actionName;
    }


    // Checks if this button is currently waiting for key input.
    public boolean isWaiting() {
        return isWaiting;
    }

 
    // Forces a refresh of the displayed key name.
    public void refresh() {
        if (!isWaiting) {
            updateDisplay();
        }
    }

    @Override
    public String toString() {
        return String.format("KeyBindingButton[action=%s, waiting=%s, currentKey=%s]",
                actionName, isWaiting, getText());
    }
}