package abyssus.pandorae.item;

import abyssus.pandorae.AbyssusPandorae;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(AbyssusPandorae.MOD_ID, "items"),
            FabricItemGroup.builder().icon(()-> new ItemStack(ModItems.PANDORA_BLESSING))
                    .displayName(Text.translatable("itemgroup.abyssus.items"))
                    .entries((displayContext, entries) -> {

                        entries.add(ModItems.PANDORA_BLESSING);

                    }).build());

    public static void registerItemGroups() {
        AbyssusPandorae.LOGGER.info("Registering Item Groups for " + AbyssusPandorae.MOD_ID);
    }
}
