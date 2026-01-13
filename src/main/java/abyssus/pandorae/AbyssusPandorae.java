package abyssus.pandorae;

import abyssus.pandorae.client.Skills.SkillActions;
import abyssus.pandorae.command.ModCommands;
import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.gui.config.ModConfig;
import abyssus.pandorae.gui.stats.AbilityData;
import abyssus.pandorae.item.ModItemGroups;
import abyssus.pandorae.item.ModItems;
import abyssus.pandorae.networking.ModNetworking;
import abyssus.pandorae.util.AbilityLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbyssusPandorae implements ModInitializer {
	public static final String MOD_ID = "abyssus-pandorae";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ModConfig config = ModConfig.load();

	@Override
	public void onInitialize() {
        ModItems.registerModItems();

        ModItemGroups.registerItemGroups();

        ModCommands.register();
        ModNetworking.register();

        SkillActions.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // get kingdom component
                var component = ModComponents.KINGDOM.get(player);

                // Loop through all purchased abilities
                for (String abilityId : component.getPurchasedAbilities()) {
                    AbilityData data = AbilityLoader.get(abilityId);

                    if (data != null && data.type() == AbilityData.AbilityType.PASSIVE) {
                        SkillActions.tickPassive(data.actionId(), player);
                    }
                }
            }
        });

    }

    public static Identifier identifier(String path) {
        return Identifier.of(AbyssusPandorae.MOD_ID, path);
    }
}