package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
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
        }
    }*/

}
