package io.github.INF1009_P10_Team7.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.EntityDefinition;
import io.github.INF1009_P10_Team7.engine.entity.EntityManager;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.MovementComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.SpriteComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.events.EventType;
import io.github.INF1009_P10_Team7.engine.events.GameEvent;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

// For Collision
import io.github.INF1009_P10_Team7.engine.collision.CollisionManager;
import io.github.INF1009_P10_Team7.engine.collision.CollisionResolution;

// For Movement Behaviors
import io.github.INF1009_P10_Team7.engine.movement.LinearMovement;

import java.util.Map;

/**
 * GameScene with Movement Behaviors
 *
 * Controls:
 * - ESC -> go to SettingsScene (pass this as previous scene)
 * - BACKSPACE -> go back to MainMenuScene
 *
 * Visual:
 * - Dark background
 *
 * Entities:
 * - Player (Blue Triangle): Uses physics-based movement
 * - Enemy (Red Circle): Uses FollowMovement to chase the player
 * - Static Object (Green Square): Static, no movement
 * - Linear Entity (Yellow Circle): Uses LinearMovement, moves in straight line
 * - AI Wanderer (Purple Circle): Uses AIMovement, random wandering
 */
public class GameScene extends Scene {

    private EntityManager entityManager;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private CollisionManager collisionManager;

    // Store references to created entities (for rendering logic)
    private Map<String, GameEntity> entities;

    private boolean isGoingToSettings = false;

    public GameScene(SceneManager sceneManager) {
        super(sceneManager);

        // Populate entity definitions (stored in parent Scene class)
        initializeEntityDefinitions();
    }

    /**
     * Define what entities should exist in this scene.
     * Populates the entityDefinitions list in parent Scene class.
     * Scene stores the data - does NOT instantiate entities.
     */
    private void initializeEntityDefinitions() {
        // ENTITY 1: Player with Physics-based movement
        entityDefinitions.add(new EntityDefinition.Builder(
            "Player",
            EntityDefinition.EntityType.PLAYER,
            new Vector2(100f, 100f))
            .physics(new Vector2(50f, 0f), 1.0f)
            .collisionRadius(25f)
            .resolutionType(CollisionResolution.ResolutionType.BOUNCE)
            .build());

        // ENTITY 2: Enemy with FollowMovement (chases player)
        entityDefinitions.add(new EntityDefinition.Builder(
            "Enemy",
            EntityDefinition.EntityType.ENEMY,
            new Vector2(400f, 200f))
            .aiMovement(80f) // Will be replaced with follow movement in EntityManager
            .collisionRadius(20f)
            .resolutionType(CollisionResolution.ResolutionType.BOUNCE)
            .build());

        // ENTITY 3: Static object (no movement)
        entityDefinitions.add(new EntityDefinition.Builder(
            "StaticObject",
            EntityDefinition.EntityType.STATIC_OBJECT,
            new Vector2(250f, 150f))
            .rotation(45f)
            .collisionRadius(21f)
            .resolutionType(CollisionResolution.ResolutionType.PASS_THROUGH)
            .build());

        // ENTITY 4: Linear Movement Entity (moves in straight line)
        entityDefinitions.add(new EntityDefinition.Builder(
            "LinearEntity",
            EntityDefinition.EntityType.LINEAR_ENTITY,
            new Vector2(600f, 300f))
            .linearMovement(new Vector2(-1f, -0.5f), 100f)
            .collisionRadius(20f)
            .resolutionType(CollisionResolution.ResolutionType.BOUNCE)
            .build());

        // ENTITY 5: AI Wanderer (random movement)
        entityDefinitions.add(new EntityDefinition.Builder(
            "AIWanderer",
            EntityDefinition.EntityType.AI_WANDERER,
            new Vector2(300f, 400f))
            .aiMovement(60f)
            .collisionRadius(20f)
            .resolutionType(CollisionResolution.ResolutionType.BOUNCE)
            .build());

        // ENTITY 6: Inactive entity (won't be updated)
        entityDefinitions.add(new EntityDefinition.Builder(
            "InactiveEntity",
            EntityDefinition.EntityType.INACTIVE_ENTITY,
            new Vector2(0f, 0f))
            .physics(new Vector2(100f, 100f), 1.0f)
            .isActive(false)
            .build());
    }

    @Override
    protected void onLoad() {
        Gdx.app.log("Scene", "GameScene loaded");

        if (entityManager == null) {

            // Initialize camera and renderer for drawing entities
            camera = new OrthographicCamera();
            camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer = new ShapeRenderer();

            // Initialize the Entity-Component System
            entityManager = new EntityManager(context.getEventBus());

            // Initialize the Collision Manager
            collisionManager = new CollisionManager(context.getEventBus());
            collisionManager.setCollisionSound("bell.mp3");
            Gdx.app.log("CollisionManager", "Initialized with collision sound");

            context.getEventBus().publish(new GameEvent(EventType.GAME_START));

            GameEvent musicEvent = new GameEvent(EventType.PLAY_MUSIC).add("file_path", "Music_Game.mp3");
            context.getEventBus().publish(musicEvent);
            Gdx.app.log("Audio Output", "Game Music loaded");

            // Pass entity definitions to EntityManager - it creates the entities
            entities = entityManager.createEntitiesFromDefinitions(entityDefinitions, collisionManager);

            Gdx.app.log("CollisionManager", "Registered " + collisionManager.getCollidableCount() + " collidable entities");
            Gdx.app.log("ECS", "EntityManager initialized with " + entityManager.getAllEntities().size() + " entities");
            Gdx.app.log("Scene", "Entity definitions passed to EntityManager - entities created");
        } else {
            Gdx.app.log("Scene", "Resuming existing game state...");
            context.getEventBus().publish(new GameEvent(EventType.GAME_RESUMED));
        }
    }


