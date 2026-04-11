package elc.florian.mcity.mixin;
import elc.florian.mcity.state.InputState;
import elc.florian.mcity.state.CameraState;


import elc.florian.mcity.MCity;
import elc.florian.mcity.client.Camera;
import elc.florian.mcity.client.CustomRayCast;
import elc.florian.mcity.client.Zoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.navigation.Navigable;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Element.class)
public interface ListenerMixin extends Navigable {
    @Inject(at = @At("HEAD"), method = "mouseMoved(DD)V")
    private void onMouseMoved(double x, double y, CallbackInfo ci) {
        if (!CameraState.detached) {
            return;
        }
        InputState.mouseX = x;
        InputState.mouseY = y;

        if (Zoom.mouseAtBorder(x, y)) {
            Zoom.setX((float) (x - MinecraftClient.getInstance().getWindow().getWidth() / 4));
            Zoom.setY((float) (y - MinecraftClient.getInstance().getWindow().getHeight() / 4));

            Camera newCam = new Camera(CameraState.cam);
            newCam.setPitch(90);
            int ground = (int) CustomRayCast.throwRayToCenter().getPos().y;

            while (ground + 2 >= newCam.getPos().getY()) {
                newCam.setPos(new Vec3d(newCam.getPos().getX(), newCam.getPos().getY() + 1, newCam.getPos().getZ()));
                ground = (int) CustomRayCast.throwRayToCenter().getPos().y;
            }
            CameraState.cam.setPos(newCam.getPos());

            if (!InputState.mouseMoving) {
                InputState.mouseMoving = true;

                Zoom.move();
            }
        } else {
            InputState.mouseMoving = false;
        }

        if (InputState.mouseMiddlePressed) {

            if (InputState.newDragStart) {
                InputState.lastX = x;
                InputState.lastY = y;
                InputState.newDragStart = false;

            }
            double moveX;
            double moveY;

            moveX = x - InputState.lastX;
            moveY = y - InputState.lastY;

            InputState.lastX = x;
            InputState.lastY = y;

            CameraState.cam.setYaw((float) (CameraState.cam.getYaw() + moveX));
            CameraState.cam.setPitch((float) (CameraState.cam.getPitch() + moveY));

            CameraState.cam.updateDir();
        }
    }
}