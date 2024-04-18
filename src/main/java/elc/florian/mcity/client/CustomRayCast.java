package elc.florian.mcity.client;

import elc.florian.mcity.MCity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class CustomRayCast {
    protected static Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }
    public static HitResult raycast(Camera camera, Entity entity, int maxDistance, boolean includeFluids) {
        Vec3d vec3d = camera.getPos();
        Vec3d vec3d2 = getRotationVector(camera.getPitch(), camera.getYaw());
        Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
        return entity.getWorld().raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, entity));
    }
    public static HitResult throwRay() {
        MinecraftClient client = MinecraftClient.getInstance();

        int maxReach = 1000; //The farthest target the cameraEntity can detect
        boolean includeFluids = false; //Whether to detect fluids as blocks

        //HitResult hit = client.cameraEntity.raycast(maxReach, tickDelta, includeFluids);
        assert client.cameraEntity != null;
        HitResult hit = raycast(MCity.cam, client.cameraEntity, maxReach, includeFluids);

        switch(hit.getType()) {
            case MISS:
                //nothing near enough
                break;
            case BLOCK:
                BlockHitResult blockHit = (BlockHitResult) hit;
                BlockPos blockPos = blockHit.getBlockPos();
                BlockState blockState = client.world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                break;
            case ENTITY:
                EntityHitResult entityHit = (EntityHitResult) hit;
                Entity entity = entityHit.getEntity();
                break;
        }
        return hit;

    }




}
