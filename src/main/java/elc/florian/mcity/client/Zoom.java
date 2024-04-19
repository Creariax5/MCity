package elc.florian.mcity.client;

import elc.florian.mcity.MCity;
import elc.florian.mcity.utils.Tools;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;
import static java.lang.Math.toRadians;

public class Zoom {
    static int fluidite = 10;
    static int ground = 60;
    static int slowing = 3;

    public static void zoom(double speed) {
        MCity.cam.setSpeed(0);
        MCity.cam.setZooming(true);

        Vec3d cam = MCity.cam.getPos();
        Vec3d dir = MCity.cam.getDir();

        Camera newCam = new Camera(MCity.cam);
        newCam.setPitch(90);
        ground = (int) CustomRayCast.throwRayToCenter().getPos().y;

        speed = speed * (cam.getY()-0)/slowing;

        Timer timer = new Timer();

        double finalVertical = speed/fluidite;
        timer.schedule(new TimerTask() {
            int i = 0;
            Vec3d deplacement = new Vec3d(cam.getX()+dir.getX()* finalVertical, cam.getY()+dir.getY()* finalVertical, cam.getZ()+dir.getZ()* finalVertical);
            @Override
            public void run() {
                if (i == fluidite) {
                    MCity.cam.setZooming(false);
                    if (MCity.cam.getSpeed() != 0) {
                        zoom(MCity.cam.getSpeed());

                    }
                    timer.cancel();
                    return;

                }
                if (deplacement.y<ground+3 && finalVertical > 0) {
                    MCity.cam.setSpeed(0);
                    MCity.cam.setZooming(false);
                    timer.cancel();
                    return;
                }
                if (deplacement.y>ground+10000 && finalVertical < 0) {
                    MCity.cam.setSpeed(0);
                    MCity.cam.setZooming(false);
                    timer.cancel();
                    return;
                }
                i++;
                MCity.cam.setPos(deplacement);
                double tmpY = abs(deplacement.getY()+dir.getY()* finalVertical);
                deplacement = new Vec3d(deplacement.getX()+dir.getX()* finalVertical, tmpY, deplacement.getZ()+dir.getZ()* finalVertical);
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
        mouseDir = Tools.rotateY(mouseDir, toRadians(MCity.cam.getYaw()));
        Vec3d pos = MCity.cam.getPos();
        mouseDir = mouseDir.multiply((float) ((pos.getY()-ground)/60));

        Vec2f finalMouseDir = mouseDir;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MCity.cam.setPos(new Vec3d(pos.getX() - finalMouseDir.x, pos.getY(), pos.getZ() - finalMouseDir.y));

                if (MCity.mouseMoving) {
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
