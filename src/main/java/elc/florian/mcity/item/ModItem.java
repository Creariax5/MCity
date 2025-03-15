package elc.florian.mcity.item;

import elc.florian.mcity.MCity;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;

import static net.minecraft.item.Items.register;

public class ModItem {
    public static final Item ROAD = registerItem("road");
    public static final Item AREA = registerItem("area");
    public static final Item WATER = registerItem("water");
    public static final Item ELECTRICITY = registerItem("electricity");

    private static Item registerItem(String name) {
        return register(name, Item::new, new Item.Settings());
    }

    public static void registerModItems() {
        MCity.LOGGER.info("Registering Mod Items for " + MCity.MOD_ID);
    }

}
