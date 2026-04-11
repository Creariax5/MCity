package elc.florian.mcity;

import elc.florian.mcity.client.Camera;
import elc.florian.mcity.client.Keybindings;
import elc.florian.mcity.item.ModItem;
import elc.florian.mcity.item.ModItemGroups;
import elc.florian.mcity.structure.PlacedStructure;
import elc.florian.mcity.structure.StructureRegistry;
import elc.florian.mcity.structure.ZoneRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.WorldSavePath;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class MCity implements ModInitializer {
	public static final String MOD_ID = "mcity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// === Tool types ===
	public enum ToolType { ROAD, AREA, WATER, ELECTRICITY }
	public enum RoadType { ROAD }
	public static final int ROAD_WIDTH = 8;
	public static final int ROAD_MIN_LENGTH = 16;
	public enum AreaType { HABITATION, COMMERCE, INDUSTRIE, DEZONNAGE }
	public enum WaterType { PUITS, CANALISATION, RESERVOIR }
	public enum ElectricityType { GENERATEUR, CABLE, TOUR_RELAIS }

	// === Tool state ===
	public static ToolType selectedTool = null;
	public static RoadType selectedRoadType = null;
	public static AreaType selectedAreaType = null;
	public static WaterType selectedWaterType = null;
	public static ElectricityType selectedElectricityType = null;
	public static boolean panelOpen = false;
	public static BlockPos lineFirstPoint = null;

	// === Sélection/édition de structures ===
	public static PlacedStructure selectedStructure = null;
	public static boolean moveMode = false;

	// === Zones ===
	public static boolean zoneFillMode = false; // false=tile, true=remplir

	// === Camera state ===
	public static boolean detached = false;
	public static Camera cam = new Camera(new Vec3d(0.0, 100.0, 0.0));
	public static ArrayList<KeyBinding> keyBinding = new ArrayList<>();

	// === Mouse state ===
	public static boolean mouseMiddlePressed = false;
	public static double lastX, lastY;
	public static double mouseX, mouseY;
	public static boolean newDragStart;
	public static boolean mouseMoving;

	// === Keyboard state ===
	public static boolean keyW_pressed = false;
	public static boolean keyA_pressed = false;
	public static boolean keyS_pressed = false;
	public static boolean keyD_pressed = false;

	public static boolean isKeyMoving() {
		return keyW_pressed || keyA_pressed || keyS_pressed || keyD_pressed;
	}

	// === Simulation ===
	public static boolean paused = false;
	public static int gameSpeed = 1;
	public static int gameDay = 1;
	public static int gameMonth = 1;
	public static int gameYear = 1;
	public static long money = 10000;
	public static int demandResidential = 50;
	public static int demandCommercial = 30;
	public static int demandIndustrial = 40;
	public static int happiness = 70;
	public static int population = 0;

	public static long lastTickTime = 0;
	private static long tickAccumulator = 0;
	private static final long TICK_INTERVAL_MS = 2000;

	public static void tickSimulation() {
		if (paused || gameSpeed == 0) return;

		long now = System.currentTimeMillis();
		if (lastTickTime == 0) {
			lastTickTime = now;
			return;
		}

		tickAccumulator += (now - lastTickTime) * gameSpeed;
		lastTickTime = now;

		while (tickAccumulator >= TICK_INTERVAL_MS) {
			tickAccumulator -= TICK_INTERVAL_MS;
			advanceDay();
		}
	}

	private static void advanceDay() {
		gameDay++;
		if (gameDay > 30) {
			gameDay = 1;
			gameMonth++;
			if (gameMonth > 12) {
				gameMonth = 1;
				gameYear++;
			}
		}
		money += 10;
	}

	public static String getDateString() {
		return String.format("%02d/%02d/An %d", gameDay, gameMonth, gameYear);
	}

	public static String getMoneyString() {
		return String.format("%,d $", money);
	}

	public static String getHappinessEmoji() {
		if (happiness >= 70) return ":)";
		if (happiness >= 40) return ":/";
		return ":(";
	}

	public static String getPopulationString() {
		return String.format("%,d hab.", population);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("MCity loading...");
		ModItem.registerModItems();
		ModItemGroups.registerItemGroups();
		Keybindings.registerKeybindings();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			java.nio.file.Path root = server.getSavePath(WorldSavePath.ROOT);
			StructureRegistry.load(root.resolve("mcity_structures.json"));
			ZoneRegistry.load(root.resolve("mcity_zones.json"));
			ZoneRegistry.markDirty();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			java.nio.file.Path root = server.getSavePath(WorldSavePath.ROOT);
			StructureRegistry.save(root.resolve("mcity_structures.json"));
			ZoneRegistry.save(root.resolve("mcity_zones.json"));
		});
	}
}
