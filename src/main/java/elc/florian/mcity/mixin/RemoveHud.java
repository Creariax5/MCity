package elc.florian.mcity.mixin;
import elc.florian.mcity.state.Tools;
import elc.florian.mcity.state.Simulation;
import elc.florian.mcity.state.CameraState;


import elc.florian.mcity.MCity;
import elc.florian.mcity.client.ToolbarHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class RemoveHud {

    @Inject(at=@At("HEAD"), method = "render", cancellable = true)
    public void render(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo info) {
        if (CameraState.detached) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                int width = client.getWindow().getScaledWidth();
                int height = client.getWindow().getScaledHeight();

                renderInfoBar(drawContext, client, width, height);
                renderToolbar(drawContext, client, width, height);
                renderPanel(drawContext, client, width, height);
                renderActionPanel(drawContext, client, width, height);
                renderFillModeToggle(drawContext, client, width, height);
            }
            info.cancel();
        }
    }

    private void renderInfoBar(DrawContext dc, MinecraftClient client, int width, int height) {
        Simulation.tick();

        int infoY = ToolbarHelper.getInfoBarY(height);

        // Fond
        dc.fill(0, infoY, width, height, 0xCC111111);

        int textY = infoY + (ToolbarHelper.INFO_BAR_HEIGHT - 8) / 2;

        // Date
        dc.drawText(client.textRenderer, Simulation.getDateString(), 5, textY, 0xFFDDDDDD, true);

        // Boutons vitesse
        int speedX = 80;
        int speedBtnY = infoY + (ToolbarHelper.INFO_BAR_HEIGHT - ToolbarHelper.SPEED_BTN_SIZE) / 2;

        // Pause
        int pauseColor = Simulation.paused ? 0xFFFF6666 : 0xFF888888;
        dc.fill(speedX, speedBtnY, speedX + ToolbarHelper.SPEED_BTN_SIZE, speedBtnY + ToolbarHelper.SPEED_BTN_SIZE, pauseColor);
        int pauseTextX = speedX + (ToolbarHelper.SPEED_BTN_SIZE - client.textRenderer.getWidth("||")) / 2;
        dc.drawText(client.textRenderer, "||", pauseTextX, speedBtnY + 3, 0xFFFFFFFF, true);

        // x1, x2, x3
        String[] speedLabels = {">", ">>", ">>>"};
        for (int s = 0; s < 3; s++) {
            int sBtnX = speedX + ToolbarHelper.SPEED_BTN_SIZE + 3 + s * (ToolbarHelper.SPEED_BTN_SIZE + 2);
            boolean active = !Simulation.paused && Simulation.gameSpeed == (s + 1);
            dc.fill(sBtnX, speedBtnY, sBtnX + ToolbarHelper.SPEED_BTN_SIZE, speedBtnY + ToolbarHelper.SPEED_BTN_SIZE, active ? 0xFF66CC66 : 0xFF555555);
            int sTextW = client.textRenderer.getWidth(speedLabels[s]);
            dc.drawText(client.textRenderer, speedLabels[s], sBtnX + (ToolbarHelper.SPEED_BTN_SIZE - sTextW) / 2, speedBtnY + 3, 0xFFFFFFFF, true);
        }

        // Demande RCI
        int demandStartX = speedX + ToolbarHelper.SPEED_BTN_SIZE + 3 + 3 * (ToolbarHelper.SPEED_BTN_SIZE + 2) + 15;
        int demandBarY = infoY + (ToolbarHelper.INFO_BAR_HEIGHT - ToolbarHelper.DEMAND_BAR_HEIGHT) / 2;
        String[] demandLabels = {"R", "C", "I"};
        int[] demandValues = {Simulation.demandResidential, Simulation.demandCommercial, Simulation.demandIndustrial};
        int[] demandColors = {0xFF44AA44, 0xFF4488DD, 0xFFDDAA44};

        for (int d = 0; d < 3; d++) {
            int dX = demandStartX + d * (ToolbarHelper.DEMAND_BAR_WIDTH + 20);
            dc.drawText(client.textRenderer, demandLabels[d], dX, textY, demandColors[d], true);
            int barX = dX + 10;
            dc.fill(barX, demandBarY, barX + ToolbarHelper.DEMAND_BAR_WIDTH, demandBarY + ToolbarHelper.DEMAND_BAR_HEIGHT, 0xFF333333);
            int fillW = (int) (ToolbarHelper.DEMAND_BAR_WIDTH * demandValues[d] / 100.0);
            dc.fill(barX, demandBarY, barX + fillW, demandBarY + ToolbarHelper.DEMAND_BAR_HEIGHT, demandColors[d]);
        }

        // Bonheur + Population + Argent (côté droit)
        int rightX = width - 5;

        // Argent
        String moneyStr = Simulation.getMoneyString();
        rightX -= client.textRenderer.getWidth(moneyStr);
        dc.drawText(client.textRenderer, moneyStr, rightX, textY, 0xFFFFDD44, true);

        rightX -= 10;

        // Population
        String popStr = Simulation.getPopulationString();
        rightX -= client.textRenderer.getWidth(popStr);
        dc.drawText(client.textRenderer, popStr, rightX, textY, 0xFFCCCCCC, true);

        rightX -= 10;

        // Bonheur
        String happyStr = Simulation.getHappinessEmoji() + " " + Simulation.happiness + "%";
        int happyColor;
        if (Simulation.happiness >= 70) happyColor = 0xFF66CC66;
        else if (Simulation.happiness >= 40) happyColor = 0xFFDDAA44;
        else happyColor = 0xFFFF6666;
        rightX -= client.textRenderer.getWidth(happyStr);
        dc.drawText(client.textRenderer, happyStr, rightX, textY, happyColor, true);
    }

    private void renderToolbar(DrawContext dc, MinecraftClient client, int width, int height) {
        int toolbarY = ToolbarHelper.getToolbarY(height);
        int startX = ToolbarHelper.getToolbarStartX(width);
        int buttonY = ToolbarHelper.getButtonY(height);

        java.util.List<elc.florian.mcity.tools.Tool> all = elc.florian.mcity.tools.ToolRegistry.all();
        int n = all.size();

        int totalWidth = n * ToolbarHelper.BUTTON_SIZE + (n - 1) * ToolbarHelper.BUTTON_SPACING;
        dc.fill(startX - 6, toolbarY, startX + totalWidth + 6, toolbarY + ToolbarHelper.TOOLBAR_HEIGHT, 0xAA222222);

        Tools.ToolType[] toolTypes = Tools.ToolType.values();
        for (int i = 0; i < n; i++) {
            int btnX = startX + i * (ToolbarHelper.BUTTON_SIZE + ToolbarHelper.BUTTON_SPACING);
            boolean selected = toolTypes[i] == Tools.selectedTool;

            if (selected) {
                dc.fill(btnX - 2, buttonY - 2, btnX + ToolbarHelper.BUTTON_SIZE + 2, buttonY + ToolbarHelper.BUTTON_SIZE + 2, 0xFFFFFFFF);
            }
            dc.fill(btnX, buttonY, btnX + ToolbarHelper.BUTTON_SIZE, buttonY + ToolbarHelper.BUTTON_SIZE, 0xFF333333);

            int iconX = btnX + (ToolbarHelper.BUTTON_SIZE - 16) / 2;
            int iconY = buttonY + (ToolbarHelper.BUTTON_SIZE - 16) / 2;
            dc.drawItem(new ItemStack(all.get(i).icon), iconX, iconY);
        }
    }

    private void renderFillModeToggle(DrawContext dc, MinecraftClient client, int width, int height) {
        if (Tools.selectedTool != Tools.ToolType.AREA) return;

        int startX = ToolbarHelper.getToolbarStartX(width);
        int buttonY = ToolbarHelper.getButtonY(height);
        int n = elc.florian.mcity.tools.ToolRegistry.all().size();
        int toolbarEnd = startX + n * ToolbarHelper.BUTTON_SIZE + (n - 1) * ToolbarHelper.BUTTON_SPACING;

        int btnW = 60;
        int btnH = ToolbarHelper.BUTTON_SIZE;
        int btnX = toolbarEnd + 15;

        String label = Tools.zoneFillMode ? "Remplir" : "Tile";
        int color = Tools.zoneFillMode ? 0xFF66CC66 : 0xFF555555;

        dc.fill(btnX, buttonY, btnX + btnW, buttonY + btnH, color);
        int tw = client.textRenderer.getWidth(label);
        dc.drawText(client.textRenderer, label, btnX + (btnW - tw) / 2, buttonY + (btnH - 8) / 2, 0xFFFFFFFF, true);
    }

    private void renderActionPanel(DrawContext dc, MinecraftClient client, int width, int height) {
        if (Tools.selectedStructure == null) return;

        int[] layout = ToolbarHelper.getActionPanelLayout(width, height);
        int panelX = layout[0], panelY = layout[1], panelW = layout[2], panelH = layout[3], nbButtons = layout[4];

        // Fond
        dc.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xDD222222);

        // Nom de la structure au-dessus
        String name = Tools.selectedStructure.kind.displayName;
        if (Tools.moveMode) name += " (cliquez pour déplacer)";
        int nameW = client.textRenderer.getWidth(name);
        dc.drawText(client.textRenderer, name, panelX + (panelW - nameW) / 2, panelY - 10, 0xFFFFFFFF, true);

        int btnY = panelY + ToolbarHelper.ACTION_PANEL_PADDING;
        boolean isLine = Tools.selectedStructure.kind.isLine;
        String[] labels = isLine ? new String[]{"Deplacer", "Supprimer"} : new String[]{"Deplacer", "Tourner", "Supprimer"};
        int[] colors = isLine ? new int[]{0xFF4488DD, 0xFFDD4444} : new int[]{0xFF4488DD, 0xFFDDAA44, 0xFFDD4444};

        for (int i = 0; i < nbButtons; i++) {
            int btnX = panelX + ToolbarHelper.ACTION_PANEL_PADDING + i * (ToolbarHelper.ACTION_BTN_WIDTH + ToolbarHelper.ACTION_PANEL_PADDING);
            dc.fill(btnX, btnY, btnX + ToolbarHelper.ACTION_BTN_WIDTH, btnY + ToolbarHelper.ACTION_BTN_HEIGHT, colors[i]);
            int tw = client.textRenderer.getWidth(labels[i]);
            dc.drawText(client.textRenderer, labels[i], btnX + (ToolbarHelper.ACTION_BTN_WIDTH - tw) / 2, btnY + 5, 0xFFFFFFFF, true);
        }
    }

    private void renderPanel(DrawContext dc, MinecraftClient client, int width, int height) {
        if (!Tools.panelOpen || Tools.selectedTool == null) return;

        java.util.List<elc.florian.mcity.tools.SubType> subs = ToolbarHelper.getActiveSubTypes();
        if (subs == null || subs.isEmpty()) return;

        int selectedIndex = ToolbarHelper.getActiveSelectedIndex();
        int startX = ToolbarHelper.getToolbarStartX(width);
        int buttonY = ToolbarHelper.getButtonY(height);

        int toolIndex = Tools.selectedTool.ordinal();
        int btnX = startX + toolIndex * (ToolbarHelper.BUTTON_SIZE + ToolbarHelper.BUTTON_SPACING);
        int panelW = ToolbarHelper.PANEL_ITEM_WIDTH;
        int panelX = btnX - (panelW - ToolbarHelper.BUTTON_SIZE) / 2;
        int panelH = subs.size() * (ToolbarHelper.PANEL_ITEM_HEIGHT + ToolbarHelper.PANEL_PADDING) + ToolbarHelper.PANEL_PADDING;
        int panelY = buttonY - panelH - 5;

        dc.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xDD222222);

        for (int j = 0; j < subs.size(); j++) {
            elc.florian.mcity.tools.SubType sub = subs.get(j);
            int itemX = panelX + ToolbarHelper.PANEL_PADDING;
            int itemY = panelY + ToolbarHelper.PANEL_PADDING + j * (ToolbarHelper.PANEL_ITEM_HEIGHT + ToolbarHelper.PANEL_PADDING);
            int itemW = panelW - 2 * ToolbarHelper.PANEL_PADDING;

            if (j == selectedIndex) {
                dc.fill(itemX - 1, itemY - 1, itemX + itemW + 1, itemY + ToolbarHelper.PANEL_ITEM_HEIGHT + 1, 0xFFFFFFFF);
            }
            dc.fill(itemX, itemY, itemX + itemW, itemY + ToolbarHelper.PANEL_ITEM_HEIGHT, sub.color);

            int tw = client.textRenderer.getWidth(sub.name);
            dc.drawText(client.textRenderer, sub.name,
                    itemX + (itemW - tw) / 2,
                    itemY + (ToolbarHelper.PANEL_ITEM_HEIGHT - 8) / 2,
                    0xFFFFFFFF, true);
        }
    }
}
