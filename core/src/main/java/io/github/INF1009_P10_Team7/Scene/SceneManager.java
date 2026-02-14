package io.github.INF1009_P10_Team7.Scene;

import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.INF1009_P10_Team7.Collision.iCollisionSystem;
import io.github.INF1009_P10_Team7.Entity.iEntitySystem;
import io.github.INF1009_P10_Team7.InputOutput.iAudioController;
import io.github.INF1009_P10_Team7.InputOutput.iInputController;
import io.github.INF1009_P10_Team7.Movement.iMovementSystem;

public class SceneManager implements iSceneNavigator {

    // Use a Stack to manage history (Game -> Settings)
    private final Stack<Scene> scenes = new Stack<>();
    
    private iInputController input;
    private iAudioController audio;
    private iEntitySystem entitySystem;
    private iMovementSystem movementSystem;
    private iCollisionSystem collisionSystem;
    
    public void setDependencies(iInputController input, iAudioController audio, iEntitySystem entitiy, iMovementSystem movement, iCollisionSystem collision) {
        this.input = input;
        this.audio = audio;
        this.entitySystem = entitiy;
        this.movementSystem = movement;
        this.collisionSystem = collision;

        Gdx.app.log("SceneManager", "InputController, AudioController, EntitySystem, MovementSystem, CollisionSystem Dependencies added.");
    }

    @Override
    public void setScene(Scene scene) {
        while (!scenes.isEmpty()) {
            scenes.pop().dispose();
        }
        pushScene(scene); 
    }

    @Override
    public void pushScene(Scene scene) {
        if (!scenes.isEmpty()) {
            scenes.peek().pause(); 
        }


        if (scene instanceof GameScene) {
            scene.init(input, audio, this, entitySystem, movementSystem, collisionSystem);
        } else {
            scene.init(input, audio, this);
        }
        
        scenes.push(scene);
        scene.create();
    }

    // REMOVES the top scene (e.g., Settings -> Game)
    public void popScene() {
        if (!scenes.isEmpty()) {
            Scene oldScene = scenes.pop();
            oldScene.dispose();
        }
        if (!scenes.isEmpty()) {
            scenes.peek().resume(); // Resume the game
        }
    }

    public void update(float dt) {
        // Only update the scene on TOP of the stack
        if (!scenes.isEmpty()) {
            scenes.peek().update(dt);
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shape) {
        // Only render the scene on TOP (Change this loop if you want transparent overlays)
        if (!scenes.isEmpty()) {
            scenes.peek().render(batch, shape);
        }
    }
    
    public void resize(int width, int height) {
        if (!scenes.isEmpty()) {
            scenes.peek().resize(width, height);
        }
    }

    private void popAll() {
        while (!scenes.isEmpty()) {
            Scene s = scenes.pop();
            s.dispose();
        }
    }

    public void dispose() {
        popAll();
    }
}