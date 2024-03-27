package elc.florian.mcity;

import elc.florian.mcity.client.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;


public class Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //HudRenderCallback.EVENT.register(new HudOverlay());

    }
}
