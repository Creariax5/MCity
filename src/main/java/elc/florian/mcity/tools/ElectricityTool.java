package elc.florian.mcity.tools;

import elc.florian.mcity.client.BuildingPlacer;
import elc.florian.mcity.client.RoadPlacer;
import net.minecraft.item.Items;

import java.util.List;

public class ElectricityTool extends Tool {
    private static final List<SubType> SUB_TYPES = List.of(
            new BuildingSubType("Generateur", 0xFFAA3333, pos -> BuildingPlacer.placeGenerateur(pos, 0)),
            new LineSubType    ("Cable",      0xFFCC4444, RoadPlacer::placeCable),
            new BuildingSubType("Tour relais", 0xFF884422, pos -> BuildingPlacer.placeTourRelais(pos, 0))
    );

    public ElectricityTool() {
        super("Electricite", Items.REDSTONE);
    }

    @Override
    public List<SubType> subTypes() { return SUB_TYPES; }
}
