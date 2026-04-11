package elc.florian.mcity.state;

import elc.florian.mcity.client.Camera;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

/** État de la caméra détachée et keybindings. */
public class CameraState {
    public static boolean detached = false;
    public static Camera cam = new Camera(new Vec3d(0.0, 100.0, 0.0));
    public static ArrayList<KeyBinding> keyBinding = new ArrayList<>();
}
