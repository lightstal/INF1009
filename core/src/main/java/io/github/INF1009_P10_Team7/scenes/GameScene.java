package io.github.INF1009_P10_Team7.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.EntityManager;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.SpriteComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneManager;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * GameScene
 *
 * Controls:
 * - ESC -> go to SettingsScene (pass this as previous scene)
 * - BACKSPACE -> go back to MainMenuScene
 *
 * Visual:
 * - Red background
 */
public class GameScene extends Scene {

    private EntityManager entityManager;
    private GameEntity player;
    private GameEntity enemy;
    private GameEntity staticObject;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    

    public GameScene(SceneManager sceneManager) {
        super(sceneManager);
    }

    @Override
    protected void onLoad() {
        Gdx.app.log("Scene", "GameScene loaded");

        io.playMusic("Music_Game.mp3");
        Gdx.app.log("Audio Output", "Game Music loaded");

        // Initialize camera and renderer for drawing entities
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer = new ShapeRenderer();

        // Initialize the Entity-Component System
        entityManager = new EntityManager();

        // Example 1: Player entity with all components (Transform + Physics + Sprite)
        player = new GameEntity("Player");
        player.addComponent(new TransformComponent(100f, 100f));  // Starting position
        player.addComponent(new PhysicComponent(new Vector2(50f, 0f), 1.0f));  // Moving right at 50 units/sec
        player.addComponent(new SpriteComponent("player_sprite"));  // Placeholder sprite reference
        entityManager.addEntity(player);
        Gdx.app.log("ECS", "Created Player entity at (100, 100) with velocity (50, 0)");

        // Example 2: Enemy entity with Transform and Physics (no sprite)
        enemy = new GameEntity("Enemy");
        enemy.addComponent(new TransformComponent(400f, 200f));  // Starting position
        PhysicComponent enemyPhysics = new PhysicComponent(2.0f);  // Heavier mass
        enemyPhysics.setVelocity(-30f, 20f);  // Moving left and up
        enemy.addComponent(enemyPhysics);
        entityManager.addEntity(enemy);
        Gdx.app.log("ECS", "Created Enemy entity at (400, 200) with velocity (-30, 20)");

        // Example 3: Static object with only Transform (no physics, no sprite)
        staticObject = new GameEntity("StaticObject");
        staticObject.addComponent(new TransformComponent(new Vector2(250f, 150f), 45f));  // Position with 45 degree rotation
        entityManager.addEntity(staticObject);
        Gdx.app.log("ECS", "Created StaticObject entity at (250, 150) with rotation 45 degrees");

        // Example 4: Inactive entity (wont be updated "SHOULDNT BE LA")
        GameEntity inactiveEntity = new GameEntity("InactiveEntity");
        inactiveEntity.addComponent(new TransformComponent(0f, 0f));
        inactiveEntity.addComponent(new PhysicComponent(new Vector2(100f, 100f), 1.0f));
        inactiveEntity.setActive(false);  // This entity won't update
        entityManager.addEntity(inactiveEntity);
        Gdx.app.log("ECS", "Created InactiveEntity (won't update because active=false)");

        Gdx.app.log("ECS", "EntityManager initialized with " + entityManager.getAllEntities().size() + " entities");
    }

    // Timer for periodic logging of entity positions
    private float logTimer = 0f;
    private static final float LOG_INTERVAL = 2.0f;  // Log every 2 seconds

    @Override
    protected void onUpdate(float delta) {
        // Update all entities in the ECS
        entityManager.updateAll(delta);

        // Apply boundary constraints to keep entities on screen
        applyBoundaries();

        // Periodically log entity positions to demonstrate physics updates
        logTimer += delta;
        if (logTimer >= LOG_INTERVAL) {
            logTimer = 0f;
            logEntityPositions();
        }

        // Input handling
        if (io.isActionJustPressed("SETTINGS")) {
            Gdx.app.log("Input", "Key binded to 'SETTINGS' action was pressed");
            sceneManager.requestScene(new SettingsScene(sceneManager, this));
        }
        if (io.isActionJustPressed("BACK")) {
            Gdx.app.log("Input", "Key binded to 'BACK' action was pressed");
            sceneManager.requestScene(new MainMenuScene(sceneManager));
        }
        if (io.isActionJustPressed("SHOOT")) {
            Gdx.app.log("Input", "Key binded to 'SHOOT' action was pressed");
            io.playSound("Sound_Boom.mp3");
            Gdx.app.log("Audio Output", "Boom Sound played");
        }
    }

