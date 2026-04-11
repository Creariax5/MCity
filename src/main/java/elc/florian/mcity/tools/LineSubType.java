package elc.florian.mcity.tools;

import elc.florian.mcity.state.Tools;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

/** Sous-type qui pose une ligne entre 2 points (route, canalisation, câble). */
public class LineSubType extends SubType {
    private final BiConsumer<BlockPos, BlockPos> placer;

    public LineSubType(String name, int color, BiConsumer<BlockPos, BlockPos> placer) {
        super(name, color);
        this.placer = placer;
    }

    @Override
    public boolean isLine() { return true; }

    @Override
    public void onClick(BlockPos pos) {
        if (Tools.lineFirstPoint == null) {
            Tools.lineFirstPoint = pos;
        } else {
            placer.accept(Tools.lineFirstPoint, pos);
            Tools.lineFirstPoint = null;
        }
    }
}
