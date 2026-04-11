package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import elc.florian.mcity.client.BuildingPlacer;
import elc.florian.mcity.client.CustomRayCast;
import elc.florian.mcity.client.RoadPlacer;
import elc.florian.mcity.client.ToolbarHelper;
import elc.florian.mcity.client.Zoom;
import elc.florian.mcity.structure.PlacedStructure;
import elc.florian.mcity.structure.StructureRegistry;
import elc.florian.mcity.structure.ZoneRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("RETURN"), method = "onMouseScroll(JDD)V")
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!MCity.detached) return;
        if (MCity.cam.isZooming()) {
            MCity.cam.setSpeed((int) (MCity.cam.getSpeed() + vertical));
        } else {
            Zoom.zoom(vertical);
        }
    }

    @Inject(at = @At("HEAD"), method = "onMouseButton(JIII)V")
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (!MCity.detached) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            MCity.mouseMiddlePressed = action == 1;
            MCity.newDragStart = true;
            return;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == 1) {
            handleLeftClick();
        }
    }

    private void handleLeftClick() {
        // UI d'abord
        if (ToolbarHelper.handleInfoBarClick(MCity.mouseX, MCity.mouseY)) return;
        if (ToolbarHelper.handleActionPanelClick(MCity.mouseX, MCity.mouseY)) return;
        if (ToolbarHelper.handleToolbarClick(MCity.mouseX, MCity.mouseY)) return;

        // Raycast sur le monde
        HitResult hit = CustomRayCast.throwRay((int) MCity.mouseX, (int) MCity.mouseY);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos blockPos = ((BlockHitResult) hit).getBlockPos().toImmutable();

        // Mode déplacement : on reclique pour poser la structure déplacée
        if (MCity.moveMode && MCity.selectedStructure != null) {
            BuildingPlacer.moveStructureTo(MCity.selectedStructure, blockPos);
            MCity.moveMode = false;
            return;
        }

        // Aucun outil → sélection
        if (MCity.selectedTool == null) {
            PlacedStructure found = StructureRegistry.findAt(blockPos);
            MCity.selectedStructure = found;
            return;
        }

        // Outil actif → placement
        MCity.selectedStructure = null;

        if (MCity.selectedTool == MCity.ToolType.ROAD && MCity.selectedRoadType != null) {
            handleLinePlacement(blockPos, () -> RoadPlacer.placeRoad(MCity.lineFirstPoint, blockPos));
        } else if (MCity.selectedTool == MCity.ToolType.AREA && MCity.selectedAreaType != null) {
            placeZone(blockPos);
        } else if (MCity.selectedTool == MCity.ToolType.WATER && MCity.selectedWaterType != null) {
            switch (MCity.selectedWaterType) {
                case PUITS -> BuildingPlacer.placePuits(blockPos, 0);
                case CANALISATION -> handleLinePlacement(blockPos, () -> RoadPlacer.placeCanalisation(MCity.lineFirstPoint, blockPos));
                case RESERVOIR -> BuildingPlacer.placeReservoir(blockPos, 0);
            }
        } else if (MCity.selectedTool == MCity.ToolType.ELECTRICITY && MCity.selectedElectricityType != null) {
            switch (MCity.selectedElectricityType) {
                case GENERATEUR -> BuildingPlacer.placeGenerateur(blockPos, 0);
                case CABLE -> handleLinePlacement(blockPos, () -> RoadPlacer.placeCable(MCity.lineFirstPoint, blockPos));
                case TOUR_RELAIS -> BuildingPlacer.placeTourRelais(blockPos, 0);
            }
        } else {
            BuildingPlacer.breakBlock(blockPos);
        }
    }

    private void placeZone(BlockPos pos) {
        int tx = ZoneRegistry.blockToTile(pos.getX());
        int tz = ZoneRegistry.blockToTile(pos.getZ());
        boolean erase = MCity.selectedAreaType == MCity.AreaType.DEZONNAGE;

        if (MCity.zoneFillMode) {
            if (erase) {
                ZoneRegistry.floodClear(tx, tz);
            } else {
                ZoneRegistry.floodFill(tx, tz, MCity.selectedAreaType);
            }
        } else {
            if (erase) {
                ZoneRegistry.removeZone(tx, tz);
            } else {
                ZoneRegistry.setZone(tx, tz, MCity.selectedAreaType);
            }
        }
    }

    private void handleLinePlacement(BlockPos blockPos, Runnable placeLine) {
        if (MCity.lineFirstPoint == null) {
            MCity.lineFirstPoint = blockPos;
        } else {
            placeLine.run();
            MCity.lineFirstPoint = null;
        }
    }
}
