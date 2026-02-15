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
import io.github.INF1009_P10_Team7.engine.collision.ICollisionResponse;
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
import io.github.INF1009_P10_Team7.engine.collision.ICollisionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * GameScene (simulation layer demo)
 *
 * Demonstrates ALL engine features including previously unused methods:
 *
 * - EntityQuery.getByName()           — lookup entities by name instead of caching refs
 * - PhysicComponent.applyForce()      — continuous thrust on player (SPACE key)
 * - PhysicComponent.applyImpulse()    — one-shot dash on SHOOT click
 * - IMovementSystem.setBehavior()     — runtime movement strategy swap (R key toggles follower AI)
 * - IMovementSystem.hasEntity()       — guard check before setBehavior
 * - IMovementSystem.getBehavior()     — read current behavior for toggle logic
 * - MovementComponent.setMovementBehaviour() — sync component when switching behavior
 * - TransformComponent.setRotation()  — spinning diamonds (green squares rendered rotated)
 * - ICollisionSystem.unregisterCollidable() — cleanup when yellow balls are collected
 * - IEntitySystem.removeEntity()      — remove collected yellow balls from entity system
 * - Vector2 utility methods           — sub, scl, dot, dst, cpy, len, nor used throughout
 * - Custom ICollisionResponse         — yellow balls use a custom pickup response (OCP)
 *
 * SOLID Principles applied:
 * - SRP: Each entity type creation is in its own method; collision responses are separate strategies
 * - OCP: Custom ICollisionResponse for pickup without modifying CollisionResolution
 * - LSP: All MovementBehaviour implementations are interchangeable at runtime
 * - ISP: Scene depends on narrow interfaces (IInputController, IAudioController, etc.)
 * - DIP: Scene depends on abstractions (interfaces), not concrete managers
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

    private MovementHandler movementLogic;

    private float logTimer = 0f;
    private static final float LOG_INTERVAL = 2.0f;

    // Track yellow balls and green squares for manual collision logic
    private final List<GameEntity> yellowBalls = new ArrayList<>();
    private final List<GameEntity> greenSquares = new ArrayList<>();

    // Player speed boost tracking (from collected yellow balls)
    private float playerSpeedMultiplier = 1.0f;
    private static final float SPEED_BOOST_PER_BALL = 0.15f;

    // Follower behavior toggle state (demonstrates setBehavior at runtime)
    private boolean followerIsChasing = true;

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

        // ===== CREATE ENTITIES (SRP: each type in its own method) =====
        createPlayer();
        createGreenSquares();
        createYellowBalls();
        createRandomBall();
        createFollowerBall();

        movementLogic = new PlayerMovement();

        Gdx.app.log("GameScene", "World locked at: " + WORLD_W + "x" + WORLD_H);
    }

    // ===== ENTITY CREATION (SRP: separated into focused methods) =====

    /** Player: Blue Triangle — controlled by input, physics-based velocity. */
    private void createPlayer() {
        GameEntity player = new GameEntity("Player");
        player.addComponent(new TransformComponent(WORLD_W / 2f, WORLD_H / 2f));
        player.addComponent(new PhysicComponent(new Vector2(0f, 0f), 1.0f));
        player.addComponent(new RenderComponent(new TriangleRenderer(25f), new Color(0.2f, 0.6f, 1f, 1f)));
        player.setCollisionRadius(25f);

        entitySystem.addEntity(player);
        ICollisionResponse bounceWithSound = (obj1, obj2, info) -> {
            audio.playSound("bell.mp3");
            CollisionResolution.BOUNCE.resolve(obj1, obj2, info);
        };
        collisionSystem.registerCollidable(player, bounceWithSound);
        movementSystem.addEntity(player, null); // Physics-only (player handles velocity)
    }

    /** 5 Static Green Diamonds — demonstrates setRotation() for diamond shape. */
    private void createGreenSquares() {
        Random rand = new Random();
        float padding = 60f;

        for (int i = 1; i <= 5; i++) {
            float rx = padding + rand.nextFloat() * (WORLD_W - 2 * padding);
            float ry = padding + rand.nextFloat() * (WORLD_H - 2 * padding);

            GameEntity square = new GameEntity("GreenSquare" + i);
            // DEMONSTRATES: setRotation() — rotate 45 degrees to render as diamond
            square.addComponent(new TransformComponent(new Vector2(rx, ry), 45f));
            square.addComponent(new RenderComponent(new RectangleRenderer(55f, 55f), new Color(0.2f, 0.85f, 0.2f, 1f)));
            square.setCollisionRadius(38f);

            entitySystem.addEntity(square);
            collisionSystem.registerCollidable(square, CollisionResolution.BOUNCE);
            greenSquares.add(square);
        }
    }

    /** 5 Yellow Balls — linear movement, bounce off green squares, pickup by player. */
    private void createYellowBalls() {
        Random rand = new Random();
        float padding = 60f;

        // OCP: Custom collision response for pickup — no modification to CollisionResolution needed
        ICollisionResponse pickupResponse = (obj1, obj2, info) -> {
            // Intentionally empty — pickup logic handled in scene update
        };

        for (int i = 1; i <= 5; i++) {
            float rx = padding + rand.nextFloat() * (WORLD_W - 2 * padding);
            float ry = padding + rand.nextFloat() * (WORLD_H - 2 * padding);

            float dirX = rand.nextFloat() * 2f - 1f;
            float dirY = rand.nextFloat() * 2f - 1f;
            if (Math.abs(dirX) < 0.1f && Math.abs(dirY) < 0.1f) {
                dirX = 1f;
                dirY = 0.5f;
            }

            float speed = 80f + rand.nextFloat() * 60f;

            GameEntity yellowBall = new GameEntity("YellowBall" + i);
            yellowBall.addComponent(new TransformComponent(rx, ry));
            yellowBall.addComponent(new RenderComponent(new CircleRenderer(18f), new Color(1f, 1f, 0.2f, 1f)));
            yellowBall.setCollisionRadius(18f);

            LinearMovement linearBehaviour = new LinearMovement(new Vector2(dirX, dirY), speed);
            yellowBall.addComponent(new MovementComponent(linearBehaviour));

            entitySystem.addEntity(yellowBall);
            // Register with custom pickup response (OCP — new behavior without modifying engine)
            collisionSystem.registerCollidable(yellowBall, pickupResponse);
            movementSystem.addEntity(yellowBall, linearBehaviour);
            yellowBalls.add(yellowBall);
        }
    }

    /** 1 Orange Ball — AI random movement. */
    private void createRandomBall() {
        Random rand = new Random();
        float padding = 60f;

        GameEntity randomBall = new GameEntity("RandomBall");
        randomBall.addComponent(new TransformComponent(
            padding + rand.nextFloat() * (WORLD_W - 2 * padding),
            padding + rand.nextFloat() * (WORLD_H - 2 * padding)
        ));
        randomBall.addComponent(new RenderComponent(new CircleRenderer(18f), new Color(1f, 0.5f, 0.1f, 1f)));
        randomBall.setCollisionRadius(18f);

        entitySystem.addEntity(randomBall);
        collisionSystem.registerCollidable(randomBall, CollisionResolution.BOUNCE);
        movementSystem.addEntity(randomBall, new AImovement(70f));
    }

    /** 1 Magenta Ball — follows the player. Behavior can be toggled at runtime with R key. */
    private void createFollowerBall() {
        Random rand = new Random();
        float padding = 60f;

        // DEMONSTRATES: getByName() — lookup player entity by name
        GameEntity player = entityQuery.getByName("Player");

        GameEntity followerBall = new GameEntity("FollowerBall");
        followerBall.addComponent(new TransformComponent(
            padding + rand.nextFloat() * (WORLD_W - 2 * padding),
            padding + rand.nextFloat() * (WORLD_H - 2 * padding)
        ));
        followerBall.addComponent(new RenderComponent(new CircleRenderer(18f), new Color(0.9f, 0.2f, 0.7f, 1f)));
        followerBall.setCollisionRadius(18f);

        // Store the initial follow behavior in a MovementComponent for runtime swapping
        FollowMovement followBehaviour = new FollowMovement(player, 80f);
        followerBall.addComponent(new MovementComponent(followBehaviour));

        entitySystem.addEntity(followerBall);
        collisionSystem.registerCollidable(followerBall, CollisionResolution.BOUNCE);
        movementSystem.addEntity(followerBall, followBehaviour);
    }

    @Override
    protected void onUpdate(float delta) {
        if (Gdx.input.getInputProcessor() != null) {
            Gdx.input.setInputProcessor(null);
        }

        logTimer += delta;
        if (logTimer >= LOG_INTERVAL) {
            logTimer = 0f;
            logEntityPositions();
        }

        // Scene navigation
        if (input.isActionJustPressed("SETTINGS")) {
            nav.pushScene(factory.createSettingsScene());
            return;
        }
        if (input.isActionJustPressed("BACK")) {
            nav.requestScene(factory.createMainMenuScene());
            return;
        }

        // DEMONSTRATES: getByName() — retrieve player by name each frame
        GameEntity player = entityQuery.getByName("Player");
        if (player == null || !player.isActive()) return;

        PhysicComponent physics = player.getComponent(PhysicComponent.class);

        // --- DEMONSTRATES: applyImpulse() — one-shot dash on SHOOT click ---
        if (input.isActionJustPressed("SHOOT")) {
            audio.playSound("Sound_Boom.mp3");
            if (physics != null) {
                // DEMONSTRATES: Vector2.cpy(), .nor(), .scl() — create a dash impulse
                Vector2 currentVel = physics.getVelocity().cpy();
                if (currentVel.len() > 0.01f) {
                    Vector2 dashImpulse = currentVel.nor().scl(150f);
                    physics.applyImpulse(dashImpulse);
                    Gdx.app.log("GameScene", "Dash impulse applied: " + dashImpulse);
                }
            }
        }

        // --- DEMONSTRATES: applyForce() — continuous thrust with SPACE key ---
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && physics != null) {
            Vector2 thrust = new Vector2(0f, 500f);
            physics.applyForce(thrust);
        }

        // Handle player movement with speed multiplier from collected yellow balls
        if (movementLogic != null && physics != null) {
            movementLogic.handle(physics, input);
            Vector2 vel = physics.getVelocity();
            vel.scl(playerSpeedMultiplier); // DEMONSTRATES: Vector2.scl()
        }

        // --- DEMONSTRATES: setBehavior(), hasEntity(), getBehavior() — toggle follower AI ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            toggleFollowerBehavior(player);
        }

        // Custom scene-level collision checks
        checkYellowBallGreenSquareBounce();
        checkYellowBallPlayerPickup(player);
        checkFollowerGreenSquareBounce();

        // Spin the green diamonds (DEMONSTRATES: setRotation continuously)
        spinGreenDiamonds(delta);
    }

    /**
     * DEMONSTRATES: IMovementSystem.hasEntity(), getBehavior(), setBehavior()
     * and MovementComponent.setMovementBehaviour()
     *
     * Toggles follower ball between FollowMovement and AImovement.
     * Strategy Pattern + LSP: behaviors are interchangeable at runtime.
     */
    private void toggleFollowerBehavior(GameEntity player) {
        GameEntity follower = entityQuery.getByName("FollowerBall");
        if (follower == null || !follower.isActive()) return;

        // DEMONSTRATES: hasEntity() — guard check before operating
        if (!movementSystem.hasEntity(follower)) {
            Gdx.app.log("GameScene", "FollowerBall not in movement system");
            return;
        }

        // DEMONSTRATES: getBehavior() — read current behavior to decide swap
        MovementBehaviour currentBehavior = movementSystem.getBehavior(follower);

        MovementBehaviour newBehavior;
        if (followerIsChasing) {
            newBehavior = new AImovement(90f);
            Gdx.app.log("GameScene", "Follower switched to AI random wander");
        } else {
            newBehavior = new FollowMovement(player, 80f);
            Gdx.app.log("GameScene", "Follower switched to chase player");
        }
        followerIsChasing = !followerIsChasing;

        // DEMONSTRATES: setBehavior() — swap behavior at runtime in MovementManager
        movementSystem.setBehavior(follower, newBehavior);

        // DEMONSTRATES: setMovementBehaviour() — keep component in sync
        MovementComponent mc = follower.getComponent(MovementComponent.class);
        if (mc != null) {
            mc.setMovementBehaviour(newBehavior);
        }
    }

    /** DEMONSTRATES: setRotation() — green diamonds spin slowly. */
    private void spinGreenDiamonds(float delta) {
        for (GameEntity square : greenSquares) {
            if (!square.isActive()) continue;
            TransformComponent tc = square.getComponent(TransformComponent.class);
            if (tc != null) {
                tc.setRotation(tc.getRotation() + 30f * delta);
            }
        }
    }

    /**
     * Bounces yellow balls off green squares.
     * DEMONSTRATES: Vector2.dst(), .cpy(), .sub(), .nor(), .dot()
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

                // DEMONSTRATES: Vector2.dst()
                float distance = ballPos.dst(sqPos);
                float minDist = ballRadius + sqRadius;

                if (distance < minDist) {
                    if (distance < 0.001f) distance = 0.001f;

                    // DEMONSTRATES: Vector2.cpy(), .sub(), .nor()
                    Vector2 normal = ballPos.cpy().sub(sqPos).nor();

                    float penetration = minDist - distance;
                    ballPos.add(normal.x * penetration, normal.y * penetration);

                    // DEMONSTRATES: Vector2.dot()
                    Vector2 dir = linear.getDirection();
                    float dotProduct = dir.dot(normal);
                    dir.x -= 2f * dotProduct * normal.x;
                    dir.y -= 2f * dotProduct * normal.y;
                }
            }
        }
    }

    /**
     * Yellow ball overlap with player — grants speed boost, fully removes ball.
     * DEMONSTRATES: unregisterCollidable(), removeEntity() on all systems, Vector2.dst()
     */
    private void checkYellowBallPlayerPickup(GameEntity player) {
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

            // DEMONSTRATES: Vector2.dst()
            float distance = playerPos.dst(ballPos);
            float minDist = playerRadius + ballRadius;

            if (distance <= minDist) {
                playerSpeedMultiplier += SPEED_BOOST_PER_BALL;
                Gdx.app.log("GameScene", "Speed boost! Multiplier: " +
                    String.format("%.2f", playerSpeedMultiplier) +
                    " (collected " + ball.getName() + ")");

                // DEMONSTRATES: unregisterCollidable() — remove from collision system
                collisionSystem.unregisterCollidable(ball);
                // DEMONSTRATES: IMovementSystem.removeEntity()
                movementSystem.removeEntity(ball);
                // DEMONSTRATES: IEntitySystem.removeEntity()
                entitySystem.removeEntity(ball);

                ball.setActive(false);
                yellowBalls.remove(i);
            }
        }
    }

    /**
     * Bounces follower ball off green squares.
     * DEMONSTRATES: Vector2.cpy(), .sub(), .nor(), .dst()
     */
    private void checkFollowerGreenSquareBounce() {
        GameEntity followerBall = entityQuery.getByName("FollowerBall");
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

            float distance = fPos.dst(sqPos);
            float minDist = fRadius + sqRadius;

            if (distance < minDist) {
                if (distance < 0.001f) distance = 0.001f;

                Vector2 normal = fPos.cpy().sub(sqPos).nor();
                float penetration = minDist - distance;
                fPos.add(normal.x * penetration, normal.y * penetration);
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

            PhysicComponent p = entity.getComponent(PhysicComponent.class);
            if (p != null) {
                Vector2 vel = p.getVelocity();
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
                    if (hitLeft && dir.x < 0f)   dir.x = -dir.x;
                    if (hitRight && dir.x > 0f)  dir.x = -dir.x;
                    if (hitBottom && dir.y < 0f) dir.y = -dir.y;
                    if (hitTop && dir.y > 0f)    dir.y = -dir.y;
                }
            }
        }
    }

    private void logEntityPositions() {
        GameEntity p = entityQuery.getByName("Player");
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
