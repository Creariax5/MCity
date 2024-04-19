package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import elc.florian.mcity.client.CustomRayCast;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class RendererMixin {
    @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0))
    private HitResult injected(HitResult hit) {
        if (MCity.detached) {
            return CustomRayCast.throwRay((int) MCity.mouseX, (int) MCity.mouseY);
        } else {
            return hit;
        }
    }

}
