package elc.florian.mcity.client;

import elc.florian.mcity.MCity;
import elc.florian.mcity.gui.EditingGUI;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import net.minecraft.text.Text;

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

                double x = client.player.getX();
                double y = client.player.getY();
                double z = client.player.getZ();

                Vec3d playerPos = new Vec3d(x, y + 100, z);
                MCity.cam.setPos(playerPos);

                client.setScreen(new EditingGUI(Text.of("hi")));
            }
        });

    }
}
