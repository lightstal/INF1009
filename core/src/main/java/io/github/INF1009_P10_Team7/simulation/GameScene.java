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
import io.github.INF1009_P10_Team7.engine.entity.IEntityQuery;
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
import java.util.Random;

/**
 * <p>The main gameplay scene for the simulation. Contains the player,
 * obstacles, collectibles, and AI-controlled entities.</p>
 *
 * <p>Demonstrates engine features:</p>
 * <ul>
 *   <li>{@code IEntityQuery.getByName()} — lookup entities by name instead of caching refs</li>
 *   <li>{@code PhysicComponent.applyImpulse()} — one-shot dash on SHOOT click</li>
 *   <li>{@code IMovementSystem.setBehavior()} — runtime movement strategy swap (R key toggles follower AI)</li>
 *   <li>{@code IMovementSystem.hasEntity()} — guard check before setBehavior</li>
 *   <li>{@code IMovementSystem.getBehavior()} — read current behavior for toggle logic</li>
 *   <li>{@code MovementComponent.setMovementBehaviour()} — sync component when switching behavior</li>
 *   <li>{@code TransformComponent.setRotation()} — spinning diamonds (green squares rendered rotated)</li>
 *   <li>{@code ICollisionSystem.unregisterCollidable()} — cleanup when yellow balls are collected</li>
 *   <li>{@code IEntitySystem.removeEntity()} — remove collected yellow balls from entity system</li>
 *   <li>{@code Vector2} utility methods — sub, scl, dot, dst, cpy, len, nor used throughout</li>
 *   <li>Custom {@code ICollisionResponse} — yellow balls use a custom pickup response (OCP)</li>
 * </ul>
 *
 * <p>SOLID Principles applied:</p>
 * <ul>
 *   <li>SRP: Each entity type creation is in its own method; collision responses are separate strategies</li>
 *   <li>OCP: Custom ICollisionResponse for pickup without modifying CollisionResolution</li>
 *   <li>LSP: All MovementBehaviour implementations are interchangeable at runtime</li>
 *   <li>ISP: Scene depends on narrow interfaces (IInputController, IAudioController, etc.)</li>
 *   <li>DIP: Scene depends on abstractions (interfaces), not concrete managers</li>
 * </ul>
 */
public class GameScene extends Scene {

    /** <p>Interface for querying entities by name or retrieving all entities.</p> */
    private final IEntityQuery entityQuery;

    /** <p>Interface for adding and removing entities from the entity system.</p> */
    private final IEntitySystem entitySystem;

    /** <p>Interface for registering and unregistering collidable entities.</p> */
    private final ICollisionSystem collisionSystem;

    /** <p>Interface for adding, removing, and swapping movement behaviours.</p> */
    private final IMovementSystem movementSystem;

    /** <p>Factory for creating scenes (used for navigation to settings/main menu).</p> */
    private final SceneFactory factory;

    /** <p>LibGDX shape renderer for drawing entities as primitives.</p> */
    private ShapeRenderer shapeRenderer;

    /** <p>Orthographic camera for 2D rendering.</p> */
    private OrthographicCamera camera;

    /** <p>Viewport that stretches the world to fill the window.</p> */
    private Viewport viewport;

    /** <p>Fixed virtual world width in pixels.</p> */
    private static final float WORLD_W = 800f;

    /** <p>Fixed virtual world height in pixels.</p> */
    private static final float WORLD_H = 480f;

    /** <p>Timer that tracks elapsed time for periodic entity position logging.</p> */
    private float logTimer = 0f;

    /** <p>Interval in seconds between entity position log messages.</p> */
    private static final float LOG_INTERVAL = 2.0f;

    /** <p>List of yellow ball entities that can be collected by the player.</p> */
    private final List<GameEntity> yellowBalls = new ArrayList<>();

    /** <p>List of green square entities that act as static obstacles.</p> */
    private final List<GameEntity> greenSquares = new ArrayList<>();

    /** <p>Multiplier applied to player speed, increases when yellow balls are collected.</p> */
    private float playerSpeedMultiplier = 1.0f;

    /** <p>Amount of speed boost gained per collected yellow ball.</p> */
    private static final float SPEED_BOOST_PER_BALL = 0.15f;

    /** <p>Tracks whether the follower ball is currently chasing the player or wandering.</p> */
    private boolean followerIsChasing = true;

