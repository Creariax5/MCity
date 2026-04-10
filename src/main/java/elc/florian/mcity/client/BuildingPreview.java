package elc.florian.mcity.client;

import com.mojang.blaze3d.systems.RenderSystem;
import elc.florian.mcity.MCity;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;

import java.util.List;

public class BuildingPreview {

    // Maison
    private static final int HOUSE_WIDTH = 5;
    private static final int HOUSE_DEPTH = 5;
    private static final int HOUSE_HEIGHT = 4;

    private static final float[] WALL_COLOR  = {0.6f, 0.4f, 0.2f, 0.35f};
    private static final float[] FLOOR_COLOR = {0.5f, 0.5f, 0.5f, 0.35f};
    private static final float[] ROOF_COLOR  = {0.5f, 0.35f, 0.15f, 0.35f};
    private static final float[] GLASS_COLOR = {0.6f, 0.8f, 1.0f, 0.25f};

    // Lignes
    private static final float[] PATH_COLOR    = {0.55f, 0.45f, 0.3f, 0.4f};
    private static final float[] ROAD_COLOR    = {0.5f, 0.5f, 0.5f, 0.4f};
    private static final float[] HIGHWAY_COLOR = {0.45f, 0.4f, 0.35f, 0.4f};
    private static final float[] COPPER_COLOR  = {0.72f, 0.45f, 0.2f, 0.4f};
    private static final float[] REDSTONE_COLOR = {0.8f, 0.1f, 0.1f, 0.4f};

    // Marqueur + structures
    private static final float[] MARKER_COLOR = {1.0f, 0.3f, 0.3f, 0.6f};
    private static final float[] BUILDING_COLOR = {0.5f, 0.5f, 0.5f, 0.3f};
    private static final float[] WATER_COLOR = {0.2f, 0.4f, 0.8f, 0.3f};
    private static final float[] ELEC_COLOR = {0.8f, 0.2f, 0.2f, 0.3f};

    public static void render(WorldRenderContext context) {
        if (!MCity.detached) return;

        if (MCity.selectedTool == MCity.ToolType.ROAD && MCity.selectedRoadType != null) {
            renderLinePreview(context, getRoadColor(MCity.selectedRoadType), RoadPlacer.getWidth(MCity.selectedRoadType));
        } else if (MCity.selectedTool == MCity.ToolType.AREA && MCity.selectedAreaType != null) {
            renderBuildingPreview(context);
        } else if (MCity.selectedTool == MCity.ToolType.WATER && MCity.selectedWaterType != null) {
            if (MCity.selectedWaterType == MCity.WaterType.CANALISATION) {
                renderLinePreview(context, COPPER_COLOR, 1);
            } else {
                renderSimplePreview(context, WATER_COLOR);
            }
        } else if (MCity.selectedTool == MCity.ToolType.ELECTRICITY && MCity.selectedElectricityType != null) {
            if (MCity.selectedElectricityType == MCity.ElectricityType.CABLE) {
                renderLinePreview(context, REDSTONE_COLOR, 1);
            } else {
                renderSimplePreview(context, ELEC_COLOR);
            }
        }
    }

    private static void renderLinePreview(WorldRenderContext context, float[] color, int width) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;

        HitResult hit = CustomRayCast.throwRay((int) MCity.mouseX, (int) MCity.mouseY);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos mousePos = blockHit.getBlockPos();

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(-context.camera().getPos().x, -context.camera().getPos().y, -context.camera().getPos().z);
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();

        beginRender();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        if (MCity.lineFirstPoint != null) {
            List<BlockPos> blocks = RoadPlacer.computeRoadBlocks(MCity.lineFirstPoint, mousePos, width);
            for (BlockPos pos : blocks) {
                drawFlatBlock(buffer, posMatrix, pos.getX(), pos.getY(), pos.getZ(), color);
            }
            drawBlock(buffer, posMatrix, MCity.lineFirstPoint.getX(), MCity.lineFirstPoint.getY(), MCity.lineFirstPoint.getZ(), MARKER_COLOR);
        } else {
            drawBlock(buffer, posMatrix, mousePos.getX(), mousePos.getY(), mousePos.getZ(), MARKER_COLOR);
        }

