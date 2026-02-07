package io.github.INF1009_P10_Team7.engine.entity.components;

import io.github.INF1009_P10_Team7.engine.entity.Component;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;


// Component that handles physics simulation for an entity.
// Stores velocity and mass, and updates the entitys transform based on physics.

public class PhysicComponent implements Component {
    private Vector2 velocity;
    private float mass;
    private Entity owner;


//    Creating a physicscomponent with default values i.e. 0 velocity and mass of 1.0
    public PhysicComponent() {
        this.velocity = new Vector2();
        this.mass = 1.0f;
    }


    //    Creating a physicscomponent with specified mass

    public PhysicComponent(float mass) {
        this.velocity = new Vector2();
        this.mass = mass;
    }

    //    Creating a physicscomponent with specified mass and velocity
    public PhysicComponent(Vector2 velocity, float mass) {
        this.velocity = new Vector2(velocity);
        this.mass = mass;
    }

    @Override
    public void onAdded(Entity owner) {
        this.owner = owner;
    }

    @Override
    public void onRemoved(Entity owner) {
        this.owner = null;
    }

    @Override
    public void update(float deltaTime) {
        if (owner == null) {
            return;
        }

        // Update position based on velocity
        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform != null) {
            Vector2 position = transform.getPosition();
            position.x += velocity.x * deltaTime;
            position.y += velocity.y * deltaTime;
        }
    }

    // Gets the velocity.
    public Vector2 getVelocity() {
        return velocity;
    }

    // Sets the velocity.
    public void setVelocity(Vector2 velocity) {
        this.velocity.set(velocity);
    }

    // Sets the velocity with x and y components.
    public void setVelocity(float x, float y) {
        this.velocity.set(x, y);
    }

    // Gets the mass.
    public float getMass() {
        return mass;
    }

    // Sets the mass.
    public void setMass(float mass) {
        this.mass = mass;
    }

    // Applies a force to the entity, changing its velocity based on mass.
    public void applyForce(Vector2 force) {
        if (mass > 0) {
            velocity.x += force.x / mass;
            velocity.y += force.y / mass;
        }
    }

    // Applies an impulse to the entity, instantly changing its velocity.
    public void applyImpulse(Vector2 impulse) {
        velocity.add(impulse);
    }

// Gets the owner entity.
    public Entity getOwner() {
        return owner;
    }
}
