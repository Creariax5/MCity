package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import elc.florian.mcity.client.CustomRayCast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class RendererMixin {
    /**
     * Inject at the start of the render method to replace the crosshairTarget
     * before it's used by blockEntityRenderDispatcher.configure()
     */
    @Inject(
            method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
            at = @At("HEAD")
    )
    private void injectCustomRayCastBeforeRender(
            ObjectAllocator allocator,
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            Matrix4f positionMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        if (MCity.detached) {
            // Get the Minecraft client instance
            MinecraftClient client = MinecraftClient.getInstance();

            // Replace the crosshairTarget with our custom raycast
            client.crosshairTarget = CustomRayCast.throwRay((int) MCity.mouseX, (int) MCity.mouseY);
        }
    }
}
