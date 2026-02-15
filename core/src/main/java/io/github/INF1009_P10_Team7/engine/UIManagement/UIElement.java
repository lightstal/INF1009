package io.github.INF1009_P10_Team7.engine.UIManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

// Create ui components
public class UIElement {
    
    // encapsulated variable
    private final Skin skin;
    private final boolean debugLogging;

    // constructor
    public UIElement(Skin skin, boolean debugLogging) {
        this.skin = skin;
        this.debugLogging = debugLogging;
    }

    // create textbutton for text inside button
    public TextButton createButton(String text, float width, float height, iUIElement action) {
        if (text == null) {
            throw new IllegalArgumentException("Button text cannot be null");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Button dimensions must be positive");
        }
        
        // add skin side the button
        TextButton button = new TextButton(text, skin, "default");
        button.setSize(width, height);

        // listener for onclick
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

  
    // Creates a key binding button.
    public KeyBindingButton createKeyBindingButton(
            String actionName,
            float width,
            float height,
            IInputController inputController) {
        
        // Input validation
        if (actionName == null || actionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Action name cannot be null or empty");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Button dimensions must be positive");
        }
        if (inputController == null) {
            throw new IllegalArgumentException("InputController cannot be null");
        }
        
        // Create specialized key binding button
        KeyBindingButton button = new KeyBindingButton(
            actionName, 
            skin, 
            inputController
        );
        
        // Configure size
        button.setSize(width, height);
        
        // Debug logging
        if (debugLogging) {
            Gdx.app.log("UIElement", 
                String.format("Created key binding button for action '%s' (%dx%d)", 
                    actionName, (int)width, (int)height));
        }
        
        return button;
    }
    
    
    // Creates a label-button pair for key bindings.
    public TextButton[] createKeyBindingPair(
            String actionName,
            float buttonWidth,
            float buttonHeight,
            IInputController inputController) {
        
        // Create non-clickable label
        TextButton label = new TextButton(actionName + ":", skin, "default");
        label.setDisabled(true);  // Make it non-interactive
        
        // Create binding button
        KeyBindingButton button = createKeyBindingButton(
            actionName, buttonWidth, buttonHeight, inputController);
        
        return new TextButton[] { label, button };
    }
    
    
    // Creates multiple key binding buttons at once.
    public KeyBindingButton[] createKeyBindingButtons(
            String[] actionNames,
            float width,
            float height,
            IInputController inputController) {
        
        if (actionNames == null || actionNames.length == 0) {
            throw new IllegalArgumentException("Action names array cannot be null or empty");
        }
        
        KeyBindingButton[] buttons = new KeyBindingButton[actionNames.length];
        
        for (int i = 0; i < actionNames.length; i++) {
            buttons[i] = createKeyBindingButton(
                actionNames[i], width, height, inputController);
        }
        
        if (debugLogging) {
            Gdx.app.log("UIElement", 
                "Created " + buttons.length + " key binding buttons");
        }
        
        return buttons;
    }
}