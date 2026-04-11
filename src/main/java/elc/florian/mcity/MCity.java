package elc.florian.mcity;

import elc.florian.mcity.client.Keybindings;
import elc.florian.mcity.item.ModItem;
import elc.florian.mcity.item.ModItemGroups;
import elc.florian.mcity.structure.MCityPersistentState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCity implements ModInitializer {
    public static final String MOD_ID = "mcity";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("MCity loading...");
        ModItem.registerModItems();
        ModItemGroups.registerItemGroups();
        Keybindings.registerKeybindings();

        // Au démarrage : charger l'état persistant dans les registres
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                MCityPersistentState.get(server.getOverworld()).restore()
        );
        // Avant la sauvegarde du monde : snapshot des registres dans l'état persistant
        ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) ->
                MCityPersistentState.get(server.getOverworld()).snapshot()
        );
    }
}
