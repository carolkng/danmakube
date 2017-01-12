package com.carolkng.danmakube;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

import static java.lang.Math.PI;

/**
 * Created by home on 2016-12-31.
 */
public class Bullet implements Pool.Poolable {
    public Vector2 position;
    private Vector2 velocity;
    public boolean alive;

    public Bullet() {
        this.position = new Vector2();
        this.velocity = new Vector2();
        this.alive = false;
    }

    /**
     * Initialize the bullet with x, y velocity
     */
    public void init(float posX, float posY, float velX, float velY) {
        position.set(posX, posY);
        velocity.set(velX, velY);
        alive = true;
    }

    public void init(float posX, float posY, float vel, double angle, String angleType) {
        position.set(posX, posY);
        if (angleType.equals("radians")) {
            velocity.set((float)(vel * Math.cos(angle)), (float)(vel * Math.sin(angle)));
        } else if (angleType.equals("degrees")) {
            angle *= PI/180;
            velocity.set((float)(vel * Math.cos(angle)), (float)(vel * Math.sin(angle)));
        }
        alive = true;
    }

    /**
     * Automatically called by Pool.free()
     * Must reset every meaningful field of the bullet.
     */
    @Override
    public void reset() {
        position.set(-60,-60);
        velocity.set(0,0);
        alive = false;
    }

    public void update (float delta) {
        position.add(0,-5);
        if (isOutOfScreen()) alive = false;
    }

    private boolean isOutOfScreen() {
        return position.x < 0 || position.y < 0 || position.x > 480 || position.y > 800;
    }
}
