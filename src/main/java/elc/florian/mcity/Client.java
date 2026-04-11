package elc.florian.mcity;

import elc.florian.mcity.client.BuildingPreview;
import elc.florian.mcity.state.Tools;
import elc.florian.mcity.tools.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ToolRegistry.register(Tools.ToolType.ROAD,        new RoadTool());
        ToolRegistry.register(Tools.ToolType.AREA,        new AreaTool());
        ToolRegistry.register(Tools.ToolType.WATER,       new WaterTool());
        ToolRegistry.register(Tools.ToolType.ELECTRICITY, new ElectricityTool());

        WorldRenderEvents.AFTER_TRANSLUCENT.register(BuildingPreview::render);
    }
}
