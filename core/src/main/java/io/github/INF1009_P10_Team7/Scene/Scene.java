package io.github.INF1009_P10_Team7.Scene;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.Collision.iCollisionSystem;
import io.github.INF1009_P10_Team7.Entity.iEntitySystem;
import io.github.INF1009_P10_Team7.InputOutput.iAudioController;
import io.github.INF1009_P10_Team7.InputOutput.iInputController;
import io.github.INF1009_P10_Team7.Movement.iMovementSystem;

public abstract class Scene {
    protected static final float V_WIDTH = 640;
    protected static final float V_HEIGHT = 480;

    protected OrthographicCamera camera;
    protected Viewport viewport;
	
	protected iInputController input;
    protected iAudioController audio;
    protected iSceneNavigator navigator;
    protected iEntitySystem entitySystem;
    protected iMovementSystem movementSystem;
    protected iCollisionSystem collisionSystem;

    public void init(iInputController input, iAudioController audio, iSceneNavigator navigator) {
        this.input = input;
        this.audio = audio;
        this.navigator = navigator;
    }
    
    public void init(iInputController input, iAudioController audio, iSceneNavigator navigator, iEntitySystem entity, iMovementSystem movement, iCollisionSystem collision) {
		this.init(input, audio, navigator);
		this.entitySystem = entity;
		this.movementSystem = movement;
		this.collisionSystem = collision;
    }

    public abstract void create();
    public abstract void update(float dt);
    public abstract void render(SpriteBatch batch, ShapeRenderer shape);
    public abstract void dispose();
    
    public void pause() {}
    public void resume() {}
    
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }
}