package abyssus.pandorae.item;

import abyssus.pandorae.AbyssusPandorae;
import abyssus.pandorae.item.custom.SoulManipulatorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {

    public static final Item PANDORA_BLESSING = registerItem("pandora_blessing", SoulManipulatorItem::new, new Item.Settings().maxCount(1).rarity(Rarity.EPIC));

    public static Item registerItem(String name, java.util.function.Function<Item.Settings, Item> factory, Item.Settings settings) {
        Identifier id = Identifier.of(AbyssusPandorae.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = factory.apply(settings.registryKey(key));

        return Registry.register(Registries.ITEM, key, item);
    }

    public static void registerModItems() {
        AbyssusPandorae.LOGGER.info("Registering Mod items for " + AbyssusPandorae.MOD_ID);
    }
}
