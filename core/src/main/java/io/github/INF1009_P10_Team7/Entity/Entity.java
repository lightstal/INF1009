package io.github.INF1009_P10_Team7.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.INF1009_P10_Team7.Collision.iCollidable;
import io.github.INF1009_P10_Team7.InputOutput.iInputController;

public abstract class Entity implements iCollidable {
    
    protected Vector2 position;
    protected Vector2 velocity;
    protected Rectangle bounds;
    protected boolean isExpired;

    public Entity(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.bounds = new Rectangle(x - width/2f, y - height/2f, width, height);
        this.isExpired = false;
    }
    
    public Vector2 getVelocity() {
        return velocity;
    }

    public abstract void update(float deltaTime, iInputController input);

    // --- HYBRID RENDERING ---
    
    public void render(SpriteBatch batch) {}
    public void render(ShapeRenderer shape) {}
    
    // Common Methods
    public void move(float x, float y) {
        this.position.add(x, y);
        this.bounds.setPosition(position.x - bounds.width / 2f, position.y - bounds.height / 2f);
    }
    
    public void destroy() { isExpired = true; }
    public boolean isExpired() { return isExpired; }
    public Vector2 getPosition() { return position; }
    
    public Rectangle getBounds() { return bounds; }    
    public void onCollision(iCollidable other) {}
}