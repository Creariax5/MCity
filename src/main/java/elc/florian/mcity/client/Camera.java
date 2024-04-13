package elc.florian.mcity.client;

import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import static java.lang.Math.*;

public class Camera {
    private Vec3d pos;
    private double xz;
    private Vec3d dir;
    private float pitch;
    private float yaw;
    private boolean zooming;
    private int speed;
    private int pitchMin;
    private int pitchMax;

    public double getXz() {
        return xz;
    }
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
        if (pitch>this.pitchMax) {
            pitch=this.pitchMax;
        }
        
        if (pitch<this.pitchMin) {
            pitch=this.pitchMin;
        }

        this.pitch = (float) toRadians(pitch);
    }

    public float getYaw() {
        return (float) toDegrees(yaw);
    }

    public void setYaw(float yaw) {
        this.yaw = (float) toRadians(yaw);
    }

    public boolean isZooming() {
        return zooming;
    }

    public void setZooming(boolean zooming) {
        this.zooming = zooming;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void updateDir() {
        double y = -Math.sin(pitch);
        xz = abs(Math.cos(pitch));
        double z = xz * Math.cos(yaw);
        double x = xz * -Math.sin(yaw);

        this.dir = new Vec3d(x, y, z);
        this.pitchMax = 90;
        this.pitchMin = 30;
    }

    public Camera(Vec3d pos) {
        this.pos = pos;
        this.pitch = (float) toRadians(90);
        this.yaw = (float) toRadians(0);

        updateDir();

        this.zooming = false;
        this.speed = 0;
    }

}
