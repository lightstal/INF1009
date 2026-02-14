package io.github.INF1009_P10_Team7;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.Collision.CollisionManager;
import io.github.INF1009_P10_Team7.Entity.EntityManager;
import io.github.INF1009_P10_Team7.InputOutput.InputOutputManager;
import io.github.INF1009_P10_Team7.InputOutput.KeyboardDevice;
import io.github.INF1009_P10_Team7.InputOutput.MouseDevice;
import io.github.INF1009_P10_Team7.Movement.MovementManager;
import io.github.INF1009_P10_Team7.Scene.MainMenuScene;
import io.github.INF1009_P10_Team7.Scene.SceneManager;

public class GameMaster extends ApplicationAdapter {

    // OWN BOTH RENDERERS
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private InputOutputManager inputOutputManager;
    private EntityManager entityManager;
    private SceneManager sceneManager;
    private MovementManager movementManager;
    private CollisionManager collisionManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        Gdx.app.log("GameMaster", "SpriteBatch and ShapeRenderer Initialized");

        inputOutputManager = new InputOutputManager();
        Gdx.app.log("GameMaster", "InputOutputManager Initialized");
        
        inputOutputManager.addDevice(new KeyboardDevice());
        inputOutputManager.addDevice(new MouseDevice());
        
        entityManager = new EntityManager();
        Gdx.app.log("GameMaster", "EntityManager Initialized");
        
        movementManager = new MovementManager();
        Gdx.app.log("GameMaster", "MovementManager Initialized");
        
        collisionManager = new CollisionManager();
        Gdx.app.log("GameMaster", "CollisionManager Initialized");
        
        
		sceneManager = new SceneManager();
        Gdx.app.log("GameMaster", "SceneManager Initialized");
		sceneManager.setDependencies(inputOutputManager, inputOutputManager, entityManager, movementManager, collisionManager);        

        setupInputs();
        setupOutputs();

        sceneManager.setScene(new MainMenuScene());
        
        Gdx.app.log("GameMaster", "All Managers Initialized and MainMenuScene Set");
    }

    private void setupInputs() {
    	Gdx.app.log("GameMaster", "Configuring Input Bindings...");
    	inputOutputManager.bindKey("UP", Input.Keys.W);
        inputOutputManager.bindKey("DOWN", Input.Keys.S);
        inputOutputManager.bindKey("LEFT", Input.Keys.A);
        inputOutputManager.bindKey("RIGHT", Input.Keys.D);
        
        inputOutputManager.bindKey("SETTINGS", Input.Keys.ESCAPE);
        
        inputOutputManager.bindKey("VOLUME_UP", Input.Keys.UP);
        inputOutputManager.bindKey("VOLUME_DOWN", Input.Keys.DOWN);
        
        inputOutputManager.bindMouse("SHOOT", Input.Buttons.LEFT);
        Gdx.app.log("GameMaster", "Input Bindings Configuration Complete");
    }

    private void setupOutputs() {
    	Gdx.app.log("GameMaster", "Loading Audio Assets...");
        inputOutputManager.loadSound("bounce", "bell.mp3");
        inputOutputManager.loadSound("death", "Sound_Boom.mp3");
        Gdx.app.log("GameMaster", "Audio Assets Loaded Successfully");
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

		ScreenUtils.clear(0, 0, 0.2f, 1);

        inputOutputManager.update();
        
        sceneManager.update(dt);
        
        sceneManager.render(batch, shapeRenderer);
    }
    
    @Override
    public void resize(int width, int height) {
    	Gdx.app.log("GameMaster", "Window Resized to: " + width + "x" + height);
        if (sceneManager != null) {
            sceneManager.resize(width, height);
        }
    }

    @Override
    public void dispose() {
    	Gdx.app.log("GameMaster", "Disposing GameMaster Resources...");
        batch.dispose();
        shapeRenderer.dispose();
        sceneManager.dispose();
        inputOutputManager.dispose();
        Gdx.app.log("GameMaster", "GameMaster Shutdown Complete");
    }
}