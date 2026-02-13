package io.github.INF1009_P10_Team7.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.engine.collision.CollisionResolution;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.EntityDefinition;
import io.github.INF1009_P10_Team7.engine.entity.EntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.MovementComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.SpriteComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.movement.LinearMovement;
import io.github.INF1009_P10_Team7.engine.movement.MovementBehaviour;
import io.github.INF1009_P10_Team7.engine.movement.MovementHandler;
import io.github.INF1009_P10_Team7.engine.movement.PlayerMovement;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import java.util.Map;

/**
 * GameScene (demo)
 *
 * Uses StretchViewport so the 800x480 virtual world SCALES with the window.
 * Entities always take the same percentage of the screen regardless of window size.
 */
public class GameScene extends Scene {

    private final EntityQuery entityQuery;
    private final SceneFactory factory;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    // FIXED WORLD SIZE - NEVER CHANGES!
    private Viewport viewport;
    private static final float WORLD_W = 800f;
    private static final float WORLD_H = 480f;

    // Convenience reference
    private Map<String, GameEntity> named;

    private boolean goingToSettings = false;

    private MovementHandler movementLogic = new PlayerMovement();

    // Timer for periodic logging of entity positions
    private float logTimer = 0f;
    private static final float LOG_INTERVAL = 2.0f;

    public GameScene(
        io.github.INF1009_P10_Team7.engine.inputoutput.InputController input,
        io.github.INF1009_P10_Team7.engine.inputoutput.AudioController audio,
        SceneNavigator nav,
        EntityQuery entityQuery,
        SceneFactory factory
    ) {
        super(input, audio, nav);
        this.entityQuery = entityQuery;
        this.factory = factory;
        initializeEntityDefinitions();
    }

