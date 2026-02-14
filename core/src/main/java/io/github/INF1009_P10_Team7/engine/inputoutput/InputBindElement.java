package io.github.INF1009_P10_Team7.engine.inputoutput;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a UI element for key binding configuration.
 */
public class InputBindElement {
    
    // Background colors
    private static final Color NORMAL_BACKGROUND_COLOR = new Color(0.3f, 0.3f, 0.35f, 1f);
    private static final Color WAITING_BACKGROUND_COLOR = new Color(0.8f, 0.8f, 0.2f, 1f);
    
    // Text colors
    private static final Color LABEL_TEXT_COLOR = new Color(0.8f, 0.8f, 0.8f, 1f);
    private static final Color KEY_TEXT_COLOR = new Color(1f, 1f, 1f, 1f);
    
    // Layout offsets
    private static final float LABEL_OFFSET_X = -75f;
    private static final float TEXT_PADDING_X = 10f;
    private static final float TEXT_OFFSET_Y = 24f;
    
    // Waiting indicator text
    private static final String WAITING_INDICATOR = "...";
    
    // Encapsulate data
    private final String actionName;
    private final Rectangle bounds;

    // Creates a new input binding UI element.
    public InputBindElement(String actionName, float x, float y, float width, float height) {
        // Validation for robustness
        if (actionName == null || actionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Action name cannot be null or empty");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive values");
        }
        
        this.actionName = actionName;
        this.bounds = new Rectangle(x, y, width, height);
    }

    // Gets the action name for this binding.
    public String getActionName() {
        return actionName;
    }

    // Gets the bounds of this UI element.
    public Rectangle getBounds() {
        return bounds;
    }

    // Checks if a world coordinate point is within this button's clickable area.
    public boolean isClicked(Vector2 worldCoords) {
        if (worldCoords == null) {
            return false;
        }
        return bounds.contains(worldCoords.x, worldCoords.y);
    }

    //Renders the background shape for this input binding element.
    public void drawShape(ShapeRenderer shape, boolean isWaiting) {
        if (shape == null) {
            throw new IllegalArgumentException("ShapeRenderer cannot be null");
        }
        
        // Select appropriate background color based on state
        Color backgroundColor = isWaiting ? WAITING_BACKGROUND_COLOR : NORMAL_BACKGROUND_COLOR;
        shape.setColor(backgroundColor);
        
        // Draw the background rectangle
        shape.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // Renders the text label and current key binding for this element.
    public void drawText(SpriteBatch batch, BitmapFont font, IInputController input, boolean isWaiting) {
        if (batch == null || font == null || input == null) {
            throw new IllegalArgumentException("Batch, font, and input controller cannot be null");
        }
        
        // Render action label (e.g., "LEFT:")
        renderActionLabel(batch, font);
        
        // Render current key binding or waiting indicator
        renderKeyText(batch, font, input, isWaiting);
    }

    // Renders the action label (e.g., "LEFT:", "SHOOT:").
    private void renderActionLabel(SpriteBatch batch, BitmapFont font) {
        font.setColor(LABEL_TEXT_COLOR);
        
        float labelX = bounds.x + LABEL_OFFSET_X;
        float labelY = bounds.y + TEXT_OFFSET_Y;
        
        font.draw(batch, actionName + ":", labelX, labelY);
    }

    // Renders the current key binding or waiting indicator.
    private void renderKeyText(SpriteBatch batch, BitmapFont font, 
                               IInputController input, boolean isWaiting) {
        font.setColor(KEY_TEXT_COLOR);
        
        // Determine text to display
        String displayText = isWaiting ? WAITING_INDICATOR : input.getKeyName(actionName);
        
        float textX = bounds.x + TEXT_PADDING_X;
        float textY = bounds.y + TEXT_OFFSET_Y;
        
        font.draw(batch, displayText, textX, textY);
    }

    // Checks if a point is within this element's bounds (alternative to isClicked).
    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    // Gets the center X coordinate of this element.
    public float getCenterX() {
        return bounds.x + bounds.width / 2f;
    }

    // Gets the center Y coordinate of this element.
    public float getCenterY() {
        return bounds.y + bounds.height / 2f;
    }

    @Override
    public String toString() {
        return String.format("InputBindElement[action=%s, bounds=(%.1f,%.1f,%.1f,%.1f)]",
                actionName, bounds.x, bounds.y, bounds.width, bounds.height);
    }
}