    /**
     * <p>Constructs the GameScene with all required engine interfaces.
     * Dependencies are injected through the constructor (DIP).</p>
     *
     * @param input           the input controller for reading player input
     * @param audio           the audio controller for playing sounds and music
     * @param nav             the scene navigator for switching between scenes
     * @param entityQuery     the entity query interface for looking up entities by name
     * @param entitySystem    the entity system for adding/removing entities
     * @param collisionSystem the collision system for registering collidable entities
     * @param movementSystem  the movement system for managing entity movement behaviours
     * @param factory         the scene factory for creating new scenes during navigation
     */
    public GameScene(
        IInputController input,
        IAudioController audio,
        SceneNavigator nav,
        IEntityQuery entityQuery,
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

    /**
     * <p>Called when the scene is first loaded. Sets up the camera, viewport,
     * shape renderer, background music, and creates all game entities.</p>
     */
    @Override
    protected void onLoad() {
        Gdx.app.log("Scene", "GameScene loaded");

        // Set up the camera centred on the world
        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_W, WORLD_H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // Create the shape renderer if it doesn't already exist
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer();
        }

        // Centre the camera on the world
        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0);
        camera.update();

        // Set the background music for the game scene
        audio.setMusic("Music_Game.mp3");

        // ===== CREATE ENTITIES (SRP: each type in its own method) =====
        createPlayer();
        createGreenSquares();
        createYellowBalls();
        createRandomBall();
        createFollowerBall();