        endRender(buffer, matrices);
    }

    private static float[] getRoadColor(MCity.RoadType type) {
        return switch (type) {
            case PATH -> PATH_COLOR;
            case ROAD -> ROAD_COLOR;
            case HIGHWAY -> HIGHWAY_COLOR;
        };
    }

    private static void renderSimplePreview(WorldRenderContext context, float[] color) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;

        HitResult hit = CustomRayCast.throwRay((int) MCity.mouseX, (int) MCity.mouseY);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos base = blockHit.getBlockPos().up();

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(-context.camera().getPos().x, -context.camera().getPos().y, -context.camera().getPos().z);
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();

        beginRender();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Preview simple : cube coloré 3x3x3
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 3; y++) {
                    drawBlock(buffer, posMatrix, base.getX() + x, base.getY() + y, base.getZ() + z, color);
                }
            }
        }

        endRender(buffer, matrices);
    }

    private static void renderBuildingPreview(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;

        HitResult hit = CustomRayCast.throwRay((int) MCity.mouseX, (int) MCity.mouseY);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos base = blockHit.getBlockPos().up();

        // Taille et couleur selon le type
        int w, d, h;
        float[] color;
        switch (MCity.selectedAreaType) {
            case HABITATION -> { w = 5; d = 5; h = 5; color = WALL_COLOR; }
            case COMMERCE -> { w = 5; d = 3; h = 4; color = new float[]{0.8f, 0.2f, 0.2f, 0.3f}; }
            case INDUSTRIE -> { w = 6; d = 5; h = 6; color = FLOOR_COLOR; }
            case FERME -> { w = 7; d = 7; h = 2; color = new float[]{0.4f, 0.6f, 0.2f, 0.3f}; }
            default -> { return; }
        }

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(-context.camera().getPos().x, -context.camera().getPos().y, -context.camera().getPos().z);
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();

        beginRender();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                // Sol
                drawFlatBlock(buffer, posMatrix, base.getX() + x, base.getY() - 1, base.getZ() + z, color);
                // Murs (contour)
                boolean isWall = x == 0 || x == w - 1 || z == 0 || z == d - 1;
                if (isWall) {
                    for (int y = 0; y < h; y++) {
                        drawBlock(buffer, posMatrix, base.getX() + x, base.getY() + y, base.getZ() + z, color);
                    }
                }
                // Toit
                drawFlatBlock(buffer, posMatrix, base.getX() + x, base.getY() + h - 1, base.getZ() + z, ROOF_COLOR);
            }
        }

        endRender(buffer, matrices);
    }

    private static void beginRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void endRender(BufferBuilder buffer, MatrixStack matrices) {
        BuiltBuffer builtBuffer = buffer.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
        }
        matrices.pop();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Dessine un bloc plein (6 faces) pour la preview maison/marqueur
     */
    private static void drawBlock(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, float[] color) {
        float x1 = x + 1.0f;
        float y1 = y + 1.0f;
        float z1 = z + 1.0f;
        float r = color[0], g = color[1], b = color[2], a = color[3];

        buffer.vertex(matrix, x,  y, z ).color(r, g, b, a);
        buffer.vertex(matrix, x1, y, z ).color(r, g, b, a);
        buffer.vertex(matrix, x1, y, z1).color(r, g, b, a);
        buffer.vertex(matrix, x,  y, z1).color(r, g, b, a);

        buffer.vertex(matrix, x,  y1, z ).color(r, g, b, a);
        buffer.vertex(matrix, x,  y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z ).color(r, g, b, a);

        buffer.vertex(matrix, x,  y,  z).color(r, g, b, a);
        buffer.vertex(matrix, x,  y1, z).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z).color(r, g, b, a);
        buffer.vertex(matrix, x1, y,  z).color(r, g, b, a);

        buffer.vertex(matrix, x,  y,  z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y,  z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x,  y1, z1).color(r, g, b, a);

        buffer.vertex(matrix, x, y,  z ).color(r, g, b, a);
        buffer.vertex(matrix, x, y,  z1).color(r, g, b, a);
        buffer.vertex(matrix, x, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x, y1, z ).color(r, g, b, a);

        buffer.vertex(matrix, x1, y,  z ).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z ).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y,  z1).color(r, g, b, a);
    }

    /**
     * Dessine juste la face du dessus (pour la preview route, plus léger)
     */
    private static void drawFlatBlock(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, float[] color) {
        float x1 = x + 1.0f;
        float y1 = y + 1.02f; // Légèrement au-dessus pour éviter le z-fighting
        float z1 = z + 1.0f;
        float r = color[0], g = color[1], b = color[2], a = color[3];

        buffer.vertex(matrix, x,  y1, z ).color(r, g, b, a);
        buffer.vertex(matrix, x,  y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z ).color(r, g, b, a);
    }
}
