package io.github.INF1009_P10_Team7.engine.inputoutput;

//import java.util.List;
//import java.util.Map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
//import com.badlogic.gdx.utils.ScreenUtils;

public class VisualOutput {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    
    public VisualOutput() {
	    // Constructor for initializing the components
    	this.batch = new SpriteBatch();
    	this.shapeRenderer = new ShapeRenderer();
    }

//    public void render(List<Entity> entities) {
//        // Implementation for rendering entities
//    	ScreenUtils.clear(0, 0, 0, 1);
//
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        for (Entity e : entities) {
//            e.draw(shapeRenderer); 
//        }
//        shapeRenderer.end();
//
//        batch.begin();
//        for (Entity e : entities) {
//            e.draw(batch); 
//        }
//        batch.end();
//    }

    public void dispose() {
        batch.dispose();	
        shapeRenderer.dispose();
    }
}