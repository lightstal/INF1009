package io.github.INF1009_P10_Team7.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.engine.collision.CollisionResolution;
import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.EntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.entity.components.*;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.*;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import java.util.Map;
import java.util.Random;

/**
 * GameScene (simulation layer demo)
 *
 * CHANGES FROM ORIGINAL V2:
 * - Entities are created directly (no EntityDefinition, no EntityType enum)
 * - Uses RenderComponent with IRenderBehaviour strategy for all rendering
 * - Registers entities with ICollisionSystem and IMovementSystem directly
 * - No context-specific code in the engine — all specifics live HERE in the scene
 */
public class GameScene extends Scene {

    private final EntityQuery entityQuery;
    private final IEntitySystem entitySystem;
    private final ICollisionSystem collisionSystem;
    private final IMovementSystem movementSystem;
    private final SceneFactory factory;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_W = 800f;
    private static final float WORLD_H = 480f;

    private Map<String, GameEntity> named;
    private boolean goingToSettings = false;
    private MovementHandler movementLogic = new PlayerMovement();

    private float logTimer = 0f;
    private static final float LOG_INTERVAL = 2.0f;

    public GameScene(
        IInputController input,
        IAudioController audio,
        SceneNavigator nav,
        EntityQuery entityQuery,
        IEntitySystem entitySystem,
        ICollisionSystem collisionSystem,
        IMovementSystem movementSystem,
        SceneFactory factory
    ) {
        super(input, audio, nav);
        this.entityQuery = entityQuery;
        this.entitySystem = entitySystem;
        this.collisionSystem = collisionSystem;
        this.movementSystem = movementSystem;
        this.factory = factory;
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

        // Configure collision sound
        collisionSystem.setCollisionSound("bell.mp3");

        // ===== CREATE ENTITIES DIRECTLY (no EntityDefinition, no switch) =====
        createEntities();

        named = entityQuery.getNamedEntities();
        movementLogic = new PlayerMovement();

        Gdx.app.log("GameScene", "World locked at: " + WORLD_W + "x" + WORLD_H);
    }

    /**
     * Scene creates entities, attaches components, and registers with managers.
     * All context-specific logic lives HERE, not in the engine.
     */
    private void createEntities() {
        // --- Player ---
        GameEntity player = new GameEntity("Player");
        player.addComponent(new TransformComponent(100f, 100f));
        player.addComponent(new PhysicComponent(new Vector2(50f, 0f), 1.0f));
        player.addComponent(new SpriteComponent("player_sprite"));
        player.addComponent(new RenderComponent(new TriangleRenderer(25f), new Color(0.2f, 0.6f, 1f, 1f)));
        player.setCollisionRadius(25f);
        entitySystem.addEntity(player);
        collisionSystem.registerCollidable(player, CollisionResolution.ResolutionType.BOUNCE);
        movementSystem.addEntity(player, null); // Physics-only movement

        // --- Diamonds (static collectibles) ---
        Random rand = new Random();
        float padding = 50f;
        for (int i = 1; i <= 3; i++) {
            float rx = padding + rand.nextFloat() * (WORLD_W - 2 * padding);
            float ry = padding + rand.nextFloat() * (WORLD_H - 2 * padding);

            GameEntity diamond = new GameEntity("Diamond" + i);
            diamond.addComponent(new TransformComponent(new Vector2(rx, ry), 45f));
            diamond.addComponent(new RenderComponent(new RectangleRenderer(30f, 30f), new Color(0.3f, 1f, 0.3f, 1f)));
            diamond.setCollisionRadius(21f);
            entitySystem.addEntity(diamond);
            collisionSystem.registerCollidable(diamond, CollisionResolution.ResolutionType.PASS_THROUGH);
        }

        // --- Enemy (follows player) ---
        GameEntity enemy = new GameEntity("Enemy");
        enemy.addComponent(new TransformComponent(400f, 200f));
        enemy.addComponent(new RenderComponent(new CircleRenderer(20f), new Color(1f, 0.3f, 0.3f, 1f)));
        enemy.setCollisionRadius(20f);
        entitySystem.addEntity(enemy);
        collisionSystem.registerCollidable(enemy, CollisionResolution.ResolutionType.BOUNCE);
        movementSystem.addEntity(enemy, new FollowMovement(player, 80f));

        // --- Static Object ---
        GameEntity staticObj = new GameEntity("StaticObject");
        staticObj.addComponent(new TransformComponent(new Vector2(250f, 150f), 45f));
        staticObj.addComponent(new RenderComponent(new RectangleRenderer(30f, 30f), new Color(0.3f, 1f, 0.3f, 1f)));
        staticObj.setCollisionRadius(21f);
        entitySystem.addEntity(staticObj);
        collisionSystem.registerCollidable(staticObj, CollisionResolution.ResolutionType.PASS_THROUGH);

        // --- Linear Entity ---
        GameEntity linear = new GameEntity("LinearEntity");
        linear.addComponent(new TransformComponent(600f, 300f));
        linear.addComponent(new RenderComponent(new CircleRenderer(20f), new Color(1f, 1f, 0.2f, 1f)));
        linear.setCollisionRadius(20f);
        LinearMovement linearBehaviour = new LinearMovement(new Vector2(-1f, -0.5f), 100f);
        linear.addComponent(new MovementComponent(linearBehaviour));
        entitySystem.addEntity(linear);
        collisionSystem.registerCollidable(linear, CollisionResolution.ResolutionType.BOUNCE);
        movementSystem.addEntity(linear, linearBehaviour);

        // --- AI Wanderer ---
        GameEntity aiWanderer = new GameEntity("AIWanderer");
        aiWanderer.addComponent(new TransformComponent(300f, 400f));
        aiWanderer.addComponent(new RenderComponent(new CircleRenderer(20f), new Color(0.8f, 0.2f, 0.8f, 1f)));
        aiWanderer.setCollisionRadius(20f);
        entitySystem.addEntity(aiWanderer);
        collisionSystem.registerCollidable(aiWanderer, CollisionResolution.ResolutionType.BOUNCE);
        movementSystem.addEntity(aiWanderer, new AImovement(60f));

        // --- Bouncing Circle ---
        GameEntity bouncer = new GameEntity("BouncingCircle");
        bouncer.addComponent(new TransformComponent(500f, 240f));
        bouncer.addComponent(new PhysicComponent(new Vector2(150f, 100f), 1.0f));
        bouncer.addComponent(new RenderComponent(new CircleRenderer(18f), new Color(0f, 0.9f, 0.9f, 1f)));
        bouncer.setCollisionRadius(18f);
        entitySystem.addEntity(bouncer);
        collisionSystem.registerCollidable(bouncer, CollisionResolution.ResolutionType.BOUNCE);
        movementSystem.addEntity(bouncer, null); // Physics-only

        // --- Inactive Entity (demonstrates lifecycle) ---
        GameEntity inactive = new GameEntity("InactiveEntity");
        inactive.addComponent(new TransformComponent(0f, 0f));
        inactive.addComponent(new PhysicComponent(new Vector2(100f, 100f), 1.0f));
        inactive.setActive(false);
        entitySystem.addEntity(inactive);
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

        if (input.isActionJustPressed("SETTINGS")) {
            goingToSettings = true;
            nav.pushScene(factory.createSettingsScene());
            return;
        }
        if (input.isActionJustPressed("BACK")) {
            goingToSettings = false;
            nav.requestScene(factory.createMainMenuScene());
            return;
        }
        if (input.isActionJustPressed("SHOOT")) {
            audio.playSound("Sound_Boom.mp3");
        }

        GameEntity player = (named != null) ? named.get("Player") : null;
        if (player == null) return;

        PhysicComponent physics = player.getComponent(PhysicComponent.class);
        if (movementLogic != null && physics != null) {
            movementLogic.handle(physics, input);
        }
    }

