package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import elc.florian.mcity.client.Camera;
import elc.florian.mcity.client.CustomRayCast;
import elc.florian.mcity.client.Zoom;
import net.minecraft.client.Keyboard;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardHandlerMixin {
    @Unique
    private void move_with_key(int x, int y) {
        Zoom.setX((float) (x));
        Zoom.setY((float) (y));

        Camera newCam = new Camera(MCity.cam);
        newCam.setPitch(90);
        int ground = (int) CustomRayCast.throwRayToCenter().getPos().y;

        while (ground + 2 >= newCam.getPos().getY()) {
            newCam.setPos(new Vec3d(newCam.getPos().getX(), newCam.getPos().getY() + 1, newCam.getPos().getZ()));
            ground = (int) CustomRayCast.throwRayToCenter().getPos().y;
        }
        MCity.cam.setPos(newCam.getPos());

        Zoom.move();
    }

    @Inject(at = @At("HEAD"), method = "onKey")
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (!MCity.detached) {
            return;
        }

        if (action == GLFW.GLFW_PRESS) {
            // Handle key press
            if (key == GLFW.GLFW_KEY_W) {
                MCity.keyW_pressed = true;
            }
            if (key == GLFW.GLFW_KEY_A) {
                MCity.keyA_pressed = true;
            }
            if (key == GLFW.GLFW_KEY_S) {
                MCity.keyS_pressed = true;
            }
            if (key == GLFW.GLFW_KEY_D) {
                MCity.keyD_pressed = true;
            }
            int x = 0;
            int y = 0;
            if (MCity.keyW_pressed) {
                x--;
            }
            if (MCity.keyA_pressed) {
                y--;
            }
            if (MCity.keyS_pressed) {
                x++;
            }
            if (MCity.keyD_pressed) {
                y++;
            }
            move_with_key(y, x);
        } else if (action == GLFW.GLFW_RELEASE) {
            if (key == GLFW.GLFW_KEY_W) {
                MCity.keyW_pressed = false;
            }
            if (key == GLFW.GLFW_KEY_A) {
                MCity.keyA_pressed = false;
            }
            if (key == GLFW.GLFW_KEY_S) {
                MCity.keyS_pressed = false;
            }
            if (key == GLFW.GLFW_KEY_D) {
                MCity.keyD_pressed = false;
            }
        }
    }
}