    /** Define what entities should exist in this scene (data only). */
    private void initializeEntityDefinitions() {
        entityDefinitions.add(new EntityDefinition.Builder(
            "Player",
            EntityDefinition.EntityType.PLAYER,
            new Vector2(100f, 100f))
            .physics(new Vector2(50f, 0f), 1.0f)
            .collisionRadius(25f)
            .resolutionType(CollisionResolution.ResolutionType.BOUNCE)
            .build());

        entityDefinitions.add(new EntityDefinition.Builder(
            "Enemy",
            EntityDefinition.EntityType.ENEMY,
            new Vector2(400f, 200f))
            .aiMovement(80f)
            .collisionRadius(20f)
            .resolutionType(CollisionResolution.ResolutionType.BOUNCE)
            .build());

        entityDefinitions.add(new EntityDefinition.Builder(
            "StaticObject",
            EntityDefinition.EntityType.STATIC_OBJECT,
            new Vector2(250f, 150f))
            .rotation(45f)
            .collisionRadius(21f)
            .resolutionType(CollisionResolution.ResolutionType.PASS_THROUGH)
            .build());

        entityDefinitions.add(new EntityDefinition.Builder(
            "LinearEntity",
            EntityDefinition.EntityType.LINEAR_ENTITY,
            new Vector2(600f, 300f))
            .linearMovement(new Vector2(-1f, -0.5f), 100f)
            .collisionRadius(20f)
            .resolutionType(CollisionResolution.ResolutionType.BOUNCE)
            .build());

        entityDefinitions.add(new EntityDefinition.Builder(
            "AIWanderer",
            EntityDefinition.EntityType.AI_WANDERER,
            new Vector2(300f, 400f))
            .aiMovement(60f)
            .collisionRadius(20f)
            .resolutionType(CollisionResolution.ResolutionType.BOUNCE)
            .build());

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

        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_W, WORLD_H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer();
        }

        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0);
        camera.update();

        audio.setMusic("Music_Game.mp3");
        Gdx.app.log("AudioController", "Game music loaded");

        named = entityQuery.getNamedEntities();
        movementLogic = new PlayerMovement();

        Gdx.app.log("GameScene", "World locked at: " + WORLD_W + "x" + WORLD_H);
    }

    @Override
    protected void onUpdate(float delta) {
        if (named == null || named.isEmpty()) {
            named = entityQuery.getNamedEntities();
        }

        logTimer += delta;
        if (logTimer >= LOG_INTERVAL) {
            logTimer = 0f;
            logEntityPositions();
        }

        // Inputs
        if (input.isActionJustPressed("SETTINGS")) {
            Gdx.app.log("InputController", "Action 'SETTINGS' pressed");
            goingToSettings = true;
            nav.pushScene(factory.createSettingsScene());
            return;
        }
        if (input.isActionJustPressed("BACK")) {
            Gdx.app.log("InputController", "Action 'BACK' pressed");
            goingToSettings = false;
            nav.requestScene(factory.createMainMenuScene());
            return;
        }
        if (input.isActionJustPressed("SHOOT")) {
            Gdx.app.log("InputController", "Action 'SHOOT' pressed");
            audio.playSound("Sound_Boom.mp3");
        }

        GameEntity player = (named != null) ? named.get("Player") : null;
        if (player == null) return;

        PhysicComponent physics = player.getComponent(PhysicComponent.class);
        if (movementLogic != null && physics != null) {
            movementLogic.handle(physics, input);
        }
    }

    /**
     * Called by GameEngine AFTER movement.updateAll() and collision.update().
     * This is the correct place for boundary clamping because all movement
     * has already been applied.
     *
     * Frame order:
     * 1. onUpdate()         - input handling
     * 2. movement.updateAll - AI/Linear/Follow/Physics move entities
     * 3. collision.update   - entity-vs-entity collisions
     * 4. onLateUpdate()     - THIS: clamp all entities back inside world bounds
     * 5. onRender()         - draw everything (entities are guaranteed in-bounds)
     */
    @Override
    protected void onLateUpdate(float delta) {
        applyBoundaries();
    }

    /**
     * Clamps ALL active entities within world bounds using EDGE detection (radius-aware).
     * The entity's EDGE (center - radius) must stay inside the world, not just its center.
     *
     * - PhysicComponent entities (Player): clamps + zeroes velocity at wall
     * - LinearMovement entities (yellow ball): clamps + reverses direction axis (bounce)
     * - Other entities (AI, Follow): clamps position
     */
    private void applyBoundaries() {
        for (Entity entity : entityQuery.getAllEntities()) {
            if (entity == null || !entity.isActive()) continue;

            TransformComponent t = entity.getComponent(TransformComponent.class);
            if (t == null) continue;

            // Get collision radius for edge-based boundary
            float radius = 0f;
            if (entity instanceof GameEntity) {
                radius = ((GameEntity) entity).getCollisionRadius();
            }

            Vector2 pos = t.getPosition();

            boolean hitLeft = false, hitRight = false, hitBottom = false, hitTop = false;

            // Clamp using EDGE (pos - radius touches wall), not center
            if (pos.x - radius < 0f)           { pos.x = radius;               hitLeft = true; }
            else if (pos.x + radius > WORLD_W)  { pos.x = WORLD_W - radius;    hitRight = true; }

            if (pos.y - radius < 0f)           { pos.y = radius;               hitBottom = true; }
            else if (pos.y + radius > WORLD_H)  { pos.y = WORLD_H - radius;    hitTop = true; }

            boolean hitX = hitLeft || hitRight;
            boolean hitY = hitBottom || hitTop;

            if (!hitX && !hitY) continue;

            // --- PhysicComponent entities (Player): zero velocity at wall ---
            PhysicComponent p = entity.getComponent(PhysicComponent.class);
            if (p != null) {
                Vector2 vel = p.getVelocity();
                if (hitLeft && vel.x < 0f) vel.x = 0f;
                if (hitRight && vel.x > 0f) vel.x = 0f;
                if (hitBottom && vel.y < 0f) vel.y = 0f;
                if (hitTop && vel.y > 0f) vel.y = 0f;
            }

            // --- LinearMovement entities (yellow ball): reverse direction axis ---
            MovementComponent mc = entity.getComponent(MovementComponent.class);
            if (mc != null) {
                MovementBehaviour behaviour = mc.getMovementBehaviour();
                if (behaviour instanceof LinearMovement) {
                    LinearMovement linear = (LinearMovement) behaviour;
                    Vector2 dir = linear.getDirection();
                    if (hitX) dir.x = -dir.x;
                    if (hitY) dir.y = -dir.y;
                }
            }
        }
    }

    private void logEntityPositions() {
        if (named == null) return;

        GameEntity player = named.get("Player");
        if (player != null) {
            TransformComponent t = player.getComponent(TransformComponent.class);
            if (t != null) {
                Vector2 pos = t.getPosition();
                Gdx.app.log("ECS", "Player position: (" + String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) + ")");
            }
        }

        GameEntity enemy = named.get("Enemy");
        if (enemy != null) {
            TransformComponent t = enemy.getComponent(TransformComponent.class);
            if (t != null) {
                Vector2 pos = t.getPosition();
                MovementComponent mc = enemy.getComponent(MovementComponent.class);
                String behaviourType = (mc != null && mc.getMovementBehaviour() != null)
                    ? mc.getMovementBehaviour().getClass().getSimpleName()
                    : "None";
                Gdx.app.log("ECS", "Enemy (" + behaviourType + ") position: (" + String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) + ")");
            }
        }
    }

    @Override
    protected void onRender() {
        if (camera == null || shapeRenderer == null || viewport == null) return;

        viewport.apply();
        camera.update();

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw entities - SKIP inactive ones!
        for (Entity entity : entityQuery.getAllEntities()) {
            if (!entity.isActive()) continue;

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null) continue;

            Vector2 pos = transform.getPosition();

            if (entity.hasComponent(SpriteComponent.class)) {
                // Player (blue triangle)
                shapeRenderer.setColor(0.2f, 0.6f, 1f, 1f);
                shapeRenderer.triangle(
                    pos.x, pos.y + 25f,
                    pos.x - 20f, pos.y - 15f,
                    pos.x + 20f, pos.y - 15f
                );
            } else if (entity instanceof GameEntity && ((GameEntity) entity).getName().equals("Enemy")) {
                // Enemy (red circle)
                shapeRenderer.setColor(1f, 0.3f, 0.3f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else if (entity instanceof GameEntity && ((GameEntity) entity).getName().equals("LinearEntity")) {
                // LinearEntity (yellow circle)
                shapeRenderer.setColor(1f, 1f, 0.2f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else if (entity instanceof GameEntity && ((GameEntity) entity).getName().equals("AIWanderer")) {
                // AIWanderer (purple circle)
                shapeRenderer.setColor(0.8f, 0.2f, 0.8f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else if (entity.hasComponent(PhysicComponent.class)) {
                // Other physics entities (orange circle)
                shapeRenderer.setColor(1f, 0.6f, 0.2f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else {
                // Static objects (green rotated square)
                shapeRenderer.setColor(0.3f, 1f, 0.3f, 1f);
                float size = 30f;
                shapeRenderer.rect(
                    pos.x - size / 2f, pos.y - size / 2f,
                    size / 2f, size / 2f,
                    size, size,
                    1f, 1f,
                    transform.getRotation()
                );
            }
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Scene", "GameScene resize: " + width + "x" + height);

        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "GameScene unloading...");
        if (goingToSettings) {
            Gdx.app.log("Scene", "GameScene preserved (going to settings)");
            goingToSettings = false;
        }
    }

    @Override
    protected void onDispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        Gdx.app.log("Scene", "GameScene disposed");
    }
}
