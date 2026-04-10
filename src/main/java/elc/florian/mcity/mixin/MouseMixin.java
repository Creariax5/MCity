package elc.florian.mcity.mixin;

import elc.florian.mcity.MCity;
import elc.florian.mcity.client.BuildingPlacer;
import elc.florian.mcity.client.CustomRayCast;
import elc.florian.mcity.client.RoadPlacer;
import elc.florian.mcity.client.ToolbarHelper;
import elc.florian.mcity.client.Zoom;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
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
    private void onOnMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci)
    {
        if (MCity.detached) {
            if (MCity.cam.isZooming()) {
                MCity.cam.setSpeed((int) (MCity.cam.getSpeed() + vertical));
                return;
            }

            Zoom.zoom(vertical);
        }
    }

    @Inject(at = @At("HEAD"), method = "onMouseButton(JIII)V")
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci)
    {
        if (MCity.detached) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                MCity.mouse_middle_pressed = action == 1;
                MCity.newDeplace = true;
                return;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == 1) {
                if (ToolbarHelper.handleInfoBarClick(MCity.mouseX, MCity.mouseY)) {
                    return;
                }
                if (ToolbarHelper.handleToolbarClick(MCity.mouseX, MCity.mouseY)) {
                    return;
                }
                HitResult hit = CustomRayCast.throwRay((int) MCity.mouseX, (int) MCity.mouseY);
                BlockHitResult blockHit = (BlockHitResult) hit;
                BlockPos blockPos = blockHit.getBlockPos().toImmutable();

                if (MCity.selectedTool == MCity.ToolType.ROAD && MCity.selectedRoadType != null) {
                    // Ligne entre 2 points
                    if (MCity.lineFirstPoint == null) {
                        MCity.lineFirstPoint = blockPos;
                    } else {
                        RoadPlacer.placeRoad(MCity.lineFirstPoint, blockPos, MCity.selectedRoadType);
                        MCity.lineFirstPoint = null;
                    }
                } else if (MCity.selectedTool == MCity.ToolType.AREA && MCity.selectedAreaType != null) {
                    switch (MCity.selectedAreaType) {
                        case HABITATION -> BuildingPlacer.placeHouse(blockPos);
                        case COMMERCE -> BuildingPlacer.placeCommerce(blockPos);
                        case INDUSTRIE -> BuildingPlacer.placeIndustrie(blockPos);
                        case FERME -> BuildingPlacer.placeFerme(blockPos);
                    }
                } else if (MCity.selectedTool == MCity.ToolType.WATER && MCity.selectedWaterType != null) {
                    switch (MCity.selectedWaterType) {
                        case PUITS -> BuildingPlacer.placePuits(blockPos);
                        case CANALISATION -> {
                            if (MCity.lineFirstPoint == null) {
                                MCity.lineFirstPoint = blockPos;
                            } else {
                                RoadPlacer.placeCanalisation(MCity.lineFirstPoint, blockPos);
                                MCity.lineFirstPoint = null;
                            }
                        }
                        case RESERVOIR -> BuildingPlacer.placeReservoir(blockPos);
                    }
                } else if (MCity.selectedTool == MCity.ToolType.ELECTRICITY && MCity.selectedElectricityType != null) {
                    switch (MCity.selectedElectricityType) {
                        case GENERATEUR -> BuildingPlacer.placeGenerateur(blockPos);
                        case CABLE -> {
                            if (MCity.lineFirstPoint == null) {
                                MCity.lineFirstPoint = blockPos;
                            } else {
                                RoadPlacer.placeCable(MCity.lineFirstPoint, blockPos);
                                MCity.lineFirstPoint = null;
                            }
                        }
                        case TOUR_RELAIS -> BuildingPlacer.placeTourRelais(blockPos);
                    }
                } else {
                    BuildingPlacer.breakBlock(blockPos);
                }
                return;
            }

        }
    }
    @Shadow
    private double x;
    @Shadow
    private double y;
}