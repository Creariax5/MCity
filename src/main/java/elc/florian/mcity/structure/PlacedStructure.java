package elc.florian.mcity.structure;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlacedStructure {
    public int id;
    public StructureKind kind;
    public int rotation; // 0, 1, 2, 3 (multiplié par 90°) - 0 pour lignes
    public BlockPos origin;   // coin minimum du bounding box (pour bâtiments)
    public BlockPos lineFrom; // pour lignes
    public BlockPos lineTo;   // pour lignes

    // Map position → état précédent (pour pouvoir restaurer à la suppression)
    public Map<BlockPos, BlockState> previousStates = new HashMap<>();

    // Vue sur les positions occupées — même set que previousStates.keySet()
    public Set<BlockPos> blocks = new HashSet<>();

    public boolean contains(BlockPos pos) {
        return blocks.contains(pos) || blocks.contains(pos.down()) || blocks.contains(pos.up());
    }

    public boolean intersects(Set<BlockPos> other) {
        for (BlockPos p : other) {
            if (blocks.contains(p)) return true;
        }
        return false;
    }

    public BlockPos getCenter() {
        if (kind.isLine && lineFrom != null && lineTo != null) {
            return new BlockPos(
                    (lineFrom.getX() + lineTo.getX()) / 2,
                    Math.max(lineFrom.getY(), lineTo.getY()),
                    (lineFrom.getZ() + lineTo.getZ()) / 2
            );
        }
        if (origin != null) {
            return origin.add(kind.width / 2, kind.height / 2, kind.depth / 2);
        }
        return BlockPos.ORIGIN;
    }
}
