package elc.florian.mcity.mixin;

import elc.florian.mcity.client.BuildingPlacer;
import elc.florian.mcity.client.CustomRayCast;
import elc.florian.mcity.client.ToolbarHelper;
import elc.florian.mcity.client.Zoom;
import elc.florian.mcity.state.CameraState;
import elc.florian.mcity.state.InputState;
import elc.florian.mcity.state.Tools;
import elc.florian.mcity.structure.StructureRegistry;
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
        if (!CameraState.detached) return;
        if (CameraState.cam.isZooming()) {
            CameraState.cam.setSpeed((int) (CameraState.cam.getSpeed() + vertical));
        } else {
            Zoom.zoom(vertical);
        }
    }

    @Inject(at = @At("HEAD"), method = "onMouseButton(JIII)V")
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (!CameraState.detached) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            InputState.mouseMiddlePressed = action == 1;
            InputState.newDragStart = true;
            return;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == 1) {
            handleLeftClick();
        }
    }

    private void handleLeftClick() {
        if (ToolbarHelper.handleInfoBarClick(InputState.mouseX, InputState.mouseY)) return;
        if (ToolbarHelper.handleActionPanelClick(InputState.mouseX, InputState.mouseY)) return;
        if (ToolbarHelper.handleToolbarClick(InputState.mouseX, InputState.mouseY)) return;

        HitResult hit = CustomRayCast.throwRay((int) InputState.mouseX, (int) InputState.mouseY);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos blockPos = ((BlockHitResult) hit).getBlockPos().toImmutable();

        // Mode déplacement de structure existante
        if (Tools.moveMode && Tools.selectedStructure != null) {
            BuildingPlacer.moveStructureTo(Tools.selectedStructure, blockPos);
            Tools.moveMode = false;
            return;
        }

        // Aucun outil actif → sélection d'une structure existante
        if (Tools.selectedSubType == null) {
            Tools.selectedStructure = StructureRegistry.findAt(blockPos);
            return;
        }

        // Outil actif → délégation au sous-type
        Tools.selectedStructure = null;
        Tools.selectedSubType.onClick(blockPos);
    }
}