    // Timer for periodic logging of entity positions
    private float logTimer = 0f;
    private static final float LOG_INTERVAL = 2.0f;  // Log every 2 seconds

    @Override
    protected void onUpdate(float delta) {
        // Update all entities in the ECS (includes movement components)
        entityManager.updateAll(delta);

        // Apply boundary constraints to keep entities on screen
        applyBoundaries();

        // Update collision detection and resolution
        collisionManager.update(delta);

        // Periodically log entity positions to demonstrate movement
        logTimer += delta;
        if (logTimer >= LOG_INTERVAL) {
            logTimer = 0f;
            logEntityPositions();
        }

        // Input handling
        if (context.getInputController().isActionJustPressed("SETTINGS")) {
            Gdx.app.log("Input", "Key binded to 'SETTINGS' action was pressed");
            isGoingToSettings = true;
            sceneManager.requestScene(new SettingsScene(sceneManager, this));
        }
        if (context.getInputController().isActionJustPressed("BACK")) {
            Gdx.app.log("Input", "Key binded to 'BACK' action was pressed");
            isGoingToSettings = false;
            sceneManager.requestScene(new MainMenuScene(sceneManager));
        }
        if (context.getInputController().isActionJustPressed("SHOOT")) {
            Gdx.app.log("Input", "Key binded to 'SHOOT' action was pressed");
            // PLAY SOUND
            GameEvent shootEvent = new GameEvent(EventType.PLAY_SOUND).add("file_path", "Sound_Boom.mp3");
            context.getEventBus().publish(shootEvent);
            Gdx.app.log("Audio Output", "Boom Sound played");
        }

        // =========== To show mouse coordinates when moving around ==========
        // =========== This will be commented to prevent log spam ========
        // Gdx.app.log("MouseTest", "X: " + inputController.getMouseX() + " Y: " + inputController.getMouseY());

        // TO IMPLEMENT IO WITH COMPONENT!!
        // TO REMOVE LOGIC WHEN SUMBITTING
        GameEntity player = entities.get("Player");
        if (player != null) {
            PhysicComponent physics = player.getComponent(PhysicComponent.class);

            if (physics != null) {
                float speed = 200f; // How fast it moves
                Vector2 velocity = physics.getVelocity();

                if (context.getInputController().isActionPressed("LEFT")) {
                    // FOR LOGIC
                    velocity.x = -speed; // LEFT
                } else if (context.getInputController().isActionPressed("RIGHT")) {
                    // FOR LOGIC
                    velocity.x = speed; // RIGHT
                } else {
                    // FOR LOGIC
                    physics.setVelocity(0, 0); // STOP
                }
            }
        }
    }

    /**
     * Logs the current positions of all entities to demonstrate movement.
     */
    private void logEntityPositions() {
        GameEntity player = entities.get("Player");
        if (player != null) {
            TransformComponent playerTransform = player.getComponent(TransformComponent.class);
            if (playerTransform != null) {
                Vector2 pos = playerTransform.getPosition();
                Gdx.app.log("ECS", "Player (Physics) position: (" +
                    String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) + ")");
            }
        }

