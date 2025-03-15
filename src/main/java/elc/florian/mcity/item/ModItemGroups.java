package elc.florian.mcity.item;

import elc.florian.mcity.MCity;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup ROAD_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(MCity.MOD_ID, "roads"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.roads"))
                    .icon(() -> new ItemStack(ModItem.ROAD)).entries((displayContext, entries) -> {
                        entries.add(ModItem.ROAD);
                        entries.add(ModItem.AREA);
                        entries.add(ModItem.WATER);
                        entries.add(ModItem.ELECTRICITY);
                    }).build());

    public static void registerItemGroups() {
        MCity.LOGGER.info("Registering Item Groups for " + MCity.MOD_ID);
    }
}
