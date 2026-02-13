package io.github.INF1009_P10_Team7.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

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
import io.github.INF1009_P10_Team7.engine.movement.MovementHandler;
import io.github.INF1009_P10_Team7.engine.movement.PlayerMovement;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import java.util.Map;

/**
 * GameScene (demo)
 */
public class GameScene extends Scene {

    private final EntityQuery entityQuery;
    private final SceneFactory factory;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

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

        if (camera == null) {
            camera = new OrthographicCamera();
            camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer = new ShapeRenderer();

            audio.setMusic("Music_Game.mp3");
            Gdx.app.log("AudioController", "Game music loaded");
        }

        named = entityQuery.getNamedEntities();
        movementLogic = new PlayerMovement();
    }

    @Override
    protected void onUpdate(float delta) {
        if (named == null || named.isEmpty()) {
            named = entityQuery.getNamedEntities();
        }

        applyBoundaries();

        logTimer += delta;
        if (logTimer >= LOG_INTERVAL) {
            logTimer = 0f;
            logEntityPositions();
        }

        // Inputs
        if (input.isActionJustPressed("SETTINGS")) {
            Gdx.app.log("InputController", "Action 'SETTINGS' pressed");
            goingToSettings = true;
            // IMPORTANT: never return to a disposed Scene instance.
            // SettingsScene will return to a *fresh* GameScene via the factory.
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

    /** Keep entities inside the screen and bounce/reverse direction. */
    private void applyBoundaries() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        for (Entity entity : entityQuery.getAllEntities()) {
            if (!entity.isActive()) continue;

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null) continue;

            Vector2 pos = transform.getPosition();

            float radius = 20f;
            if (entity.hasComponent(SpriteComponent.class)) radius = 25f;

            PhysicComponent physics = entity.getComponent(PhysicComponent.class);
            if (physics != null) {
                Vector2 vel = physics.getVelocity();

                if (pos.x - radius < 0) {
                    pos.x = radius;
                    vel.x = Math.abs(vel.x);
                }
                if (pos.x + radius > w) {
                    pos.x = w - radius;
                    vel.x = -Math.abs(vel.x);
                }
                if (pos.y - radius < 0) {
                    pos.y = radius;
                    vel.y = Math.abs(vel.y);
                }
                if (pos.y + radius > h) {
                    pos.y = h - radius;
                    vel.y = -Math.abs(vel.y);
                }
            } else {
                MovementComponent movement = entity.getComponent(MovementComponent.class);
                if (movement != null) {
                    boolean hit = false;

                    if (pos.x - radius < 0) { pos.x = radius; hit = true; }
                    if (pos.x + radius > w) { pos.x = w - radius; hit = true; }
                    if (pos.y - radius < 0) { pos.y = radius; hit = true; }
                    if (pos.y + radius > h) { pos.y = h - radius; hit = true; }

                    if (hit && movement.getMovementBehaviour() instanceof LinearMovement) {
                        ((LinearMovement) movement.getMovementBehaviour()).reverseDirection();
                    }
                }
            }
        }
    }

    @Override
    protected void onRender() {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);

        if (camera == null || shapeRenderer == null) return;

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Entity entity : entityQuery.getAllEntities()) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null) continue;

            Vector2 pos = transform.getPosition();

            if (!entity.isActive()) {
                shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 15f);
            } else if (entity.hasComponent(SpriteComponent.class)) {
                shapeRenderer.setColor(0.2f, 0.6f, 1f, 1f);
                shapeRenderer.triangle(pos.x, pos.y + 25f,
                        pos.x - 20f, pos.y - 15f,
                        pos.x + 20f, pos.y - 15f);
            } else if (entity instanceof GameEntity && ((GameEntity) entity).getName().equals("Enemy")) {
                shapeRenderer.setColor(1f, 0.3f, 0.3f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else if (entity instanceof GameEntity && ((GameEntity) entity).getName().equals("LinearEntity")) {
                shapeRenderer.setColor(1f, 1f, 0.2f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else if (entity instanceof GameEntity && ((GameEntity) entity).getName().equals("AIWanderer")) {
                shapeRenderer.setColor(0.8f, 0.2f, 0.8f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else if (entity.hasComponent(PhysicComponent.class)) {
                shapeRenderer.setColor(1f, 0.6f, 0.2f, 1f);
                shapeRenderer.circle(pos.x, pos.y, 20f);
            } else {
                shapeRenderer.setColor(0.3f, 1f, 0.3f, 1f);
                float size = 30f;
                shapeRenderer.rect(pos.x - size/2f, pos.y - size/2f, size/2f, size/2f,
                        size, size, 1f, 1f, transform.getRotation());
            }
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Scene", "GameScene resize: " + width + "x" + height);
        if (camera != null) camera.setToOrtho(false, width, height);
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
