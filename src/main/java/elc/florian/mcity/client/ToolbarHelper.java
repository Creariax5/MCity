package elc.florian.mcity.client;

import elc.florian.mcity.MCity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class ToolbarHelper {
    // Toolbar outils
    public static final int BUTTON_SIZE = 20;
    public static final int BUTTON_SPACING = 4;
    public static final int TOOLBAR_HEIGHT = 28;
    public static final MCity.ToolType[] TOOLS = MCity.ToolType.values();
    public static final Item[] TOOL_ITEMS = {Items.COBBLESTONE, Items.OAK_DOOR, Items.WATER_BUCKET, Items.REDSTONE};

    // Barre d'infos
    public static final int INFO_BAR_HEIGHT = 18;
    public static final int SPEED_BTN_SIZE = 14;
    public static final int DEMAND_BAR_WIDTH = 40;
    public static final int DEMAND_BAR_HEIGHT = 10;

    // Panneau sous-menu (générique)
    public static final int PANEL_ITEM_WIDTH = 90;
    public static final int PANEL_ITEM_HEIGHT = 20;
    public static final int PANEL_PADDING = 4;

    // Sous-types Route (une seule option)
    public static final String[] ROAD_NAMES = {"Route"};
    public static final int[] ROAD_COLORS = {0xFF666666};

    // Sous-types Zone
    public static final String[] AREA_NAMES = {"Habitation", "Commerce", "Industrie", "Dezonnage"};
    public static final int[] AREA_COLORS = {0xFF44AA44, 0xFF4488DD, 0xFFDDAA44, 0xFF888888};

    // Sous-types Eau
    public static final String[] WATER_NAMES = {"Puits", "Canalisation", "Reservoir"};
    public static final int[] WATER_COLORS = {0xFF3377AA, 0xFFCC8844, 0xFF446688};

    // Sous-types Electricité
    public static final String[] ELEC_NAMES = {"Generateur", "Cable", "Tour relais"};
    public static final int[] ELEC_COLORS = {0xFFAA3333, 0xFFCC4444, 0xFF884422};

    // Panneau d'action sur structure sélectionnée
    public static final int ACTION_BTN_WIDTH = 70;
    public static final int ACTION_BTN_HEIGHT = 18;
    public static final int ACTION_PANEL_PADDING = 4;

    // Positions
    public static int getInfoBarY(int screenHeight) {
        return screenHeight - INFO_BAR_HEIGHT;
    }

    public static int getToolbarY(int screenHeight) {
        return screenHeight - INFO_BAR_HEIGHT - TOOLBAR_HEIGHT;
    }

    public static int getToolbarStartX(int screenWidth) {
        int totalWidth = TOOLS.length * BUTTON_SIZE + (TOOLS.length - 1) * BUTTON_SPACING;
        return (screenWidth - totalWidth) / 2;
    }

    public static int getButtonY(int screenHeight) {
        return getToolbarY(screenHeight) + (TOOLBAR_HEIGHT - BUTTON_SIZE) / 2;
    }

    // Retourne les noms du panneau actif
    public static String[] getActiveNames() {
        if (MCity.selectedTool == null) return null;
        return switch (MCity.selectedTool) {
            case ROAD -> ROAD_NAMES;
            case AREA -> AREA_NAMES;
            case WATER -> WATER_NAMES;
            case ELECTRICITY -> ELEC_NAMES;
        };
    }

    public static int[] getActiveColors() {
        if (MCity.selectedTool == null) return null;
        return switch (MCity.selectedTool) {
            case ROAD -> ROAD_COLORS;
            case AREA -> AREA_COLORS;
            case WATER -> WATER_COLORS;
            case ELECTRICITY -> ELEC_COLORS;
        };
    }

    public static int getActiveSelectedIndex() {
        if (MCity.selectedTool == null) return -1;
        return switch (MCity.selectedTool) {
            case ROAD -> MCity.selectedRoadType == null ? -1 : MCity.selectedRoadType.ordinal();
            case AREA -> MCity.selectedAreaType == null ? -1 : MCity.selectedAreaType.ordinal();
            case WATER -> MCity.selectedWaterType == null ? -1 : MCity.selectedWaterType.ordinal();
            case ELECTRICITY -> MCity.selectedElectricityType == null ? -1 : MCity.selectedElectricityType.ordinal();
        };
    }

    private static void selectSubType(int index) {
        switch (MCity.selectedTool) {
            case ROAD -> {
                MCity.RoadType type = MCity.RoadType.values()[index];
                MCity.selectedRoadType = (MCity.selectedRoadType == type) ? null : type;
                MCity.lineFirstPoint = null;
            }
            case AREA -> {
                MCity.AreaType type = MCity.AreaType.values()[index];
                MCity.selectedAreaType = (MCity.selectedAreaType == type) ? null : type;
            }
            case WATER -> {
                MCity.WaterType type = MCity.WaterType.values()[index];
                MCity.selectedWaterType = (MCity.selectedWaterType == type) ? null : type;
            }
            case ELECTRICITY -> {
                MCity.ElectricityType type = MCity.ElectricityType.values()[index];
                MCity.selectedElectricityType = (MCity.selectedElectricityType == type) ? null : type;
            }
        }
    }

    private static void clearAllSubTypes() {
        MCity.selectedRoadType = null;
        MCity.selectedAreaType = null;
        MCity.selectedWaterType = null;
        MCity.selectedElectricityType = null;
        MCity.lineFirstPoint = null;
    }

    private static boolean hasActiveSubType() {
        return MCity.selectedRoadType != null
                || MCity.selectedAreaType != null
                || MCity.selectedWaterType != null
                || MCity.selectedElectricityType != null;
    }

    // Clic toolbar
    public static boolean handleToolbarClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !MCity.detached) return false;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        // Toggle fill mode (quand AREA est actif)
        if (MCity.selectedTool == MCity.ToolType.AREA && handleFillModeClick(mouseX, mouseY, width, height)) {
            return true;
        }

        // Clic sur le panneau en premier
        if (MCity.panelOpen) {
            if (handlePanelClick(mouseX, mouseY, width, height)) {
                return true;
            }
        }

        int startX = getToolbarStartX(width);
        int buttonY = getButtonY(height);

        for (int i = 0; i < TOOLS.length; i++) {
            int btnX = startX + i * (BUTTON_SIZE + BUTTON_SPACING);
            if (mouseX >= btnX && mouseX <= btnX + BUTTON_SIZE
                    && mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE) {

                if (MCity.selectedTool == TOOLS[i]) {
                    // Toggle : désélectionner
                    MCity.panelOpen = false;
                    MCity.selectedTool = null;
                    clearAllSubTypes();
                } else {
                    clearAllSubTypes();
                    MCity.selectedTool = TOOLS[i];
                    if (TOOLS[i] == MCity.ToolType.ROAD) {
                        // Route : auto-sélection (une seule option)
                        MCity.selectedRoadType = MCity.RoadType.ROAD;
                        MCity.panelOpen = false;
                    } else {
                        MCity.panelOpen = true;
                    }
                }
                return true;
            }
        }

        // Clic ailleurs → fermer panneau
        if (MCity.panelOpen) {
            MCity.panelOpen = false;
            if (!hasActiveSubType()) {
                MCity.selectedTool = null;
            }
            return true;
        }

        return false;
    }

    private static boolean handleFillModeClick(double mouseX, double mouseY, int width, int height) {
        int startX = getToolbarStartX(width);
        int buttonY = getButtonY(height);
        int toolbarEnd = startX + TOOLS.length * BUTTON_SIZE + (TOOLS.length - 1) * BUTTON_SPACING;
        int btnX = toolbarEnd + 15;
        int btnW = 60;

        if (mouseX >= btnX && mouseX <= btnX + btnW
                && mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE) {
            MCity.zoneFillMode = !MCity.zoneFillMode;
            return true;
        }
        return false;
    }

    private static boolean handlePanelClick(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        String[] names = getActiveNames();
        if (names == null) return false;

        int startX = getToolbarStartX(screenWidth);
        int buttonY = getButtonY(screenHeight);

        // Trouver la position X du bouton actif
        int toolIndex = MCity.selectedTool.ordinal();
        int btnX = startX + toolIndex * (BUTTON_SIZE + BUTTON_SPACING);
        int panelX = btnX - (PANEL_ITEM_WIDTH - BUTTON_SIZE) / 2;
        int panelH = names.length * (PANEL_ITEM_HEIGHT + PANEL_PADDING) + PANEL_PADDING;
        int panelY = buttonY - panelH - 5;

        for (int i = 0; i < names.length; i++) {
            int itemX = panelX + PANEL_PADDING;
            int itemY = panelY + PANEL_PADDING + i * (PANEL_ITEM_HEIGHT + PANEL_PADDING);
            int itemW = PANEL_ITEM_WIDTH - 2 * PANEL_PADDING;

            if (mouseX >= itemX && mouseX <= itemX + itemW
                    && mouseY >= itemY && mouseY <= itemY + PANEL_ITEM_HEIGHT) {
                selectSubType(i);
                MCity.panelOpen = false;
                return true;
            }
        }
        return false;
    }

    // Clic barre d'infos
    public static boolean handleInfoBarClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !MCity.detached) return false;

        int height = client.getWindow().getScaledHeight();
        int infoY = getInfoBarY(height);
        int speedX = 80;
        int speedBtnY = infoY + (INFO_BAR_HEIGHT - SPEED_BTN_SIZE) / 2;

        // Pause
        if (mouseX >= speedX && mouseX <= speedX + SPEED_BTN_SIZE
                && mouseY >= speedBtnY && mouseY <= speedBtnY + SPEED_BTN_SIZE) {
            MCity.paused = !MCity.paused;
            if (!MCity.paused && MCity.gameSpeed == 0) MCity.gameSpeed = 1;
            MCity.lastTickTime = 0;
            return true;
        }

        // x1, x2, x3
        for (int i = 0; i < 3; i++) {
            int sBtnX = speedX + SPEED_BTN_SIZE + 3 + i * (SPEED_BTN_SIZE + 2);
            if (mouseX >= sBtnX && mouseX <= sBtnX + SPEED_BTN_SIZE
                    && mouseY >= speedBtnY && mouseY <= speedBtnY + SPEED_BTN_SIZE) {
                MCity.gameSpeed = i + 1;
                MCity.paused = false;
                MCity.lastTickTime = 0;
                return true;
            }
        }

        return false;
    }

    // === Panneau d'action sur structure sélectionnée ===

    public static int[] getActionPanelLayout(int screenWidth, int screenHeight) {
        // Panneau centré horizontalement, juste au-dessus de la toolbar
        boolean isLine = MCity.selectedStructure.kind.isLine;
        int nbButtons = isLine ? 2 : 3; // Move, Rotate (sauf lignes), Delete
        int panelW = nbButtons * ACTION_BTN_WIDTH + (nbButtons + 1) * ACTION_PANEL_PADDING;
        int panelH = ACTION_BTN_HEIGHT + 2 * ACTION_PANEL_PADDING;
        int panelX = (screenWidth - panelW) / 2;
        int panelY = getToolbarY(screenHeight) - panelH - 5;
        return new int[]{panelX, panelY, panelW, panelH, nbButtons};
    }

    public static boolean handleActionPanelClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !MCity.detached || MCity.selectedStructure == null) return false;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int[] layout = getActionPanelLayout(width, height);
        int panelX = layout[0], panelY = layout[1], nbButtons = layout[4];

        int btnY = panelY + ACTION_PANEL_PADDING;
        boolean isLine = MCity.selectedStructure.kind.isLine;

        for (int i = 0; i < nbButtons; i++) {
            int btnX = panelX + ACTION_PANEL_PADDING + i * (ACTION_BTN_WIDTH + ACTION_PANEL_PADDING);
            if (mouseX >= btnX && mouseX <= btnX + ACTION_BTN_WIDTH
                    && mouseY >= btnY && mouseY <= btnY + ACTION_BTN_HEIGHT) {
                // Order: Move, (Rotate), Delete
                if (i == 0) {
                    MCity.moveMode = true;
                } else if (!isLine && i == 1) {
                    BuildingPlacer.rotateStructure(MCity.selectedStructure);
                } else {
                    BuildingPlacer.deleteStructure(MCity.selectedStructure);
                    MCity.selectedStructure = null;
                }
                return true;
            }
        }
        return false;
    }
}
