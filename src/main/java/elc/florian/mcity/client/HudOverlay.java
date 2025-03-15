package elc.florian.mcity.client;

import com.mojang.blaze3d.systems.RenderSystem;
import elc.florian.mcity.MCity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class HudOverlay implements HudRenderCallback {
    private static final Identifier TOOL_BAR = Identifier.of(MCity.MOD_ID, "textures/hud/tool_bar.png");

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        int x = 0;
        int y = 0;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null) {
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();

            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, TOOL_BAR);

            drawContext.drawTexture(RenderLayer::getGuiTextured, TOOL_BAR, 0,height-40,0.0f,0.0f,width,80, 10, 10);
        }
    }
}
