package io.github.platform;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;



class Platform {
    Rectangle rect;
    Texture texture;

    Platform(Rectangle rect, Texture texture) {
        this.rect = rect;
        this.texture = texture;
    }
}

