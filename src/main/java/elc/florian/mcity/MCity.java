package elc.florian.mcity;

import elc.florian.mcity.client.Camera;
import elc.florian.mcity.client.Keybindings;
import elc.florian.mcity.item.ModItem;
import elc.florian.mcity.item.ModItemGroups;
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
	public static ArrayList<KeyBinding> keyBinding = new ArrayList<>();
	public static boolean detached = false;
	public static boolean mouse_middle_pressed = false;
	public static Camera cam = new Camera(new Vec3d(0.0, 100.0, 0.0));

	public enum ToolType { ROAD, AREA, WATER, ELECTRICITY }
	public enum RoadType { PATH, ROAD, HIGHWAY }
	public enum AreaType { HABITATION, COMMERCE, INDUSTRIE, FERME }
	public enum WaterType { PUITS, CANALISATION, RESERVOIR }
	public enum ElectricityType { GENERATEUR, CABLE, TOUR_RELAIS }

	public static ToolType selectedTool = null;
	public static RoadType selectedRoadType = null;
	public static AreaType selectedAreaType = null;
	public static WaterType selectedWaterType = null;
	public static ElectricityType selectedElectricityType = null;
	public static boolean panelOpen = false;
	public static BlockPos lineFirstPoint = null;

	// Simulation
	public static boolean paused = false;
	public static int gameSpeed = 1; // 0=pause, 1=x1, 2=x2, 3=x3
	public static int gameDay = 1;
	public static int gameMonth = 1;
	public static int gameYear = 1;
	public static long money = 10000;
	public static int demandResidential = 50;  // 0-100
	public static int demandCommercial = 30;   // 0-100
	public static int demandIndustrial = 40;   // 0-100
	public static int happiness = 70;          // 0-100
	public static int population = 0;
	public static long lastTickTime = 0;
	public static long tickAccumulator = 0;
	private static final long TICK_INTERVAL_MS = 2000; // 1 jour toutes les 2 secondes à x1

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
		// Placeholder: léger revenu par jour
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

	public static double lastX;
	public static double lastY;
	public static double mouseX;
	public static double mouseY;
	public static boolean newDeplace;
	public static boolean mouseMoving;

	public static boolean keyW_pressed = false;
	public static boolean keyA_pressed = false;
	public static boolean keyS_pressed = false;
	public static boolean keyD_pressed = false;

	public static boolean isKeyMoving() {
		return keyW_pressed || keyA_pressed || keyS_pressed || keyD_pressed;
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		ModItem.registerModItems();
		ModItemGroups.registerItemGroups();

		Keybindings.registerKeybindings();
	}
}