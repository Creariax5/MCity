package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class RayCastMouse {
    @Inject(at = @At("HEAD"), method = "updateTargetedEntity")
    private void onOnMouseScroll(float tickDelta, CallbackInfo ci)
    {
        if (MCity.detached) {

        }
    }

}
