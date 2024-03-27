package elc.florian.mcity.client;

import elc.florian.mcity.MCity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import static elc.florian.mcity.MCity.keyBinding;

public class Keybindings {
    public static void registerKeybindings() {
        keyBinding.add(
                KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.city_editor_view", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_F6, // The keycode of the key
                "category.city_editor_view" // The translation key of the keybinding's category.
        )));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.get(0).wasPressed()) {
                assert client.player != null;
                MCity.detached = !MCity.detached;
            }
        });


        keyBinding.add(
                KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.city_editor_view"
        )));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.get(1).wasPressed()) {
                if (MCity.detached) {
                    Vec3d cam = MCity.cam.getPos();
                    Vec3d dir = MCity.cam.getDir();
                    //System.out.println(dir.x + ", " + dir.y + ", " + dir.z);
                    //System.out.println(cam.x + ", " + cam.y + ", " + cam.z);
                    MCity.cam.setPos(cam.add(dir));
                }
            }
        });

        keyBinding.add(
                KeyBindingHelper.registerKeyBinding(new KeyBinding(
                        "key.dezoom",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        "category.city_editor_view"
                )));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.get(2).wasPressed()) {
                if (MCity.detached) {
                    Vec3d cam = MCity.cam.getPos();
                    Vec3d dir = MCity.cam.getDir();

                    MCity.cam.setPos(cam.subtract(dir));
                }
            }
        });

    }
}
