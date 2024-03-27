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
    public static final ItemGroup RUBY_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(MCity.MOD_ID, "road"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.road"))
                    .icon(() -> new ItemStack(ModItem.ROAD)).entries((displayContext, entries) -> {
                        entries.add(ModItem.ROAD);

                        entries.add(Items.DIAMOND);


                    }).build());


    public static void registerItemGroups() {
        MCity.LOGGER.info("Registering Item Groups for " + MCity.MOD_ID);
    }

}
