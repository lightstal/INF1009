//package io.github.INF1009_P10_Team7.engine.movement;
//
//import io.github.INF1009_P10_Team7.engine.entity.Entity;
//import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
//import com.badlogic.gdx.math.MathUtils;
//
//public class AIMovement implements MovementBehaviour {
//    private float speed;
//    private float timer = 0;
//    private float dx = 0, dy = 0;
//
//    public AIMovement(float speed) { this.speed = speed; }
//
//    @Override
//    public void move(Entity entity, float deltaTime) {
//        TransformComponent tc = entity.getComponent(TransformComponent.class);
//        timer += deltaTime;
//        if (timer > 1.0f) { // Change direction every 1 second
//            dx = MathUtils.random(-1f, 1f);
//            dy = MathUtils.random(-1f, 1f);
//            timer = 0;
//        }
//        if (tc != null) {
//            tc.getPosition().x += dx * speed * deltaTime;
//            tc.getPosition().y += dy * speed * deltaTime;
//        }
//    }
//}
