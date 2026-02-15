package io.github.INF1009_P10_Team7.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * GameScene (simulation layer demo)
 *
 * Features:
 * 1) 5 static green squares (large) with random spawn positions
 * 2) 1 blue triangle (player-controlled)
 * 3) 5 yellow balls (slightly bigger) with linear movement — bounce off green squares and walls,
 *    but on collision with the blue triangle they grant a speed boost and disappear
 * 4) 1 orange ball with random (AI) movement to showcase random movement
 * 5) 1 magenta ball with follow movement that chases the player
 * 6) All entities bounce off each other EXCEPT yellow balls pass through
 *    the player (pickup mechanic instead of bounce)
 * 7) Boom sound on left click, ding/bell sound on collisions
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

    // Track yellow balls and green squares for manual collision logic
    private final List<GameEntity> yellowBalls = new ArrayList<>();
    private final List<GameEntity> greenSquares = new ArrayList<>();
    private GameEntity player;
    private GameEntity followerBall;

    // Player speed boost tracking
    private float playerSpeedMultiplier = 1.0f;
    private static final float SPEED_BOOST_PER_BALL = 0.15f;

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

        // Configure collision sound (ding on collision)
        collisionSystem.setCollisionSound("bell.mp3");

        // ===== CREATE ENTITIES =====
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
        Random rand = new Random();
        float padding = 60f;

        // --- 1) Player: Blue Triangle (controllable) ---
        player = new GameEntity("Player");
        player.addComponent(new TransformComponent(WORLD_W / 2f, WORLD_H / 2f));
        player.addComponent(new PhysicComponent(new Vector2(0f, 0f), 1.0f));
        player.addComponent(new RenderComponent(new TriangleRenderer(25f), new Color(0.2f, 0.6f, 1f, 1f)));
        player.setCollisionRadius(25f);
        entitySystem.addEntity(player);
        collisionSystem.registerCollidable(player, CollisionResolution.ResolutionType.BOUNCE);
        movementSystem.addEntity(player, null); // Physics-only (player-controlled)

        // --- 2) 5 Static Green Squares with random spawn positions (large) ---
        for (int i = 1; i <= 5; i++) {
            float rx = padding + rand.nextFloat() * (WORLD_W - 2 * padding);
            float ry = padding + rand.nextFloat() * (WORLD_H - 2 * padding);

            GameEntity square = new GameEntity("GreenSquare" + i);
            square.addComponent(new TransformComponent(new Vector2(rx, ry), 0f));
            square.addComponent(new RenderComponent(new RectangleRenderer(55f, 55f), new Color(0.2f, 0.85f, 0.2f, 1f)));
            square.setCollisionRadius(38f);
            entitySystem.addEntity(square);
            // BOUNCE so other entities (orange ball, follow ball, player) bounce off them
            collisionSystem.registerCollidable(square, CollisionResolution.ResolutionType.BOUNCE);
            greenSquares.add(square);
        }

        // --- 3) 5 Yellow Balls with linear movement (slightly bigger) ---
        for (int i = 1; i <= 5; i++) {
            float rx = padding + rand.nextFloat() * (WORLD_W - 2 * padding);
            float ry = padding + rand.nextFloat() * (WORLD_H - 2 * padding);

            // Random direction
            float dirX = rand.nextFloat() * 2f - 1f;
            float dirY = rand.nextFloat() * 2f - 1f;
            if (Math.abs(dirX) < 0.1f && Math.abs(dirY) < 0.1f) {
                dirX = 1f;
                dirY = 0.5f;
            }

            float speed = 80f + rand.nextFloat() * 60f; // 80-140

            GameEntity yellowBall = new GameEntity("YellowBall" + i);
            yellowBall.addComponent(new TransformComponent(rx, ry));
            yellowBall.addComponent(new RenderComponent(new CircleRenderer(18f), new Color(1f, 1f, 0.2f, 1f)));
            yellowBall.setCollisionRadius(18f);
            LinearMovement linearBehaviour = new LinearMovement(new Vector2(dirX, dirY), speed);
            yellowBall.addComponent(new MovementComponent(linearBehaviour));
            entitySystem.addEntity(yellowBall);
            // NOT registered with collisionSystem — all yellow ball collisions
            // (bounce off green squares + pickup by player) are handled manually
            // in the scene, so no engine ding sound is triggered for them.
            movementSystem.addEntity(yellowBall, linearBehaviour);
            yellowBalls.add(yellowBall);
        }

        // --- 4) 1 Orange Ball with random (AI) movement ---
        GameEntity randomBall = new GameEntity("RandomBall");
        randomBall.addComponent(new TransformComponent(
            padding + rand.nextFloat() * (WORLD_W - 2 * padding),
            padding + rand.nextFloat() * (WORLD_H - 2 * padding)
        ));
        randomBall.addComponent(new RenderComponent(new CircleRenderer(18f), new Color(1f, 0.5f, 0.1f, 1f)));
        randomBall.setCollisionRadius(18f);
        entitySystem.addEntity(randomBall);
        collisionSystem.registerCollidable(randomBall, CollisionResolution.ResolutionType.BOUNCE);
        movementSystem.addEntity(randomBall, new AImovement(70f));

        // --- 5) 1 Magenta Ball with follow movement (chases the player) ---
        // NOT registered with collisionSystem so it passes through the blue triangle.
        // Bouncing off green squares (with ding) is handled manually in the scene.
        followerBall = new GameEntity("FollowerBall");
        followerBall.addComponent(new TransformComponent(
            padding + rand.nextFloat() * (WORLD_W - 2 * padding),
            padding + rand.nextFloat() * (WORLD_H - 2 * padding)
        ));
        followerBall.addComponent(new RenderComponent(new CircleRenderer(18f), new Color(0.9f, 0.2f, 0.7f, 1f)));
        followerBall.setCollisionRadius(18f);
        entitySystem.addEntity(followerBall);
        movementSystem.addEntity(followerBall, new FollowMovement(player, 80f));
    }

    @Override
    protected void onUpdate(float delta) {
        if (named == null || named.isEmpty()) {
            named = entityQuery.getNamedEntities();
        }

        if (Gdx.input.getInputProcessor() != null) {
            Gdx.input.setInputProcessor(null);
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

        // Boom sound on left click
        if (input.isActionJustPressed("SHOOT")) {
            audio.playSound("Sound_Boom.mp3");
        }

        if (player == null || !player.isActive()) return;

        // Handle player movement with speed multiplier from collected yellow balls
        PhysicComponent physics = player.getComponent(PhysicComponent.class);
        if (movementLogic != null && physics != null) {
            movementLogic.handle(physics, input);
            Vector2 vel = physics.getVelocity();
            vel.x *= playerSpeedMultiplier;
            vel.y *= playerSpeedMultiplier;
        }

        // Custom collision checks for yellow balls
        checkYellowBallGreenSquareBounce();
        checkYellowBallPlayerPickup();

        // Magenta ball bounces off green squares (with ding) but passes through player
        checkFollowerGreenSquareBounce();

        // ESC to open settings
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            nav.pushScene(factory.createSettingsScene());
        }
    }

    /**
     * Manually bounces yellow balls off green squares.
     * Since yellow balls are PASS_THROUGH in the collision system,
     * we handle their bouncing off static green squares here.
     */
    private void checkYellowBallGreenSquareBounce() {
        for (GameEntity ball : yellowBalls) {
            if (!ball.isActive()) continue;

            TransformComponent ballTransform = ball.getComponent(TransformComponent.class);
            if (ballTransform == null) continue;
            Vector2 ballPos = ballTransform.getPosition();
            float ballRadius = ball.getCollisionRadius();

            MovementComponent mc = ball.getComponent(MovementComponent.class);
            if (mc == null) continue;
            MovementBehaviour behaviour = mc.getMovementBehaviour();
            if (!(behaviour instanceof LinearMovement)) continue;
            LinearMovement linear = (LinearMovement) behaviour;

            for (GameEntity square : greenSquares) {
                if (!square.isActive()) continue;

                TransformComponent sqTransform = square.getComponent(TransformComponent.class);
                if (sqTransform == null) continue;
                Vector2 sqPos = sqTransform.getPosition();
                float sqRadius = square.getCollisionRadius();

                // Circle-circle overlap check
                float dx = ballPos.x - sqPos.x;
                float dy = ballPos.y - sqPos.y;
                float distSq = dx * dx + dy * dy;
                float minDist = ballRadius + sqRadius;

                if (distSq < minDist * minDist) {
                    float dist = (float) Math.sqrt(distSq);
                    if (dist < 0.001f) dist = 0.001f;

                    // Collision normal (from square towards ball)
                    float nx = dx / dist;
                    float ny = dy / dist;

                    // Separate the ball from the square
                    float penetration = minDist - dist;
                    ballPos.x += nx * penetration;
                    ballPos.y += ny * penetration;

                    // Reflect the linear movement direction
                    Vector2 dir = linear.getDirection();
                    float dot = dir.x * nx + dir.y * ny;
                    dir.x -= 2f * dot * nx;
                    dir.y -= 2f * dot * ny;
                }
            }
        }
    }

    /**
     * Checks if any active yellow ball overlaps the player.
     * Grants a speed boost and deactivates (disappears) the ball.
     */
    private void checkYellowBallPlayerPickup() {
        if (player == null || !player.isActive()) return;

        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform == null) return;
        Vector2 playerPos = playerTransform.getPosition();
        float playerRadius = player.getCollisionRadius();

        for (int i = yellowBalls.size() - 1; i >= 0; i--) {
            GameEntity ball = yellowBalls.get(i);
            if (!ball.isActive()) continue;

            TransformComponent ballTransform = ball.getComponent(TransformComponent.class);
            if (ballTransform == null) continue;
            Vector2 ballPos = ballTransform.getPosition();
            float ballRadius = ball.getCollisionRadius();

            float dx = playerPos.x - ballPos.x;
            float dy = playerPos.y - ballPos.y;
            float distSq = dx * dx + dy * dy;
            float minDist = playerRadius + ballRadius;

            if (distSq <= minDist * minDist) {
                // Speed boost
                playerSpeedMultiplier += SPEED_BOOST_PER_BALL;
                Gdx.app.log("GameScene", "Speed boost! Multiplier now: " +
                    String.format("%.2f", playerSpeedMultiplier) +
                    " (collected " + ball.getName() + ")");

                // Disappear
                ball.setActive(false);
            }
        }
    }

    /**
     * Manually bounces the magenta follower ball off green squares (with ding sound).
     * The follower is not registered with the collision system so it can pass through the player.
     */
    private void checkFollowerGreenSquareBounce() {
        if (followerBall == null || !followerBall.isActive()) return;

        TransformComponent fTransform = followerBall.getComponent(TransformComponent.class);
        if (fTransform == null) return;
        Vector2 fPos = fTransform.getPosition();
        float fRadius = followerBall.getCollisionRadius();

        for (GameEntity square : greenSquares) {
            if (!square.isActive()) continue;

            TransformComponent sqTransform = square.getComponent(TransformComponent.class);
            if (sqTransform == null) continue;
            Vector2 sqPos = sqTransform.getPosition();
            float sqRadius = square.getCollisionRadius();

            float dx = fPos.x - sqPos.x;
            float dy = fPos.y - sqPos.y;
            float distSq = dx * dx + dy * dy;
            float minDist = fRadius + sqRadius;

            if (distSq < minDist * minDist) {
                float dist = (float) Math.sqrt(distSq);
                if (dist < 0.001f) dist = 0.001f;

                // Push the follower out of the square
                float nx = dx / dist;
                float ny = dy / dist;
                float penetration = minDist - dist;
                fPos.x += nx * penetration;
                fPos.y += ny * penetration;
            }
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

            // Reflect physics velocity at walls
            PhysicComponent p = entity.getComponent(PhysicComponent.class);
            if (p != null) {
                Vector2 vel = p.getVelocity();

                if (hitLeft  && vel.x < 0f) vel.x = -vel.x;
                if (hitRight && vel.x > 0f) vel.x = -vel.x;
                if (hitBottom && vel.y < 0f) vel.y = -vel.y;
                if (hitTop    && vel.y > 0f) vel.y = -vel.y;
            }

            // Reflect linear movement direction at walls
            MovementComponent mc = entity.getComponent(MovementComponent.class);
            if (mc != null) {
                MovementBehaviour behaviour = mc.getMovementBehaviour();
                if (behaviour instanceof LinearMovement) {
                    LinearMovement linear = (LinearMovement) behaviour;
                    Vector2 dir = linear.getDirection();
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

        GameEntity p = named.get("Player");
        if (p != null) {
            TransformComponent t = p.getComponent(TransformComponent.class);
            if (t != null) {
                Vector2 pos = t.getPosition();
                Gdx.app.log("ECS", "Player position: (" +
                    String.format("%.1f", pos.x) + ", " +
                    String.format("%.1f", pos.y) + ")");
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

        // Render ALL active entities using RenderComponent (Strategy Pattern)
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