    /**
     * Logs the current positions of all example entities.
     * Demonstrates how components are accessed and how physics updates positions.
     */
    private void logEntityPositions() {
        // Get player position - demonstrates component access
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform != null) {
            Vector2 pos = playerTransform.getPosition();
            Gdx.app.log("ECS", "Player position: (" + String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) + ")");
        }

        // Get enemy position and velocity - demonstrates multiple component access
        TransformComponent enemyTransform = enemy.getComponent(TransformComponent.class);
        PhysicComponent enemyPhysics = enemy.getComponent(PhysicComponent.class);
        if (enemyTransform != null && enemyPhysics != null) {
            Vector2 pos = enemyTransform.getPosition();
            Vector2 vel = enemyPhysics.getVelocity();
            Gdx.app.log("ECS", "Enemy position: (" + String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) +
                       "), velocity: (" + String.format("%.1f", vel.x) + ", " + String.format("%.1f", vel.y) + ")");
        }

        // Static object doesn't move (no PhysicComponent)
        TransformComponent staticTransform = staticObject.getComponent(TransformComponent.class);
        if (staticTransform != null) {
            Vector2 pos = staticTransform.getPosition();
            Gdx.app.log("ECS", "StaticObject position: (" + String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) +
                       ") - unchanged (no physics)");
        }
    }

    /**
     * Applies boundary constraints to all entities with physics.
     * Entities bounce off screen edges by reversing their velocity.
     */
    private void applyBoundaries() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        for (Entity entity : entityManager.getAllEntities()) {
            if (!entity.isActive()) continue;

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            PhysicComponent physics = entity.getComponent(PhysicComponent.class);

            // Only apply boundaries to entities with both transform and physics
            if (transform == null || physics == null) continue;

            Vector2 pos = transform.getPosition();
            Vector2 vel = physics.getVelocity();

            // Determine entity radius based on type (matches render sizes)
            float radius = 20f;
            if (entity.hasComponent(SpriteComponent.class)) {
                radius = 25f;  // Player size
            }

            // Bounce off left boundary
            if (pos.x - radius < 0) {
                pos.x = radius;
                vel.x = Math.abs(vel.x);  // Reverse to positive (move right)
            }
            // Bounce off right boundary
            if (pos.x + radius > screenWidth) {
                pos.x = screenWidth - radius;
                vel.x = -Math.abs(vel.x);  // Reverse to negative (move left)
            }
            // Bounce off bottom boundary
            if (pos.y - radius < 0) {
                pos.y = radius;
                vel.y = Math.abs(vel.y);  // Reverse to positive (move up)
            }
            // Bounce off top boundary
            if (pos.y + radius > screenHeight) {
                pos.y = screenHeight - radius;
                vel.y = -Math.abs(vel.y);  // Reverse to negative (move down)
            }
        }
    }

    @Override
    protected void onRender() {
        // Dark background for better visibility
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);

        // Update camera and set projection for ShapeRenderer
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Render all entities with TransformComponent
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Entity entity : entityManager.getAllEntities()) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null) continue;

            Vector2 pos = transform.getPosition();

            // Different colors based on entity type and state
            if (!entity.isActive()) {
                // Gray for inactive entities
                shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 15f);
            } else if (entity.hasComponent(SpriteComponent.class)) {
                // Blue for entities with sprites (Player)
                shapeRenderer.setColor(0.2f, 0.6f, 1f, 1f);
                shapeRenderer.triangle(pos.x, pos.y + 25f,
                                       pos.x - 20f, pos.y - 15f,
                                       pos.x + 20f, pos.y - 15f);
            } else if (entity.hasComponent(PhysicComponent.class)) {
                // Red for entities with physics only (Enemy)
                shapeRenderer.setColor(1f, 0.3f, 0.3f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else {
                // Green for static entities (Transform only)
                shapeRenderer.setColor(0.3f, 1f, 0.3f, 1f);
                // Draw rotated rectangle for static object
                float size = 30f;
                shapeRenderer.rect(pos.x - size/2, pos.y - size/2, size/2, size/2,
                                   size, size, 1f, 1f, transform.getRotation());
            }
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Scene", "GameScene resize: " + width + "x" + height);
        // Update camera viewport on resize
        camera.setToOrtho(false, width, height);
    }

    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "GameScene unloaded");

        // Clean up the EntityManager
        if (entityManager != null) {
            entityManager.clear();
            Gdx.app.log("ECS", "EntityManager cleared");
        }

        // Dispose of renderer resources
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
