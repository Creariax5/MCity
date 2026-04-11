package elc.florian.mcity.tools;

import elc.florian.mcity.client.RoadPlacer;
import net.minecraft.item.Items;

import java.util.List;

public class RoadTool extends Tool {
    private static final List<SubType> SUB_TYPES = List.of(
            new LineSubType("Route", 0xFF666666, RoadPlacer::placeRoad)
    );

    public RoadTool() {
        super("Route", Items.COBBLESTONE);
    }

    @Override
    public List<SubType> subTypes() { return SUB_TYPES; }
}
