package elc.florian.mcity.item;

import elc.florian.mcity.MCity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItem {
    public static final Item ROAD = registerItem("road", Item::new, new Item.Settings());
    public static final Item AREA = registerItem("area", Item::new, new Item.Settings());
    public static final Item WATER = registerItem("water", Item::new, new Item.Settings());
    public static final Item ELECTRICITY = registerItem("electricity", Item::new, new Item.Settings());

    private static Item registerItem(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MCity.MOD_ID, name));
        return Items.register(key, factory, settings);
    }

    public static void registerModItems() {
        MCity.LOGGER.info("Registering Mod Items for " + MCity.MOD_ID);
    }
}
