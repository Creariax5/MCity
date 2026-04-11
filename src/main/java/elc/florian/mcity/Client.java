package elc.florian.mcity;

import elc.florian.mcity.client.BuildingPreview;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(BuildingPreview::render);
    }
}
