package elc.florian.mcity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import elc.florian.mcity.MCity;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraView {
    @Redirect(method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V"))
    private void injected(Camera instance, double x, double y, double z) {
        this.moveBy(-this.clipToSpace(80.0), 0.0, 0.0);
    }
    @Inject(method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", at = @At("TAIL"))
    private void injected(CallbackInfo ci, @Local(ordinal = 1) boolean inverseView) {
        if (MCity.detached) {
            Vec3d cam = MCity.cam.getPos();

            this.setRotation(MCity.cam.getYaw(), MCity.cam.getPitch());
            this.setPos(cam.x, cam.y, cam.z);
            this.moveBy(this.getPos().z-cam.z, this.getPos().y-cam.y, this.getPos().x-cam.x);

        }
    }

    @Shadow
    protected abstract void moveBy(double x, double y, double z);
    @Shadow
    protected abstract double clipToSpace(double x);
    @Shadow
    protected abstract void setPos(double x, double y, double z);
    @Shadow
    public abstract Vec3d getPos();
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);
    @Shadow
    private float pitch;
    @Shadow
    private float yaw;

}