        GameEntity enemy = entities.get("Enemy");
        if (enemy != null) {
            TransformComponent enemyTransform = enemy.getComponent(TransformComponent.class);
            if (enemyTransform != null) {
                Vector2 pos = enemyTransform.getPosition();
                MovementComponent enemyMovement = enemy.getComponent(MovementComponent.class);
                String behaviorType = enemyMovement != null ?
                    enemyMovement.getMovementBehaviour().getClass().getSimpleName() : "None";
                Gdx.app.log("ECS", "Enemy (" + behaviorType + ") position: (" +
                    String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) + ")");
            }
        }

        GameEntity linearEntity = entities.get("LinearEntity");
        if (linearEntity != null) {
            TransformComponent linearTransform = linearEntity.getComponent(TransformComponent.class);
            if (linearTransform != null) {
                Vector2 pos = linearTransform.getPosition();
                Gdx.app.log("ECS", "LinearEntity (LinearMovement) position: (" +
                    String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) + ")");
            }
        }

        GameEntity aiWanderer = entities.get("AIWanderer");
        if (aiWanderer != null) {
            TransformComponent aiTransform = aiWanderer.getComponent(TransformComponent.class);
            if (aiTransform != null) {
                Vector2 pos = aiTransform.getPosition();
                Gdx.app.log("ECS", "AIWanderer (AIMovement) position: (" +
                    String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) + ")");
            }
        }

        GameEntity staticObject = entities.get("StaticObject");
        if (staticObject != null) {
            TransformComponent staticTransform = staticObject.getComponent(TransformComponent.class);
            if (staticTransform != null) {
                Vector2 pos = staticTransform.getPosition();
                Gdx.app.log("ECS", "StaticObject position: (" +
                    String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) +
                    ") - unchanged (no movement)");
            }
        }
    }

    /**
     * Applies boundary constraints to all entities.
     * Entities bounce off screen edges.
     */
    private void applyBoundaries() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        for (Entity entity : entityManager.getAllEntities()) {
            if (!entity.isActive()) continue;

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null) continue;

            Vector2 pos = transform.getPosition();

            // Determine entity radius
            float radius = 20f;
            if (entity.hasComponent(SpriteComponent.class)) {
                radius = 25f;  // Player size
            }

            // For entities with PhysicsComponent, bounce by reversing velocity
            PhysicComponent physics = entity.getComponent(PhysicComponent.class);
            if (physics != null) {
                Vector2 vel = physics.getVelocity();

                // Bounce off left/right boundaries
                if (pos.x - radius < 0) {
                    pos.x = radius;
                    vel.x = Math.abs(vel.x);
                }
                if (pos.x + radius > screenWidth) {
                    pos.x = screenWidth - radius;
                    vel.x = -Math.abs(vel.x);
                }
                // Bounce off top/bottom boundaries
                if (pos.y - radius < 0) {
                    pos.y = radius;
                    vel.y = Math.abs(vel.y);
                }
                if (pos.y + radius > screenHeight) {
                    pos.y = screenHeight - radius;
                    vel.y = -Math.abs(vel.y);
                }
            }
            // For entities with MovementComponent, clamp position and reverse direction
            else {
                MovementComponent movement = entity.getComponent(MovementComponent.class);
                if (movement != null) {
                    boolean hitBoundary = false;

                    // Clamp position to screen boundaries
                    if (pos.x - radius < 0) {
                        pos.x = radius;
                        hitBoundary = true;
                    }
                    if (pos.x + radius > screenWidth) {
                        pos.x = screenWidth - radius;
                        hitBoundary = true;
                    }
                    if (pos.y - radius < 0) {
                        pos.y = radius;
                        hitBoundary = true;
                    }
                    if (pos.y + radius > screenHeight) {
                        pos.y = screenHeight - radius;
                        hitBoundary = true;
                    }

                    // For LinearMovement, reverse direction when hitting boundary
                    if (hitBoundary && movement.getMovementBehaviour() instanceof LinearMovement) {
                        LinearMovement linear = (LinearMovement) movement.getMovementBehaviour();
                        linear.reverseDirection();
                    }
                }
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

            // Different colors and shapes based on entity type
            if (!entity.isActive()) {
                // Gray for inactive entities
                shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 15f);
            } else if (entity.hasComponent(SpriteComponent.class)) {
                // Blue Triangle for Player (physics-based)
                shapeRenderer.setColor(0.2f, 0.6f, 1f, 1f);
                shapeRenderer.triangle(pos.x, pos.y + 25f,
                    pos.x - 20f, pos.y - 15f,
                    pos.x + 20f, pos.y - 15f);
            } else if (entity instanceof GameEntity && ((GameEntity) entity).getName().equals("Enemy")) {
                // Red Circle for Enemy (FollowMovement)
                shapeRenderer.setColor(1f, 0.3f, 0.3f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else if (entity instanceof GameEntity && ((GameEntity) entity).getName().equals("LinearEntity")) {
                // Yellow Circle for LinearMovement entity
                shapeRenderer.setColor(1f, 1f, 0.2f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else if (entity instanceof GameEntity && ((GameEntity) entity).getName().equals("AIWanderer")) {
                // Purple Circle for AI Wanderer
                shapeRenderer.setColor(0.8f, 0.2f, 0.8f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else if (entity.hasComponent(PhysicComponent.class)) {
                // Orange for other physics entities
                shapeRenderer.setColor(1f, 0.6f, 0.2f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else {
                // Green Square for Static Object
                shapeRenderer.setColor(0.3f, 1f, 0.3f, 1f);
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
        Gdx.app.log("Scene", "GameScene unloading...");

        if (isGoingToSettings) {
            // publish Event to notify relevant managers of change in game state
            GameEvent pauseEvent = new GameEvent(EventType.GAME_PAUSED);
            context.getEventBus().publish(pauseEvent);

            Gdx.app.log("Scene", "GameScene state preserved (Going to Settings)");
            isGoingToSettings = false;
        } else {
            dispose();
        }
    }

    @Override
    protected void onDispose() {
        Gdx.app.log("Scene", "GameScene diposed");

        // Clean up the EntityManager
        if (entityManager != null) {
            entityManager.dispose();
            Gdx.app.log("ECS", "GameScene EntityManager disposed");
        }

        // Clean up the CollisionManager
        if (collisionManager != null) {
            collisionManager.clear();
            Gdx.app.log("CollisionManager", "CollisionManager cleared");
        }

        // Dispose of renderer resources
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
