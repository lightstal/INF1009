package io.github.INF1009_P10_Team7.cyber;

import com.badlogic.gdx.graphics.Color;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.RenderComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TriangleRenderer;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import io.github.INF1009_P10_Team7.cyber.components.*;
import io.github.INF1009_P10_Team7.cyber.minigame.IMiniGame;
import io.github.INF1009_P10_Team7.cyber.drone.DroneAI;

public class CyberEntityFactory {

    public static GameEntity createPlayer(float x, float y, float radius, SpriteAnimator animator) {
        GameEntity player = new GameEntity("CyberPlayer");
        player.addComponent(new TransformComponent(x, y));
        player.addComponent(new PhysicComponent(new Vector2(0f, 0f), 1.0f));
        player.addComponent(new RenderComponent(new TriangleRenderer(radius), new Color(0.3f, 0.8f, 1.0f, 1f)));
        player.addComponent(new AnimatorComponent(animator, radius * 2.8f));
        player.setCollisionRadius(radius);
        return player;
    }

    public static GameEntity createDrone(int id, DroneAI aiLogic) {
        GameEntity drone = new GameEntity("Drone_" + id);
        drone.addComponent(new TransformComponent(aiLogic.getSpawnX(), aiLogic.getSpawnY()));
        drone.addComponent(new DroneComponent(aiLogic));
        return drone;
    }

    public static GameEntity createClue(int id, float x, float y, int col, int row, String clueId, String title, String desc, String objName) {
        GameEntity clue = new GameEntity("Clue_" + id);
        clue.addComponent(new TransformComponent(x, y));
        clue.addComponent(new ClueComponent(clueId, title, desc, objName, col, row));
        return clue;
    }

    public static GameEntity createTerminal(int id, float x, float y, IMiniGame minigame) {
        GameEntity terminal = new GameEntity("Terminal_" + id);
        terminal.addComponent(new TransformComponent(x, y));
        terminal.addComponent(new TerminalComponent(minigame, id));
        return terminal;
    }

    public static GameEntity createCCTV(int id, float x, float y, float baseAngle, float phaseOffset, IMapCollision mapCollision) {
        GameEntity cctv = new GameEntity("CCTV_" + id);
        cctv.addComponent(new TransformComponent(x, y));
        cctv.addComponent(new CCTVComponent(baseAngle, phaseOffset, mapCollision));
        return cctv;
    }

    public static GameEntity createExitDoor(float x, float y) {
        GameEntity door = new GameEntity("ExitDoor");
        door.addComponent(new TransformComponent(x, y));
        door.addComponent(new ExitDoorComponent());
        return door;
    }
}