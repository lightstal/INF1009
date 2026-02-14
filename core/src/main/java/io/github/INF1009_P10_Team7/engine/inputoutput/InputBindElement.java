package io.github.INF1009_P10_Team7.engine.inputoutput;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class InputBindElement {
    private final String actionName;
    private final Rectangle bounds;

    public InputBindElement(String actionName, float x, float y, float w, float h) {
        this.actionName = actionName;
        this.bounds = new Rectangle(x, y, w, h);
    }

    public String getActionName() {
        return actionName;
    }

    // checks if the mouse click is within this button's area
    public boolean isClicked(Vector2 worldCoords) {
        return bounds.contains(worldCoords.x, worldCoords.y);
    }

    // render shape for button
    public void drawShape(ShapeRenderer shape, boolean isWaiting) {
        if (isWaiting) {
            shape.setColor(0.8f, 0.8f, 0.2f, 1f);
        } else {
            shape.setColor(0.3f, 0.3f, 0.35f, 1f); 
        }
        shape.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // render text in for button
    public void drawText(SpriteBatch batch, BitmapFont font, InputController input, boolean isWaiting) {
        // labeling
        font.setColor(0.8f, 0.8f, 0.8f, 1f);
        font.draw(batch, actionName + ":", bounds.x - 75, bounds.y + 24);

        // draw the current key and change key
        String keyText = isWaiting ? "..." : input.getKeyName(actionName);
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, keyText, bounds.x + 10, bounds.y + 24);
    }
}