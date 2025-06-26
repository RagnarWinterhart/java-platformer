package io.github.platform;

import com.badlogic.gdx.math.Vector2;


class TrailPoint {
    Vector2 position;
    float r, g, b;

    TrailPoint(Vector2 position, float r, float g, float b) {
        this.position = position;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
