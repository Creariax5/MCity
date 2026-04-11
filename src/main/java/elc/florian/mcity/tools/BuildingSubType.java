package elc.florian.mcity.tools;

import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

/** Sous-type qui pose un bâtiment unique en un clic. */
public class BuildingSubType extends SubType {
    private final Consumer<BlockPos> placer;

    public BuildingSubType(String name, int color, Consumer<BlockPos> placer) {
        super(name, color);
        this.placer = placer;
    }

    @Override
    public void onClick(BlockPos pos) {
        placer.accept(pos);
    }
}