        Gdx.app.log("GameScene", "World locked at: " + WORLD_W + "x" + WORLD_H);
    }

    // ===== ENTITY CREATION (SRP: separated into focused methods) =====

    /**
     * <p>Creates the player entity — a blue triangle controlled by keyboard input
     * with physics-based velocity. Registers it with the entity, collision, and
     * movement systems.</p>
     *
     * <p>The player uses an {@link InputDrivenMovement} behaviour that delegates
     * to {@link PlayerMovement} for translating key presses into velocity.</p>
     */
    private void createPlayer() {
        // Create the player entity and attach components
        GameEntity player = new GameEntity("Player");
        player.addComponent(new TransformComponent(WORLD_W / 2f, WORLD_H / 2f));       // Start at centre
        player.addComponent(new PhysicComponent(new Vector2(0f, 0f), 1.0f));            // Zero initial velocity, mass 1
        player.addComponent(new RenderComponent(new TriangleRenderer(25f), new Color(0.2f, 0.6f, 1f, 1f))); // Blue triangle
        player.setCollisionRadius(25f);

        // Register with the entity system
        entitySystem.addEntity(player);

        // Custom collision response: play a sound then bounce (OCP — extends behaviour without modifying engine)
        ICollisionResponse bounceWithSound = (obj1, obj2, info) -> {
            audio.playSound("bell.mp3");
            CollisionResolution.BOUNCE.resolve(obj1, obj2, info);
        };
        collisionSystem.registerCollidable(player, bounceWithSound);

        // Wire up input-driven movement: PlayerMovement handles key-to-velocity mapping
        MovementHandler playerHandler = new PlayerMovement();
        MovementBehaviour inputDriven = new InputDrivenMovement(playerHandler, input);
        movementSystem.addEntity(player, inputDriven);
    }

    /**
     * <p>Creates 5 static green diamond entities at random positions.
     * Each is a square rotated 45 degrees to appear as a diamond shape.</p>
     *
     * <p>Demonstrates {@code TransformComponent.setRotation()} for rotating
     * the render shape without changing collision bounds.</p>
     */
    private void createGreenSquares() {
        Random rand = new Random();
        float padding = 60f; // Keep diamonds away from screen edges

        for (int i = 1; i <= 5; i++) {
            // Random position within padded bounds
            float rx = padding + rand.nextFloat() * (WORLD_W - 2 * padding);
            float ry = padding + rand.nextFloat() * (WORLD_H - 2 * padding);

            GameEntity square = new GameEntity("GreenSquare" + i);
            // Rotate 45 degrees so the square renders as a diamond
            square.addComponent(new TransformComponent(new Vector2(rx, ry), 45f));
            square.addComponent(new RenderComponent(new RectangleRenderer(55f, 55f), new Color(0.2f, 0.85f, 0.2f, 1f)));
            square.setCollisionRadius(38f);

            // Register with systems — diamonds use the default BOUNCE collision response
            entitySystem.addEntity(square);
            collisionSystem.registerCollidable(square, CollisionResolution.BOUNCE);
            greenSquares.add(square);
        }
    }

    /**
     * <p>Creates 5 yellow ball entities that move in straight lines, bounce off
     * green squares, and can be collected by the player for speed boosts.</p>
     *
     * <p>Uses a custom {@link ICollisionResponse} that does nothing (OCP) — the
     * actual pickup logic is handled in the scene update loop instead.</p>
     */
    private void createYellowBalls() {
        Random rand = new Random();
        float padding = 60f;

        // OCP: Custom collision response for pickup — no modification to CollisionResolution needed
        ICollisionResponse pickupResponse = (obj1, obj2, info) -> {
            // Intentionally empty — pickup logic handled in scene update
        };

        for (int i = 1; i <= 5; i++) {
            // Random starting position
            float rx = padding + rand.nextFloat() * (WORLD_W - 2 * padding);
            float ry = padding + rand.nextFloat() * (WORLD_H - 2 * padding);

            // Random movement direction, ensure it's not near-zero
            float dirX = rand.nextFloat() * 2f - 1f;
            float dirY = rand.nextFloat() * 2f - 1f;
            if (Math.abs(dirX) < 0.1f && Math.abs(dirY) < 0.1f) {
                dirX = 1f;
                dirY = 0.5f;
            }

            // Random speed between 80 and 140
            float speed = 80f + rand.nextFloat() * 60f;

            GameEntity yellowBall = new GameEntity("YellowBall" + i);
            yellowBall.addComponent(new TransformComponent(rx, ry));
            yellowBall.addComponent(new RenderComponent(new CircleRenderer(18f), new Color(1f, 1f, 0.2f, 1f)));
            yellowBall.setCollisionRadius(18f);

            // Attach linear movement behaviour for straight-line travel
            LinearMovement linearBehaviour = new LinearMovement(new Vector2(dirX, dirY), speed);
            yellowBall.addComponent(new MovementComponent(linearBehaviour));

            // Register with all systems
            entitySystem.addEntity(yellowBall);
            collisionSystem.registerCollidable(yellowBall, pickupResponse); // Custom pickup response (OCP)
            movementSystem.addEntity(yellowBall, linearBehaviour);
            yellowBalls.add(yellowBall);
        }
    }

    /**
     * <p>Creates a single orange ball that moves randomly using AI movement.
     * It bounces off other entities via the default BOUNCE collision response.</p>
     */
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

        // Register with systems — uses AImovement for random wandering
        entitySystem.addEntity(randomBall);
        collisionSystem.registerCollidable(randomBall, CollisionResolution.BOUNCE);
        movementSystem.addEntity(randomBall, new AImovement(70f));
    }

    /**
     * <p>Creates a magenta ball that follows the player using {@link FollowMovement}.
     * Its behaviour can be toggled at runtime with the R key between chasing
     * and random wandering (demonstrates runtime strategy swap via LSP).</p>
     *
     * <p>Demonstrates {@code IEntityQuery.getByName()} to look up the player
     * entity by name rather than storing a direct reference.</p>
     */
    private void createFollowerBall() {
        Random rand = new Random();
        float padding = 60f;

        // Look up the player by name (demonstrates getByName)
        GameEntity player = entityQuery.getByName("Player");

        GameEntity followerBall = new GameEntity("FollowerBall");
        followerBall.addComponent(new TransformComponent(
            padding + rand.nextFloat() * (WORLD_W - 2 * padding),
            padding + rand.nextFloat() * (WORLD_H - 2 * padding)
        ));
        followerBall.addComponent(new RenderComponent(new CircleRenderer(18f), new Color(0.9f, 0.2f, 0.7f, 1f)));
        followerBall.setCollisionRadius(18f);

        // Store the follow behaviour in a MovementComponent for later runtime swapping
        FollowMovement followBehaviour = new FollowMovement(player, 80f);
        followerBall.addComponent(new MovementComponent(followBehaviour));

        // Register with all systems
        entitySystem.addEntity(followerBall);
        collisionSystem.registerCollidable(followerBall, CollisionResolution.BOUNCE);
        movementSystem.addEntity(followerBall, followBehaviour);
    }

    /**
     * <p>Called every frame. Handles input for scene navigation, player dash,
     * follower AI toggling, collision checks, and diamond spinning.</p>
     *
     * @param delta time since last frame in seconds
     */
    @Override
    protected void onUpdate(float delta) {
        // Clear input processor to prevent Stage from intercepting game input
        if (Gdx.input.getInputProcessor() != null) {
            Gdx.input.setInputProcessor(null);
        }

        // Periodic entity position logging for debugging
        logTimer += delta;
        if (logTimer >= LOG_INTERVAL) {
            logTimer = 0f;
            logEntityPositions();
        }

        // --- Scene navigation ---
        // ESC opens settings (pushed on top of game scene)
        if (input.isActionJustPressed("SETTINGS")) {
            nav.pushScene(factory.createSettingsScene());
            return;
        }
        // BACKSPACE returns to main menu (replaces current scene)
        if (input.isActionJustPressed("BACK")) {
            nav.requestScene(factory.createMainMenuScene());
            return;
        }

        // Retrieve the player entity by name each frame
        GameEntity player = entityQuery.getByName("Player");
        if (player == null || !player.isActive()) return;

        PhysicComponent physics = player.getComponent(PhysicComponent.class);

        // --- Dash impulse on SHOOT (demonstrates applyImpulse) ---
        if (input.isActionJustPressed("SHOOT")) {
            audio.playSound("Sound_Boom.mp3");
            if (physics != null) {
                // Copy current velocity, normalise, and scale to create a dash impulse
                Vector2 currentVel = physics.getVelocity().cpy();
                if (currentVel.len() > 0.01f) {
                    Vector2 dashImpulse = currentVel.nor().scl(150f);
                    physics.applyImpulse(dashImpulse);
                    Gdx.app.log("GameScene", "Dash impulse applied: " + dashImpulse);
                }
            }
        }

        // Apply speed multiplier from collected yellow balls
        if (physics != null) {
            Vector2 vel = physics.getVelocity();
            vel.scl(playerSpeedMultiplier);
        }

        // --- Toggle follower AI behaviour with R key ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            toggleFollowerBehavior(player);
        }

        // Run custom scene-level collision checks
        checkYellowBallGreenSquareBounce();
        checkYellowBallPlayerPickup(player);
        checkFollowerGreenSquareBounce();

        // Spin the green diamonds continuously
        spinGreenDiamonds(delta);
    }

    /**
     * <p>Toggles the follower ball between {@link FollowMovement} (chasing player)
     * and {@link AImovement} (random wandering) at runtime.</p>
     *
     * <p>Demonstrates:</p>
     * <ul>
     *   <li>{@code IMovementSystem.hasEntity()} — guard check before operating</li>
     *   <li>{@code IMovementSystem.getBehavior()} — read current behavior for toggle logic</li>
     *   <li>{@code IMovementSystem.setBehavior()} — swap behavior at runtime</li>
     *   <li>{@code MovementComponent.setMovementBehaviour()} — keep component in sync</li>
     * </ul>
     *
     * <p>Strategy Pattern + LSP: behaviours are interchangeable at runtime.</p>
     *
     * @param player the player entity for the follower to track when chasing
     */
    private void toggleFollowerBehavior(GameEntity player) {
        // Look up the follower ball by name
        GameEntity follower = entityQuery.getByName("FollowerBall");
        if (follower == null || !follower.isActive()) return;

        // Guard check: make sure the follower is registered in the movement system
        if (!movementSystem.hasEntity(follower)) {
            Gdx.app.log("GameScene", "FollowerBall not in movement system");
            return;
        }

        // Read the current behavior to decide which one to swap to
        MovementBehaviour currentBehavior = movementSystem.getBehavior(follower);

        // Swap between chasing and random wandering
        MovementBehaviour newBehavior;
        if (followerIsChasing) {
            newBehavior = new AImovement(90f);
            Gdx.app.log("GameScene", "Follower switched to AI random wander");
        } else {
            newBehavior = new FollowMovement(player, 80f);
            Gdx.app.log("GameScene", "Follower switched to chase player");
        }
        followerIsChasing = !followerIsChasing;

        // Update the movement system with the new behaviour
        movementSystem.setBehavior(follower, newBehavior);

        // Keep the entity's MovementComponent in sync with the system
        MovementComponent mc = follower.getComponent(MovementComponent.class);
        if (mc != null) {
            mc.setMovementBehaviour(newBehavior);
        }
    }

    /**
     * <p>Rotates each green diamond slowly each frame.
     * Demonstrates continuous use of {@code TransformComponent.setRotation()}.</p>
     *
     * @param delta time since last frame in seconds
     */
    private void spinGreenDiamonds(float delta) {
        for (GameEntity square : greenSquares) {
            if (!square.isActive()) continue;
            TransformComponent tc = square.getComponent(TransformComponent.class);
            if (tc != null) {
                // Add 30 degrees per second of rotation
                tc.setRotation(tc.getRotation() + 30f * delta);
            }
        }
    }

    /**
     * <p>Checks for overlap between yellow balls and green squares, and reflects
     * the ball's direction when a collision is detected.</p>
     *
     * <p>Demonstrates {@code Vector2.dst()}, {@code .cpy()}, {@code .sub()},
     * {@code .nor()}, and {@code .dot()} for collision normal and reflection.</p>
     */
    private void checkYellowBallGreenSquareBounce() {
        for (GameEntity ball : yellowBalls) {
            if (!ball.isActive()) continue;

            TransformComponent ballTransform = ball.getComponent(TransformComponent.class);
            if (ballTransform == null) continue;
            Vector2 ballPos = ballTransform.getPosition();
            float ballRadius = ball.getCollisionRadius();

            // Get the linear movement behaviour to reflect its direction
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

                // Check distance between centres
                float distance = ballPos.dst(sqPos);
                float minDist = ballRadius + sqRadius;

                if (distance < minDist) {
                    if (distance < 0.001f) distance = 0.001f; // Prevent division by zero

                    // Calculate collision normal (direction from square to ball)
                    Vector2 normal = ballPos.cpy().sub(sqPos).nor();

                    // Push ball out of the overlap
                    float penetration = minDist - distance;
                    ballPos.add(normal.x * penetration, normal.y * penetration);

                    // Reflect the ball's direction using the dot product
                    Vector2 dir = linear.getDirection();
                    float dotProduct = dir.dot(normal);
                    dir.x -= 2f * dotProduct * normal.x;
                    dir.y -= 2f * dotProduct * normal.y;
                }
            }
        }
    }

    /**
     * <p>Checks if the player overlaps any yellow ball. On overlap the ball is
     * collected: removed from all systems and the player gains a speed boost.</p>
     *
     * <p>Demonstrates {@code unregisterCollidable()}, {@code removeEntity()} on
     * all systems, and {@code Vector2.dst()} for distance checking.</p>
     *
     * @param player the player entity to check against yellow balls
     */
    private void checkYellowBallPlayerPickup(GameEntity player) {
        if (player == null || !player.isActive()) return;

        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform == null) return;
        Vector2 playerPos = playerTransform.getPosition();
        float playerRadius = player.getCollisionRadius();

        // Iterate backwards so removal doesn't skip elements
        for (int i = yellowBalls.size() - 1; i >= 0; i--) {
            GameEntity ball = yellowBalls.get(i);
            if (!ball.isActive()) continue;

            TransformComponent ballTransform = ball.getComponent(TransformComponent.class);
            if (ballTransform == null) continue;
            Vector2 ballPos = ballTransform.getPosition();
            float ballRadius = ball.getCollisionRadius();

            // Check overlap using distance between centres
            float distance = playerPos.dst(ballPos);
            float minDist = playerRadius + ballRadius;

            if (distance <= minDist) {
                // Grant speed boost to the player
                playerSpeedMultiplier += SPEED_BOOST_PER_BALL;
                Gdx.app.log("GameScene", "Speed boost! Multiplier: " +
                    String.format("%.2f", playerSpeedMultiplier) +
                    " (collected " + ball.getName() + ")");

                // Fully remove the ball from all engine systems
                collisionSystem.unregisterCollidable(ball);  // Remove from collision system
                movementSystem.removeEntity(ball);           // Remove from movement system
                entitySystem.removeEntity(ball);             // Remove from entity system

                // Deactivate and remove from our local tracking list
                ball.setActive(false);
                yellowBalls.remove(i);
            }
        }
    }

    /**
     * <p>Bounces the follower ball away from green squares on overlap.
     * Pushes the follower out along the collision normal.</p>
     *
     * <p>Demonstrates {@code Vector2.cpy()}, {@code .sub()},
     * {@code .nor()}, and {@code .dst()}.</p>
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

                // Push the follower out of the overlap along the collision normal
                Vector2 normal = fPos.cpy().sub(sqPos).nor();
                float penetration = minDist - distance;
                fPos.add(normal.x * penetration, normal.y * penetration);
            }
        }
    }

    /**
     * <p>Called after onUpdate. Applies world boundary clamping to keep
     * all entities within the visible world area.</p>
     *
     * @param delta time since last frame in seconds
     */
    @Override
    protected void onLateUpdate(float delta) {
        applyBoundaries();
    }

    /**
     * <p>Clamps every entity's position to stay within the world bounds.
     * Reflects velocity and linear movement direction when an entity
     * hits a wall.</p>
     */
    private void applyBoundaries() {
        for (Entity entity : entityQuery.getAllEntities()) {
            if (entity == null || !entity.isActive()) continue;

            TransformComponent t = entity.getComponent(TransformComponent.class);
            if (t == null) continue;

            // Get collision radius (only available on GameEntity)
            float radius = 0f;
            if (entity instanceof GameEntity) {
                radius = ((GameEntity) entity).getCollisionRadius();
            }

            Vector2 pos = t.getPosition();
            boolean hitLeft = false, hitRight = false, hitBottom = false, hitTop = false;

            // Clamp position to world bounds
            if (pos.x - radius < 0f)           { pos.x = radius;               hitLeft = true; }
            else if (pos.x + radius > WORLD_W)  { pos.x = WORLD_W - radius;    hitRight = true; }

            if (pos.y - radius < 0f)           { pos.y = radius;               hitBottom = true; }
            else if (pos.y + radius > WORLD_H)  { pos.y = WORLD_H - radius;    hitTop = true; }

            boolean hitX = hitLeft || hitRight;
            boolean hitY = hitBottom || hitTop;
            if (!hitX && !hitY) continue;

            // Reflect physics velocity on boundary hit
            PhysicComponent p = entity.getComponent(PhysicComponent.class);
            if (p != null) {
                Vector2 vel = p.getVelocity();
                if (hitLeft  && vel.x < 0f) vel.x = -vel.x;
                if (hitRight && vel.x > 0f) vel.x = -vel.x;
                if (hitBottom && vel.y < 0f) vel.y = -vel.y;
                if (hitTop    && vel.y > 0f) vel.y = -vel.y;
            }

            // Reflect linear movement direction on boundary hit
            MovementComponent mc = entity.getComponent(MovementComponent.class);
            if (mc != null) {
                MovementBehaviour behaviour = mc.getMovementBehaviour();
                if (behaviour instanceof LinearMovement) {
                    LinearMovement linear = (LinearMovement) behaviour;
                    Vector2 dir = linear.getDirection();
                    if ((hitLeft && dir.x < 0f) || (hitRight && dir.x > 0f)) {
                        linear.reverseX();
                    }
                    if ((hitBottom && dir.y < 0f) || (hitTop && dir.y > 0f)) {
                        linear.reverseY();
                    }
                }
            }
        }
    }

    /**
     * <p>Logs the player's current position for debugging purposes.
     * Called periodically based on {@link #LOG_INTERVAL}.</p>
     */
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

    /**
     * <p>Renders all active entities using the shape renderer.
     * Clears the screen with a dark background, then iterates through
     * all entities and calls their RenderComponent to draw.</p>
     */
    @Override
    protected void onRender() {
        if (camera == null || shapeRenderer == null || viewport == null) return;

        // Apply viewport and update camera
        viewport.apply();
        camera.update();

        // Clear screen with dark background
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw all active entities
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

    /**
     * <p>Called when the window is resized. Updates the viewport to
     * maintain the correct aspect ratio.</p>
     *
     * @param width  new window width in pixels
     * @param height new window height in pixels
     */
    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    /** <p>Called when the scene is unloaded (before disposal).</p> */
    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "GameScene unloading...");
    }

    /** <p>Disposes of the shape renderer to free GPU resources.</p> */
    @Override
    protected void onDispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        Gdx.app.log("Scene", "GameScene disposed");
    }
}
