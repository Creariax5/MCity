package elc.florian.mcity.tools;

import elc.florian.mcity.state.Tools;
import elc.florian.mcity.structure.ZoneRegistry;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class AreaTool extends Tool {

    private static SubType zoneSubType(String name, int color, Tools.AreaType type) {
        return new BuildingSubType(name, color, pos -> placeZone(pos, type));
    }

    private static void placeZone(BlockPos pos, Tools.AreaType type) {
        int tx = ZoneRegistry.blockToTile(pos.getX());
        int tz = ZoneRegistry.blockToTile(pos.getZ());
        boolean erase = type == Tools.AreaType.DEZONNAGE;
        if (Tools.zoneFillMode) {
            if (erase) ZoneRegistry.floodClear(tx, tz);
            else ZoneRegistry.floodFill(tx, tz, type);
        } else {
            if (erase) ZoneRegistry.removeZone(tx, tz);
            else ZoneRegistry.setZone(tx, tz, type);
        }
    }

    private static final List<SubType> SUB_TYPES = List.of(
            zoneSubType("Habitation", 0xFF44AA44, Tools.AreaType.HABITATION),
            zoneSubType("Commerce",   0xFF4488DD, Tools.AreaType.COMMERCE),
            zoneSubType("Industrie",  0xFFDDAA44, Tools.AreaType.INDUSTRIE),
            zoneSubType("Dezonnage",  0xFF888888, Tools.AreaType.DEZONNAGE)
    );

    public AreaTool() {
        super("Zone", Items.OAK_DOOR);
    }

    @Override
    public List<SubType> subTypes() { return SUB_TYPES; }
}
