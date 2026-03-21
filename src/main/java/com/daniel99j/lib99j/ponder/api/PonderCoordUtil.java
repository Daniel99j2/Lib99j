package com.daniel99j.lib99j.ponder.api;

import net.minecraft.world.phys.Vec2;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class PonderCoordUtil {
    private static final int SIZE = 100;
    public static final Vector2i SCREEN_SIZE = new Vector2i(SIZE*16, SIZE*9);

    public static Vector2i relativeToPixels(Vec2 relative) {
        return new Vector2i((int) (SCREEN_SIZE.x*relative.x), (int) (SCREEN_SIZE.y*relative.y));
    }

    public static Vec2 pixelsToRelative(Vector2i pixels) {
        return new Vec2((float) pixels.x /SCREEN_SIZE.x, (float) pixels.y /SCREEN_SIZE.y);
    }

    public static Vec2 pixelsToWorld(Vector2i pixels) {
        Vec2 relative = pixelsToRelative(pixels);

        float normalSize = 0.31f/10;
        //old = old.add(new Vec2(-0.5f, -0.5f));
        var scale = new Vector3f(normalSize * 16, normalSize * 9, 0);

        Vec2 scaled = new Vec2(relative.x*scale.x, -relative.y*scale.y);

        scaled = scaled.add(new Vec2(-scale.x/2, scale.y/2));

        return scaled;
    }
}