    @Override
    protected void onLateUpdate(float delta) {
        applyBoundaries();
    }

    private void applyBoundaries() {
        for (Entity entity : entityQuery.getAllEntities()) {
            if (entity == null || !entity.isActive()) continue;

            TransformComponent t = entity.getComponent(TransformComponent.class);
            if (t == null) continue;

            float radius = 0f;
            if (entity instanceof GameEntity) {
                radius = ((GameEntity) entity).getCollisionRadius();
            }

            Vector2 pos = t.getPosition();

            boolean hitLeft = false, hitRight = false, hitBottom = false, hitTop = false;

            if (pos.x - radius < 0f)           { pos.x = radius;               hitLeft = true; }
            else if (pos.x + radius > WORLD_W)  { pos.x = WORLD_W - radius;    hitRight = true; }

            if (pos.y - radius < 0f)           { pos.y = radius;               hitBottom = true; }
            else if (pos.y + radius > WORLD_H)  { pos.y = WORLD_H - radius;    hitTop = true; }

            boolean hitX = hitLeft || hitRight;
            boolean hitY = hitBottom || hitTop;

            if (!hitX && !hitY) continue;

            PhysicComponent p = entity.getComponent(PhysicComponent.class);
            if (p != null) {
                Vector2 vel = p.getVelocity();

                // Reflect velocity at walls (bounce) for all physics entities
                if (hitLeft  && vel.x < 0f) vel.x = -vel.x;
                if (hitRight && vel.x > 0f) vel.x = -vel.x;
                if (hitBottom && vel.y < 0f) vel.y = -vel.y;
                if (hitTop    && vel.y > 0f) vel.y = -vel.y;
            }

            MovementComponent mc = entity.getComponent(MovementComponent.class);
            if (mc != null) {
                MovementBehaviour behaviour = mc.getMovementBehaviour();
                if (behaviour instanceof LinearMovement) {
                    LinearMovement linear = (LinearMovement) behaviour;
                    Vector2 dir = linear.getDirection();
                    // Only reverse if direction is pointing INTO the wall
                    if (hitLeft && dir.x < 0f)   dir.x = -dir.x;
                    if (hitRight && dir.x > 0f)  dir.x = -dir.x;
                    if (hitBottom && dir.y < 0f) dir.y = -dir.y;
                    if (hitTop && dir.y > 0f)    dir.y = -dir.y;
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

        // Render ALL entities using RenderComponent (Strategy Pattern!)
        for (Entity entity : entityQuery.getAllEntities()) {
            if (!entity.isActive()) continue;

            RenderComponent rc = entity.getComponent(RenderComponent.class);
            if (rc != null) {
                rc.render(shapeRenderer);
            }
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "GameScene unloading...");
    }

    @Override
    protected void onDispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        Gdx.app.log("Scene", "GameScene disposed");
    }
}
