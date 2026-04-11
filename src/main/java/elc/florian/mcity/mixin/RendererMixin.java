package elc.florian.mcity.mixin;
import elc.florian.mcity.state.InputState;
import elc.florian.mcity.state.CameraState;


import elc.florian.mcity.MCity;
import elc.florian.mcity.client.CustomRayCast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class RendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
            at = @At("HEAD")
    )
    private void replaceRaycast(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
                                Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (CameraState.detached) {
            MinecraftClient.getInstance().crosshairTarget = CustomRayCast.throwRay((int) InputState.mouseX, (int) InputState.mouseY);
        }
    }
}
