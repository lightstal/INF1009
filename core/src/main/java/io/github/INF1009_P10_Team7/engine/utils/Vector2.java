package io.github.INF1009_P10_Team7.engine.utils;

/**
 * A 2D vector class for representing positions, velocities, and directions.
 * Provides common vector operations for physics and transform calculations.
 */
public class Vector2 {
    public float x;
    public float y;

    /**
     * Creates a zero vector (0, 0).
     */
    public Vector2() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Creates a vector with the specified x and y components.
     *
     * @param x The x component
     * @param y The y component
     */
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a copy of another vector.
     *
     * @param other The vector to copy
     */
    public Vector2(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Sets the x and y components of this vector.
     *
     * @param x The new x component
     * @param y The new y component
     * @return This vector for chaining
     */
    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets this vector to the values of another vector.
     *
     * @param other The vector to copy from
     * @return This vector for chaining
     */
    public Vector2 set(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    /**
     * Adds another vector to this vector.
     *
     * @param other The vector to add
     * @return This vector for chaining
     */
    public Vector2 add(Vector2 other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    /**
     * Adds the specified values to this vector.
     *
     * @param x The x value to add
     * @param y The y value to add
     * @return This vector for chaining
     */
    public Vector2 add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Subtracts another vector from this vector.
     *
     * @param other The vector to subtract
     * @return This vector for chaining
     */
    public Vector2 sub(Vector2 other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    /**
     * Multiplies this vector by a scalar.
     *
     * @param scalar The scalar to multiply by
     * @return This vector for chaining
     */
    public Vector2 scl(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    /**
     * Calculates the length (magnitude) of this vector.
     *
     * @return The length of this vector
     */
    public float len() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculates the squared length of this vector.
     * More efficient than len() when only comparing magnitudes.
     *
     * @return The squared length of this vector
     */
    public float len2() {
        return x * x + y * y;
    }

    /**
     * Normalizes this vector to unit length.
     *
     * @return This vector for chaining
     */
    public Vector2 nor() {
        float length = len();
        if (length != 0) {
            x /= length;
            y /= length;
        }
        return this;
    }

    /**
     * Calculates the dot product with another vector.
     *
     * @param other The other vector
     * @return The dot product
     */
    public float dot(Vector2 other) {
        return x * other.x + y * other.y;
    }

    /**
     * Calculates the distance to another vector.
     *
     * @param other The other vector
     * @return The distance between this vector and the other
     */
    public float dst(Vector2 other) {
        float dx = other.x - x;
        float dy = other.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Creates a copy of this vector.
     *
     * @return A new vector with the same components
     */
    public Vector2 cpy() {
        return new Vector2(this);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector2 other = (Vector2) obj;
        return Float.compare(other.x, x) == 0 && Float.compare(other.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        return result;
    }
}