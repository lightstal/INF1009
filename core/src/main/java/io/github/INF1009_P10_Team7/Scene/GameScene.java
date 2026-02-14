package io.github.INF1009_P10_Team7.Scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.INF1009_P10_Team7.Collision.CollisionType;
import io.github.INF1009_P10_Team7.Entity.SimulationEntity;
import io.github.INF1009_P10_Team7.Entity.CircleRenderer;
import io.github.INF1009_P10_Team7.Entity.DiamondRenderer;
import io.github.INF1009_P10_Team7.Entity.Entity;
import io.github.INF1009_P10_Team7.Entity.RectangleRenderer;
import io.github.INF1009_P10_Team7.Movement.AIMovement;
import io.github.INF1009_P10_Team7.Movement.FollowMovement;
import io.github.INF1009_P10_Team7.Movement.LinearMovement;
import io.github.INF1009_P10_Team7.Movement.PlayerMovement;
import io.github.INF1009_P10_Team7.Movement.iMovementBehaviour;

public class GameScene extends Scene {
	
	private final List<Entity> activeEntities = new ArrayList<>();
    private final List<Entity> pendingEntities = new ArrayList<>();
    private final Map<Entity, iMovementBehaviour> movementRegistry = new HashMap<>();
    private final Map<Entity, CollisionType> collisionRegistry = new HashMap<>();
	
    public GameScene() {
    	
    }

    @Override
    public void create() {
    	Gdx.app.log("GameScene", "Initializing GameScene...");

        // 1. Initialize inherited Camera and Viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(V_WIDTH, V_HEIGHT, camera);

        Entity player = new SimulationEntity(V_WIDTH / 2f, V_HEIGHT / 2f, 40, 40, Color.CYAN, new CircleRenderer());
        pendingEntities.add(player);
        movementRegistry.put(player, new PlayerMovement(200f, input));
        collisionRegistry.put(player, CollisionType.PASS_THROUGH);

        // A Red Rectangle
        Entity linearEnemy = new SimulationEntity(100, 100, 50, 50, Color.RED, new RectangleRenderer());
        pendingEntities.add(linearEnemy);
        movementRegistry.put(linearEnemy, new LinearMovement(1.0f, 1.0f, 150f));
        collisionRegistry.put(linearEnemy, CollisionType.BOUNCE);
        
        // A Yellow Diamond Wanderer
        Entity wanderer = new SimulationEntity(V_WIDTH / 2f + 150, V_HEIGHT / 2f, 50, 50, Color.YELLOW, new DiamondRenderer());
        pendingEntities.add(wanderer);
        movementRegistry.put(wanderer, new AIMovement(100f, 2.0f));
        collisionRegistry.put(wanderer, CollisionType.BOUNCE);
        
        Entity follower = new SimulationEntity(V_WIDTH - 100, V_HEIGHT - 100, 45, 45, Color.PINK, new DiamondRenderer());
        pendingEntities.add(follower);
        movementRegistry.put(follower, new FollowMovement(player, 120f));
        collisionRegistry.put(follower, CollisionType.DESTROY);

        // An Immovable Green Rectangle Wall
        Entity wall = new SimulationEntity(V_WIDTH - 100, V_HEIGHT / 2f, 40, 150, Color.GREEN, new RectangleRenderer());
        pendingEntities.add(wall);
        collisionRegistry.put(wall, CollisionType.STATIC);

        Gdx.app.log("GameScene", "Movement Showcase Initialized: Linear(Red), Follow(Green), AI(Yellow)");
        
        audio.playMusic("Music_Game.mp3");
        
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    
    
    @Override
    public void update(float dt) {
    	if (input.isActionJustPressed("SETTINGS")) {
            Gdx.app.log("GameScene", "Opening Settings Menu...");
            navigator.pushScene(new SettingScene()); 
            return;
        }

        if (input.isActionJustPressed("SHOOT")) {
            Gdx.app.log("GameScene", "Shoot Action Triggered");
            audio.playSound("death");
        }
        
        entitySystem.updateEntities(activeEntities, pendingEntities, dt, input);
    	movementSystem.process(movementRegistry, dt);
    	collisionSystem.process(collisionRegistry, V_WIDTH, V_HEIGHT);
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shape) {
    	ScreenUtils.clear(0, 0, 0.1f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);
        
    	entitySystem.renderEntities(activeEntities, batch, shape);
    }
    
    @Override
    public void pause() {
        audio.pauseMusic();
        Gdx.app.log("GameScene", "Simulation Paused");
    }

    @Override
    public void resume() {
        audio.resumeMusic();
        Gdx.app.log("GameScene", "Simulation Resumed");
    }

    @Override
    public void dispose() {
    	for (Entity e : activeEntities) {
            e.destroy();
        }
    	activeEntities.clear();
        pendingEntities.clear();
        movementRegistry.clear();
        collisionRegistry.clear();
    	audio.stopMusic();
    	
    	Gdx.app.log("GameScene", "GameScene memory safely cleared.");
    }
}