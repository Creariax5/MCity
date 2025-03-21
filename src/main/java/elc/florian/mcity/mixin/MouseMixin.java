package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import elc.florian.mcity.client.CustomRayCast;
import elc.florian.mcity.client.Zoom;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("RETURN"), method = "onMouseScroll(JDD)V")
    private void onOnMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci)
    {
        if (MCity.detached) {
            if (MCity.cam.isZooming()) {
                MCity.cam.setSpeed((int) (MCity.cam.getSpeed() + vertical));
                return;
            }

            Zoom.zoom(vertical);
        }
    }

    @Inject(at = @At("HEAD"), method = "onMouseButton(JIII)V")
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci)
    {
        if (MCity.detached) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                MCity.mouse_middle_pressed = action == 1;
                MCity.newDeplace = true;
                return;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == 1) {
                HitResult hit = CustomRayCast.throwRay((int) MCity.mouseX, (int) MCity.mouseY);
                BlockHitResult blockHit = (BlockHitResult) hit;
                BlockPos blockPos = blockHit.getBlockPos();
                MinecraftClient.getInstance().interactionManager.breakBlock(blockPos);
                return;
            }

        }
    }
    @Shadow
    private double x;
    @Shadow
    private double y;
}