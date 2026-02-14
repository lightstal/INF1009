package io.github.INF1009_P10_Team7.engine.inputoutput;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a UI element for key binding configuration.
 * 
 * SOLID Principles Applied:
 * - SRP: Manages input binding UI element (data + its specific rendering)
 * - OCP: Can be extended for different visual styles
 * - Encapsulation: Private fields with controlled access, constants extracted
 * 
 * IMPROVEMENTS FROM ORIGINAL:
 * 1. Extracted all magic numbers to named constants
 * 2. Added proper validation in constructor
 * 3. Improved encapsulation with Color objects
 * 4. Better documentation
 * 5. More defensive programming
 */
public class InputBindElement {
    
    // ============================================================
    // CONSTANTS - Extracted magic numbers for better maintainability
    // ============================================================
    
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
    
    // ============================================================
    // FIELDS - Encapsulated data
    // ============================================================
    
    private final String actionName;
    private final Rectangle bounds;

    /**
     * Creates a new input binding UI element.
     * 
     * @param actionName The name of the action (e.g., "LEFT", "RIGHT", "SHOOT")
     * @param x X position of the UI element
     * @param y Y position of the UI element
     * @param width Width of the UI element
     * @param height Height of the UI element
     * @throws IllegalArgumentException if actionName is null/empty or dimensions are invalid
     */
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

    /**
     * Gets the action name for this binding.
     * 
     * @return The action name (e.g., "LEFT", "RIGHT")
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Gets the bounds of this UI element.
     * 
     * @return The bounding rectangle
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Checks if a world coordinate point is within this button's clickable area.
     * 
     * @param worldCoords The world coordinates to test (from viewport.unproject)
     * @return true if the point is within bounds, false otherwise
     */
    public boolean isClicked(Vector2 worldCoords) {
        if (worldCoords == null) {
            return false;
        }
        return bounds.contains(worldCoords.x, worldCoords.y);
    }

    /**
     * Renders the background shape for this input binding element.
     * 
     * Following OCP: This method can be overridden in subclasses for custom visuals.
     * 
     * @param shape The shape renderer to use
     * @param isWaiting true if waiting for key input (shows different color)
     * @throws IllegalArgumentException if shape is null
     */
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

    /**
     * Renders the text label and current key binding for this element.
     * 
     * Following OCP: This method can be overridden in subclasses for custom text rendering.
     * 
     * @param batch The sprite batch for text rendering
     * @param font The font to use for rendering
     * @param input The input controller to query current key bindings
     * @param isWaiting true if waiting for key input (shows "..." instead of key name)
     * @throws IllegalArgumentException if any parameter is null
     */
    public void drawText(SpriteBatch batch, BitmapFont font, IInputController input, boolean isWaiting) {
        if (batch == null || font == null || input == null) {
            throw new IllegalArgumentException("Batch, font, and input controller cannot be null");
        }
        
        // Render action label (e.g., "LEFT:")
        renderActionLabel(batch, font);
        
        // Render current key binding or waiting indicator
        renderKeyText(batch, font, input, isWaiting);
    }

    /**
     * Renders the action label (e.g., "LEFT:", "SHOOT:").
     * 
     * Extracted to separate method following SRP at method level.
     * 
     * @param batch The sprite batch
     * @param font The font
     */
    private void renderActionLabel(SpriteBatch batch, BitmapFont font) {
        font.setColor(LABEL_TEXT_COLOR);
        
        float labelX = bounds.x + LABEL_OFFSET_X;
        float labelY = bounds.y + TEXT_OFFSET_Y;
        
        font.draw(batch, actionName + ":", labelX, labelY);
    }

    /**
     * Renders the current key binding or waiting indicator.
     * 
     * Extracted to separate method following SRP at method level.
     * 
     * @param batch The sprite batch
     * @param font The font
     * @param input The input controller
     * @param isWaiting Whether waiting for input
     */
    private void renderKeyText(SpriteBatch batch, BitmapFont font, 
                               IInputController input, boolean isWaiting) {
        font.setColor(KEY_TEXT_COLOR);
        
        // Determine text to display
        String displayText = isWaiting ? WAITING_INDICATOR : input.getKeyName(actionName);
        
        float textX = bounds.x + TEXT_PADDING_X;
        float textY = bounds.y + TEXT_OFFSET_Y;
        
        font.draw(batch, displayText, textX, textY);
    }

    /**
     * Checks if a point is within this element's bounds (alternative to isClicked).
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if point is within bounds
     */
    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    /**
     * Gets the center X coordinate of this element.
     * 
     * @return Center X position
     */
    public float getCenterX() {
        return bounds.x + bounds.width / 2f;
    }

    /**
     * Gets the center Y coordinate of this element.
     * 
     * @return Center Y position
     */
    public float getCenterY() {
        return bounds.y + bounds.height / 2f;
    }

    @Override
    public String toString() {
        return String.format("InputBindElement[action=%s, bounds=(%.1f,%.1f,%.1f,%.1f)]",
                actionName, bounds.x, bounds.y, bounds.width, bounds.height);
    }
}