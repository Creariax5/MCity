package elc.florian.mcity.tools;

import elc.florian.mcity.client.BuildingPlacer;
import elc.florian.mcity.client.RoadPlacer;
import net.minecraft.item.Items;

import java.util.List;

public class WaterTool extends Tool {
    private static final List<SubType> SUB_TYPES = List.of(
            new BuildingSubType("Puits",     0xFF3377AA, pos -> BuildingPlacer.placePuits(pos, 0)),
            new LineSubType    ("Canalisation", 0xFFCC8844, RoadPlacer::placeCanalisation),
            new BuildingSubType("Reservoir", 0xFF446688, pos -> BuildingPlacer.placeReservoir(pos, 0))
    );

    public WaterTool() {
        super("Eau", Items.WATER_BUCKET);
    }

    @Override
    public List<SubType> subTypes() { return SUB_TYPES; }
}
