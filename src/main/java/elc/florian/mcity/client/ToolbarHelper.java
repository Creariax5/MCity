package elc.florian.mcity.client;

import elc.florian.mcity.state.CameraState;
import elc.florian.mcity.state.Simulation;
import elc.florian.mcity.state.Tools;
import elc.florian.mcity.tools.SubType;
import elc.florian.mcity.tools.Tool;
import elc.florian.mcity.tools.ToolRegistry;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class ToolbarHelper {
    // Toolbar outils
    public static final int BUTTON_SIZE = 20;
    public static final int BUTTON_SPACING = 4;
    public static final int TOOLBAR_HEIGHT = 28;

    // Barre d'infos
    public static final int INFO_BAR_HEIGHT = 18;
    public static final int SPEED_BTN_SIZE = 14;
    public static final int DEMAND_BAR_WIDTH = 40;
    public static final int DEMAND_BAR_HEIGHT = 10;

    // Panneau sous-menu
    public static final int PANEL_ITEM_WIDTH = 90;
    public static final int PANEL_ITEM_HEIGHT = 20;
    public static final int PANEL_PADDING = 4;

    // Panneau d'action sur structure sélectionnée
    public static final int ACTION_BTN_WIDTH = 70;
    public static final int ACTION_BTN_HEIGHT = 18;
    public static final int ACTION_PANEL_PADDING = 4;

    // === Layout ===

    public static int getInfoBarY(int screenHeight) { return screenHeight - INFO_BAR_HEIGHT; }

    public static int getToolbarY(int screenHeight) { return screenHeight - INFO_BAR_HEIGHT - TOOLBAR_HEIGHT; }

    public static int getToolbarStartX(int screenWidth) {
        int n = ToolRegistry.all().size();
        int totalWidth = n * BUTTON_SIZE + (n - 1) * BUTTON_SPACING;
        return (screenWidth - totalWidth) / 2;
    }

    public static int getButtonY(int screenHeight) {
        return getToolbarY(screenHeight) + (TOOLBAR_HEIGHT - BUTTON_SIZE) / 2;
    }

    // === Helpers de sélection ===

    public static Tool getActiveTool() {
        return Tools.selectedTool == null ? null : ToolRegistry.get(Tools.selectedTool);
    }

    public static List<SubType> getActiveSubTypes() {
        Tool t = getActiveTool();
        return t == null ? null : t.subTypes();
    }

    public static int getActiveSelectedIndex() {
        List<SubType> subs = getActiveSubTypes();
        if (subs == null || Tools.selectedSubType == null) return -1;
        return subs.indexOf(Tools.selectedSubType);
    }

    private static void clearSelection() {
        Tools.selectedSubType = null;
        Tools.lineFirstPoint = null;
    }

    // === Clics ===

    public static boolean handleToolbarClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !CameraState.detached) return false;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        // Toggle fill mode (quand AREA est actif)
        if (Tools.selectedTool == Tools.ToolType.AREA && handleFillModeClick(mouseX, mouseY, width, height)) {
            return true;
        }

        // Clic sur le panneau en premier
        if (Tools.panelOpen && handlePanelClick(mouseX, mouseY, width, height)) {
            return true;
        }

        int startX = getToolbarStartX(width);
        int buttonY = getButtonY(height);
        Tools.ToolType[] toolTypes = Tools.ToolType.values();

        for (int i = 0; i < toolTypes.length; i++) {
            int btnX = startX + i * (BUTTON_SIZE + BUTTON_SPACING);
            if (mouseX >= btnX && mouseX <= btnX + BUTTON_SIZE
                    && mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE) {

                if (Tools.selectedTool == toolTypes[i]) {
                    // Toggle : désélectionner
                    Tools.panelOpen = false;
                    Tools.selectedTool = null;
                    clearSelection();
                } else {
                    clearSelection();
                    Tools.selectedTool = toolTypes[i];
                    Tool tool = ToolRegistry.get(toolTypes[i]);
                    if (tool != null && tool.autoSelectSingleSubType()) {
                        Tools.selectedSubType = tool.subTypes().get(0);
                        Tools.panelOpen = false;
                    } else {
                        Tools.panelOpen = true;
                    }
                }
                return true;
            }
        }

        // Clic ailleurs → fermer panneau
        if (Tools.panelOpen) {
            Tools.panelOpen = false;
            if (Tools.selectedSubType == null) {
                Tools.selectedTool = null;
            }
            return true;
        }

        return false;
    }

    private static boolean handleFillModeClick(double mouseX, double mouseY, int width, int height) {
        int startX = getToolbarStartX(width);
        int buttonY = getButtonY(height);
        int n = ToolRegistry.all().size();
        int toolbarEnd = startX + n * BUTTON_SIZE + (n - 1) * BUTTON_SPACING;
        int btnX = toolbarEnd + 15;
        int btnW = 60;

        if (mouseX >= btnX && mouseX <= btnX + btnW
                && mouseY >= buttonY && mouseY <= buttonY + BUTTON_SIZE) {
            Tools.zoneFillMode = !Tools.zoneFillMode;
            return true;
        }
        return false;
    }

    private static boolean handlePanelClick(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        List<SubType> subs = getActiveSubTypes();
        if (subs == null) return false;

        int startX = getToolbarStartX(screenWidth);
        int buttonY = getButtonY(screenHeight);

        int toolIndex = Tools.selectedTool.ordinal();
        int btnX = startX + toolIndex * (BUTTON_SIZE + BUTTON_SPACING);
        int panelX = btnX - (PANEL_ITEM_WIDTH - BUTTON_SIZE) / 2;
        int panelH = subs.size() * (PANEL_ITEM_HEIGHT + PANEL_PADDING) + PANEL_PADDING;
        int panelY = buttonY - panelH - 5;

        for (int i = 0; i < subs.size(); i++) {
            int itemX = panelX + PANEL_PADDING;
            int itemY = panelY + PANEL_PADDING + i * (PANEL_ITEM_HEIGHT + PANEL_PADDING);
            int itemW = PANEL_ITEM_WIDTH - 2 * PANEL_PADDING;

            if (mouseX >= itemX && mouseX <= itemX + itemW
                    && mouseY >= itemY && mouseY <= itemY + PANEL_ITEM_HEIGHT) {
                SubType clicked = subs.get(i);
                Tools.selectedSubType = (Tools.selectedSubType == clicked) ? null : clicked;
                Tools.lineFirstPoint = null;
                Tools.panelOpen = false;
                return true;
            }
        }
        return false;
    }

    public static boolean handleInfoBarClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !CameraState.detached) return false;

        int height = client.getWindow().getScaledHeight();
        int infoY = getInfoBarY(height);
        int speedX = 80;
        int speedBtnY = infoY + (INFO_BAR_HEIGHT - SPEED_BTN_SIZE) / 2;

        if (mouseX >= speedX && mouseX <= speedX + SPEED_BTN_SIZE
                && mouseY >= speedBtnY && mouseY <= speedBtnY + SPEED_BTN_SIZE) {
            Simulation.paused = !Simulation.paused;
            if (!Simulation.paused && Simulation.gameSpeed == 0) Simulation.gameSpeed = 1;
            Simulation.lastTickTime = 0;
            return true;
        }

        for (int i = 0; i < 3; i++) {
            int sBtnX = speedX + SPEED_BTN_SIZE + 3 + i * (SPEED_BTN_SIZE + 2);
            if (mouseX >= sBtnX && mouseX <= sBtnX + SPEED_BTN_SIZE
                    && mouseY >= speedBtnY && mouseY <= speedBtnY + SPEED_BTN_SIZE) {
                Simulation.gameSpeed = i + 1;
                Simulation.paused = false;
                Simulation.lastTickTime = 0;
                return true;
            }
        }

        return false;
    }

    // === Panneau d'action sur structure sélectionnée ===

    public static int[] getActionPanelLayout(int screenWidth, int screenHeight) {
        boolean isLine = Tools.selectedStructure.kind.isLine;
        int nbButtons = isLine ? 2 : 3;
        int panelW = nbButtons * ACTION_BTN_WIDTH + (nbButtons + 1) * ACTION_PANEL_PADDING;
        int panelH = ACTION_BTN_HEIGHT + 2 * ACTION_PANEL_PADDING;
        int panelX = (screenWidth - panelW) / 2;
        int panelY = getToolbarY(screenHeight) - panelH - 5;
        return new int[]{panelX, panelY, panelW, panelH, nbButtons};
    }

    public static boolean handleActionPanelClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !CameraState.detached || Tools.selectedStructure == null) return false;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int[] layout = getActionPanelLayout(width, height);
        int panelX = layout[0], panelY = layout[1], nbButtons = layout[4];

        int btnY = panelY + ACTION_PANEL_PADDING;
        boolean isLine = Tools.selectedStructure.kind.isLine;

        for (int i = 0; i < nbButtons; i++) {
            int btnX = panelX + ACTION_PANEL_PADDING + i * (ACTION_BTN_WIDTH + ACTION_PANEL_PADDING);
            if (mouseX >= btnX && mouseX <= btnX + ACTION_BTN_WIDTH
                    && mouseY >= btnY && mouseY <= btnY + ACTION_BTN_HEIGHT) {
                if (i == 0) {
                    Tools.moveMode = true;
                } else if (!isLine && i == 1) {
                    BuildingPlacer.rotateStructure(Tools.selectedStructure);
                } else {
                    BuildingPlacer.deleteStructure(Tools.selectedStructure);
                    Tools.selectedStructure = null;
                }
                return true;
            }
        }
        return false;
    }
}
