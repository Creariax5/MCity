package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import elc.florian.mcity.client.Zoom;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.navigation.Navigable;
import net.minecraft.world.BlockView;
import net.minecraft.world.HeightLimitView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BlockView.class)
public interface BlockViewMixin extends HeightLimitView, FabricBlockView {
    /*@Inject(at = @At("HEAD"), method = "mouseMoved(DD)V")
    private void onMouseMoved(double x, double y, CallbackInfo ci) {
        if (!MCity.detached) {
            return;
        }

        if (Zoom.mouseAtBorder(x, y)) {
            Zoom.setX((float) (x - MinecraftClient.getInstance().getWindow().getWidth() /4));
            Zoom.setY((float) (y - MinecraftClient.getInstance().getWindow().getHeight() /4));
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
    }*/

}
