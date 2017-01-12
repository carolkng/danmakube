package com.carolkng.danmakube;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Project: Danmakube
 * Created by Carol Ng (@carolkng) on 2017-01-12.
 */

public class BulletRectangle extends Rectangle {
    public Vector2 velocity = new Vector2();

    public void setVelocity(float velX, float velY) {
        velocity.set(velX, velY);
    }

    public void setVelocity(float vel, float inputAngle, String drg) {
        velocity.set(vel, 0);
        if (drg == "degrees") {
            velocity.setAngle(inputAngle);
        } else if (drg == "radians") {
            velocity.setAngleRad(inputAngle);
        } else {
            throw new IllegalArgumentException("Angle type must be one of 'degrees' or 'radians'.");
        }
    }

    public Vector2 getPosition() {
        return new Vector2(this.x, this.y);
    }
}
