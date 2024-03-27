package elc.florian.mcity.client;

import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import static java.lang.Math.*;

public class Camera {
    private Vec3d pos;
    private Vec3d dir;
    private float pitch;
    private float yaw;

    public Vec3d getDir() {
        return dir;
    }

    public void setDir(Vec3d dir) {
        this.dir = dir;
    }

    public Vec3d getPos() {
        return pos;
    }

    public void setPos(Vec3d pos) {
        this.pos = pos;
    }

    public float getPitch() {
        return (float) toDegrees(pitch);
    }

    public void setPitch(float pitch) {
        this.pitch = (float) toRadians(pitch);
    }

    public float getYaw() {
        return (float) toDegrees(yaw);
    }

    public void setYaw(float yaw) {
        this.yaw = (float) toRadians(yaw);
    }

    public Camera(Vec3d pos) {
        this.pos = pos;
        this.pitch = (float) toRadians(0);
        this.yaw = (float) toRadians(0);

        double y = -Math.sin(pitch);
        double xz = Math.cos(pitch);
        double x = -xz * Math.sin(yaw);
        double z = xz * Math.cos(yaw);

        this.dir = new Vec3d(x, y, z);

        System.out.println(dir.x + ", " + dir.y + ", " + dir.z);

    }
}
