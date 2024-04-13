package elc.florian.mcity;

import elc.florian.mcity.client.Camera;
import elc.florian.mcity.client.Keybindings;
import elc.florian.mcity.item.ModItem;
import elc.florian.mcity.item.ModItemGroups;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class MCity implements ModInitializer {
	public static final String MOD_ID = "mcity";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ArrayList<KeyBinding> keyBinding = new ArrayList<>();
	public static boolean detached = false;
	public static boolean mouse_middle_pressed = false;
	public static Camera cam = new Camera(new Vec3d(0.0, 100.0, 0.0));

	public static double lastX;
	public static double lastY;
	public static boolean newDeplace;
	public static boolean mouseMoving;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		ModItemGroups.registerItemGroups();
		ModItem.registerModItems();

		Keybindings.registerKeybindings();
	}
}