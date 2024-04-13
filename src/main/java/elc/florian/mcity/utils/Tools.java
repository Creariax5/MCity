package elc.florian.mcity.utils;

import net.minecraft.util.math.Vec2f;

public class Tools {
    public static Vec2f rotateY(Vec2f vector, double angle) {
        float x1 = (float)(vector.x * Math.cos(angle) - vector.y * Math.sin(angle));

        float y1 = (float)(vector.x * Math.sin(angle) + vector.y * Math.cos(angle)) ;

        return new Vec2f(x1, y1);

    }

}
