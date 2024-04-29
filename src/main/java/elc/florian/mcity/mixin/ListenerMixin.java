package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import elc.florian.mcity.client.Camera;
import elc.florian.mcity.client.CustomRayCast;
import elc.florian.mcity.client.Zoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.navigation.Navigable;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Element.class)
public interface ListenerMixin extends Navigable {
    @Inject(at = @At("HEAD"), method = "mouseMoved(DD)V")
    private void onMouseMoved(double x, double y, CallbackInfo ci) {
        if (!MCity.detached) {
            return;
        }
        MCity.mouseX = x;
        MCity.mouseY = y;

        if (Zoom.mouseAtBorder(x, y)) {
            Zoom.setX((float) (x - MinecraftClient.getInstance().getWindow().getWidth() /4));
            Zoom.setY((float) (y - MinecraftClient.getInstance().getWindow().getHeight() /4));

            Camera newCam = new Camera(MCity.cam);
            newCam.setPitch(90);
            int ground = (int) CustomRayCast.throwRayToCenter().getPos().y;

            while (ground + 2 >= newCam.getPos().getY()) {
                newCam.setPos(new Vec3d(newCam.getPos().getX(), newCam.getPos().getY() + 1, newCam.getPos().getZ()));
                ground = (int) CustomRayCast.throwRayToCenter().getPos().y;
                System.out.println(newCam.getPos().getY());
                System.out.println("ground: " + ground + 2);
            }
            MCity.cam.setPos(newCam.getPos());

            if (!MCity.mouseMoving) {
                MCity.mouseMoving = true;

                Zoom.move();
            }
        } else {
            MCity.mouseMoving = false;
        }

        if (MCity.mouse_middle_pressed) {

            if (MCity.newDeplace) {
                MCity.lastX = x;
                MCity.lastY = y;
                MCity.newDeplace = false;

            }
            double moveX;
            double moveY;

            moveX = x - MCity.lastX;
            moveY = y - MCity.lastY;

            MCity.lastX = x;
            MCity.lastY = y;

            MCity.cam.setYaw((float) (MCity.cam.getYaw() + moveX));
            MCity.cam.setPitch((float) (MCity.cam.getPitch() + moveY));

            MCity.cam.updateDir();
        }
    }

    /*@Inject(at = @At("HEAD"), method = "keyPressed(III)Z")
    default void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (MCity.mouse_middle_pressed) {
            if (keyCode == GLFW.GLFW_KEY_W) {
                Vec3d dir = MCity.cam.getDir();
                Vec3d pos = MCity.cam.getPos();
                System.out.println("hiiiii");

                MCity.cam.setPos(new Vec3d(pos.getX() + (dir.getX()/MCity.cam.getXz()), pos.getY(), pos.getZ() + (dir.getZ()/MCity.cam.getXz())));
            }
        }
    }*/
}
