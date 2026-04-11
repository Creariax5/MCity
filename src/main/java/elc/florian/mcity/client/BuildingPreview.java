package elc.florian.mcity.client;

import com.mojang.blaze3d.systems.RenderSystem;
import elc.florian.mcity.MCity;
import elc.florian.mcity.structure.ZoneRegistry;
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

    // Couleurs preview
    private static final float[] WALL_COLOR  = {0.6f, 0.4f, 0.2f, 0.35f};
    private static final float[] FLOOR_COLOR = {0.5f, 0.5f, 0.5f, 0.35f};
    private static final float[] ROOF_COLOR  = {0.5f, 0.35f, 0.15f, 0.35f};

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
    private static final float[] HIGHLIGHT_COLOR = {1.0f, 1.0f, 0.3f, 0.35f};

    // Couleurs des zones
    private static final float[] ZONE_GRID_COLOR = {1.0f, 1.0f, 1.0f, 0.10f};
    private static final float[] ZONE_HABITATION = {0.3f, 0.9f, 0.3f, 0.35f};
    private static final float[] ZONE_COMMERCE   = {0.3f, 0.5f, 0.9f, 0.35f};
    private static final float[] ZONE_INDUSTRIE  = {0.9f, 0.8f, 0.3f, 0.35f};

    public static void render(WorldRenderContext context) {
        if (!MCity.detached) return;

        if (MCity.selectedStructure != null) {
            renderHighlight(context);
        }

        // Grille de zones visible en mode AREA
        if (MCity.selectedTool == MCity.ToolType.AREA) {
            renderZoneGrid(context);
        }

        if (MCity.selectedTool == MCity.ToolType.ROAD && MCity.selectedRoadType != null) {
            renderLinePreview(context, getRoadColor(MCity.selectedRoadType), RoadPlacer.getWidth(MCity.selectedRoadType), true);
        } else if (MCity.selectedTool == MCity.ToolType.WATER && MCity.selectedWaterType != null) {
            if (MCity.selectedWaterType == MCity.WaterType.CANALISATION) {
                renderLinePreview(context, COPPER_COLOR, 1, false);
            } else {
                renderSimplePreview(context, WATER_COLOR);
            }
        } else if (MCity.selectedTool == MCity.ToolType.ELECTRICITY && MCity.selectedElectricityType != null) {
            if (MCity.selectedElectricityType == MCity.ElectricityType.CABLE) {
                renderLinePreview(context, REDSTONE_COLOR, 1, false);
            } else {
                renderSimplePreview(context, ELEC_COLOR);
            }
        }
    }

    private static void renderHighlight(WorldRenderContext context) {
        elc.florian.mcity.structure.PlacedStructure s = MCity.selectedStructure;
        if (s == null || s.blocks.isEmpty()) return;

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(-context.camera().getPos().x, -context.camera().getPos().y, -context.camera().getPos().z);
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();

        beginRender();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (BlockPos p : s.blocks) {
            drawBlock(buffer, posMatrix, p.getX(), p.getY(), p.getZ(), HIGHLIGHT_COLOR);
        }

        endRender(buffer, matrices);
    }

    private static final float[] INVALID_COLOR = {1.0f, 0.2f, 0.2f, 0.5f};

    private static void renderLinePreview(WorldRenderContext context, float[] color, int width, boolean canValidate) {
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

            float[] drawColor = color;
            if (canValidate) {
                boolean valid = RoadPlacer.isRoadValid(MCity.lineFirstPoint, mousePos);
                if (!valid) drawColor = INVALID_COLOR;
            }

            for (BlockPos pos : blocks) {
                drawFlatBlock(buffer, posMatrix, pos.getX(), pos.getY(), pos.getZ(), drawColor);
            }
            drawBlock(buffer, posMatrix, MCity.lineFirstPoint.getX(), MCity.lineFirstPoint.getY(), MCity.lineFirstPoint.getZ(), MARKER_COLOR);
        } else {
            drawBlock(buffer, posMatrix, mousePos.getX(), mousePos.getY(), mousePos.getZ(), MARKER_COLOR);
        }

        endRender(buffer, matrices);
    }

    private static float[] getRoadColor(MCity.RoadType type) {
        return ROAD_COLOR;
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

    private static float[] getZoneColor(MCity.AreaType type) {
        return switch (type) {
            case HABITATION -> ZONE_HABITATION;
            case COMMERCE -> ZONE_COMMERCE;
            case INDUSTRIE -> ZONE_INDUSTRIE;
            case DEZONNAGE -> ZONE_GRID_COLOR;
        };
    }

    private static void renderZoneGrid(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;

        java.util.Set<Long> placeable = ZoneRegistry.getPlaceableTiles();
        if (placeable.isEmpty() && ZoneRegistry.allZones().isEmpty()) return;

        // Y au niveau de la souris (approximatif — sol au niveau du bloc pointé)
        HitResult hit = CustomRayCast.throwRay((int) MCity.mouseX, (int) MCity.mouseY);
        int y = (hit != null && hit.getType() == HitResult.Type.BLOCK)
                ? ((BlockHitResult) hit).getBlockPos().getY() + 1
                : 64;

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(-context.camera().getPos().x, -context.camera().getPos().y, -context.camera().getPos().z);
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();

        beginRender();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int tileSize = ZoneRegistry.TILE_SIZE;

        // Grille fantôme de toutes les tiles plaçables
        for (long key : placeable) {
            int tx = ZoneRegistry.tileX(key);
            int tz = ZoneRegistry.tileZ(key);
            float x0 = tx * tileSize;
            float z0 = tz * tileSize;
            drawTile(buffer, posMatrix, x0, y, z0, tileSize, ZONE_GRID_COLOR);
        }

        // Tiles zonées : couleur pleine
        for (java.util.Map.Entry<Long, MCity.AreaType> e : ZoneRegistry.allZones().entrySet()) {
            int tx = ZoneRegistry.tileX(e.getKey());
            int tz = ZoneRegistry.tileZ(e.getKey());
            float x0 = tx * tileSize;
            float z0 = tz * tileSize;
            drawTile(buffer, posMatrix, x0, y, z0, tileSize, getZoneColor(e.getValue()));
        }

        endRender(buffer, matrices);
    }

    private static void drawTile(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, int size, float[] color) {
        float y1 = y + 0.05f;
        float x1 = x + size;
        float z1 = z + size;
        float r = color[0], g = color[1], b = color[2], a = color[3];
        buffer.vertex(matrix, x, y1, z).color(r, g, b, a);
        buffer.vertex(matrix, x, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z).color(r, g, b, a);
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
