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
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static java.lang.Math.toRadians;

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
        Vec3d pos = camera.getPos();
        Vec3d dir = camera.getDir();
        Vec3d end = pos.add(dir.x * maxDistance, dir.y * maxDistance, dir.z * maxDistance);
        return entity.getWorld().raycast(new RaycastContext(pos, end, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, entity));
    }
    public static HitResult throwRayToCenter() {
        MinecraftClient client = MinecraftClient.getInstance();

        int maxReach = 1000; //The farthest target the cameraEntity can detect
        boolean includeFluids = false; //Whether to detect fluids as blocks

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

    public static Vec3d newRotCam(MinecraftClient client, int x, int y) {
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        double fov = client.options.getFov().getValue();
        double angleSize = fov/height;

        //rot vec things
        Vector3f verticalRotationAxis = new Vector3f(MCity.cam.getDir().toVector3f());
        verticalRotationAxis.cross(new Vector3f(0, 1, 0));
        verticalRotationAxis.normalize();

        Vector3f horizontalRotationAxis = new Vector3f(MCity.cam.getDir().toVector3f());
        horizontalRotationAxis.cross(verticalRotationAxis);
        horizontalRotationAxis.normalize();

        verticalRotationAxis = new Vector3f(MCity.cam.getDir().toVector3f());
        verticalRotationAxis.cross(horizontalRotationAxis);
        //rot vec things

        //quaternions things
        float horizontalRotation = (float) toRadians((x - width/2f) * angleSize);
        float verticalRotation = (float) toRadians((y - height/2f) * angleSize);

        final Vector3f temp2 = new Vector3f(MCity.cam.getDir().toVector3f());
        temp2.rotate((new Quaternionf()).setAngleAxis(verticalRotation, verticalRotationAxis.x, verticalRotationAxis.y, verticalRotationAxis.z));
        temp2.rotate((new Quaternionf()).setAngleAxis(horizontalRotation, horizontalRotationAxis.x, horizontalRotationAxis.y, horizontalRotationAxis.z));
        //quaternions things

        return new Vec3d(temp2);
    }

    public static HitResult throwRay(int x, int y) {
        MinecraftClient client = MinecraftClient.getInstance();

        int maxReach = 1000; //The farthest target the cameraEntity can detect
        boolean includeFluids = false; //Whether to detect fluids as blocks

        Camera newCam = new Camera(MCity.cam);
        newCam.setDir(newRotCam(client, x, y));

        assert client.cameraEntity != null;
        HitResult hit = raycast(newCam, client.cameraEntity, maxReach, includeFluids);

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
