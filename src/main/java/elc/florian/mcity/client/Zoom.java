package elc.florian.mcity.client;

import elc.florian.mcity.state.CameraState;
import elc.florian.mcity.state.InputState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.toRadians;

public class Zoom {
    static int fluidite = 10;
    static int ground = 60;
    static int slowing = 3;

    private static Vec2f rotateY(Vec2f vector, double angle) {
        float x1 = (float) (vector.x * Math.cos(angle) - vector.y * Math.sin(angle));
        float y1 = (float) (vector.x * Math.sin(angle) + vector.y * Math.cos(angle));
        return new Vec2f(x1, y1);
    }

    public static void zoom(double speed) {
        CameraState.cam.setSpeed(0);
        CameraState.cam.setZooming(true);

        Vec3d cam = CameraState.cam.getPos();
        Vec3d dir = CameraState.cam.getDir();

        Camera newCam = new Camera(CameraState.cam);
        newCam.setPitch(90);
        ground = (int) CustomRayCast.throwRayToCenter().getPos().y;

        double height = Math.max(1, cam.getY() - ground);
        speed = speed * height / slowing;

        Timer timer = new Timer();

        double finalVertical = speed/fluidite;
        timer.schedule(new TimerTask() {
            int i = 0;
            Vec3d deplacement = new Vec3d(cam.getX()+dir.getX()* finalVertical, cam.getY()+dir.getY()* finalVertical, cam.getZ()+dir.getZ()* finalVertical);
            @Override
            public void run() {
                if (i == fluidite) {
                    CameraState.cam.setZooming(false);
                    if (CameraState.cam.getSpeed() != 0) {
                        zoom(CameraState.cam.getSpeed());

                    }
                    timer.cancel();
                    return;

                }
                if (deplacement.y<ground+3 && finalVertical > 0) {
                    CameraState.cam.setSpeed(0);
                    CameraState.cam.setZooming(false);
                    timer.cancel();
                    return;
                }
                if (deplacement.y>ground+10000 && finalVertical < 0) {
                    CameraState.cam.setSpeed(0);
                    CameraState.cam.setZooming(false);
                    timer.cancel();
                    return;
                }
                i++;
                CameraState.cam.setPos(deplacement);
                deplacement = new Vec3d(
                        deplacement.getX() + dir.getX() * finalVertical,
                        deplacement.getY() + dir.getY() * finalVertical,
                        deplacement.getZ() + dir.getZ() * finalVertical);
            }
        }, 0, 10);

    }

    static int mouseCap = 1;
    static float x = 1;
    static float y = 1;

    public static void setX(float x) {
        Zoom.x = x;
    }

    public static void setY(float y) {
        Zoom.y = y;
    }

    public static void move() {
        Timer timer = new Timer();

        Vec2f mouseDir = new Vec2f(x, y).normalize();
        mouseDir = rotateY(mouseDir, toRadians(CameraState.cam.getYaw()));
        Vec3d pos = CameraState.cam.getPos();
        double height = Math.max(10, pos.getY() - ground);
        mouseDir = mouseDir.multiply((float) (height / 60));

        Vec2f finalMouseDir = mouseDir;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                CameraState.cam.setPos(new Vec3d(pos.getX() - finalMouseDir.x, pos.getY(), pos.getZ() - finalMouseDir.y));

                if (InputState.mouseMoving || InputState.isKeyMoving()) {
                    move();
                }
            }
        }, 10);

    }

    public static boolean mouseAtBorder(double x, double y) {
        if (y > (double) (MinecraftClient.getInstance().getWindow().getHeight() /2)-mouseCap) {
            return true;

        }
        if (y < mouseCap) {
            return true;

        }
        if (x > (double) (MinecraftClient.getInstance().getWindow().getWidth() /2)-mouseCap) {
            return true;

        }
        if (x < mouseCap) {
            return true;

        }
        return false;
    }
}
