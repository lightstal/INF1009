package io.github.INF1009_P10_Team7.engine.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.INF1009_P10_Team7.engine.collision.IWorldCollisionQuery;

/**
 * Runtime contract for level-map loading, queries, and rendering.
 *
 * <p>Scenes should rely on this abstraction instead of owning map loader and
 * collision parsing details.</p>
 */
public interface ILevelMapRuntime {
    void load();
    IWorldCollisionQuery getCollisionQuery();
    int[][] getTerminalTiles();
    float getExitX();
    float getExitY();
    TextureRegion getDoorClosedRegion();
    TextureRegion getDoorOpenedRegion();
    void render(OrthographicCamera camera);
    void dispose();
}
