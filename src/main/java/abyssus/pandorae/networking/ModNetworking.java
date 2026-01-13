package abyssus.pandorae.networking;

import abyssus.pandorae.AbyssusPandorae;
import abyssus.pandorae.client.Skills.SkillActions;
import abyssus.pandorae.component.Kingdom;
import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.gui.stats.AbilityData;
import abyssus.pandorae.util.AbilityLoader;
import abyssus.pandorae.util.KingdomTagManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ModNetworking {
    public static void register() {
        // C2S Payloads
        PayloadTypeRegistry.playC2S().register(KingdomSelectPayload.ID, KingdomSelectPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AbilityPurchasePayload.ID, AbilityPurchasePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AbilityActionPayload.ID, AbilityActionPayload.CODEC);
        // S2C Payloads
        PayloadTypeRegistry.playS2C().register(OpenKingdomScreenPayload.ID, OpenKingdomScreenPayload.CODEC);


        //Handle kingdom selection
        ServerPlayNetworking.registerGlobalReceiver(KingdomSelectPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                Kingdom selectedKingdom = payload.kingdom();

                var component = ModComponents.KINGDOM.get(player);
                component.setKingdom(selectedKingdom); // Save the data to Cardinal Components

                KingdomTagManager.updatePlayerDisplay(player, selectedKingdom, component.getSoulState());
                // send a message to the player confirming it worked
                player.sendMessage(Text.translatable("abyssus-pandorae.choose_kingdom.message").append(Text.translatable(selectedKingdom.getTranslationKey()).formatted(Formatting.AQUA)), false);
                ModComponents.KINGDOM.sync(player);
            });
        });

        // Handle Ability purchase
        ServerPlayNetworking.registerGlobalReceiver(AbilityPurchasePayload.ID, ((payload, context) -> {
            context.server().execute(()-> {
                var player = context.player();
                var component = ModComponents.KINGDOM.get(player);
                String slotId = payload.slotId();

                // LOad the abilities via the loader
                List<AbilityData> abilities = AbilityLoader.loadForKingdom(component.getKingdom());

                // check if purchased already
                AbilityData ability = abilities.stream()
                        .filter(a -> a.id().equals(slotId))
                        .findFirst()
                        .orElse(null);

                if (ability == null) return;

                boolean prereqMet = ability.prerequisites().isEmpty() || ability.prerequisites().stream().allMatch(component::hasAbility);
                boolean hasFaithThreshold = component.getFaith() >= ability.cost();
                boolean hasConflict = ability.conflicts() != null && ability.conflicts().stream().anyMatch(component::hasAbility);

                if (prereqMet && hasFaithThreshold && !hasConflict && !component.hasAbility(slotId)) {
                    component.purchaseAbility(slotId);

                    SkillActions.refreshAttributes(player);

                    player.sendMessage(Text.translatable("text.abyssus-pandorae.prefix").formatted(Formatting.GOLD).append(Text.translatable("text.abyssus-pandorae.ability_mastered", ability.name()).formatted(Formatting.GREEN)), true);

                    ModComponents.KINGDOM.sync(player);
                }
            });
        }));

        // Handle Ability Use
        ServerPlayNetworking.registerGlobalReceiver(AbilityActionPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                var component = ModComponents.KINGDOM.get(player);
                String actionId = payload.actionId();

                // Does the player actually own a skill that uses this action?
                boolean hasAbility = component.getPurchasedAbilities().stream().map(AbilityLoader::get).anyMatch(data -> data != null && data.actionId().equals(actionId));

                if (hasAbility) {
                    SkillActions.execute(actionId, player);
                } else {
                    AbyssusPandorae.LOGGER.warn("Player {} tried to use unowned ability: {}", player.getName().getString(), actionId);
                }
            });
        }));

        // Handle Player Join
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            server.execute(() -> {
                ServerPlayerEntity player = handler.getPlayer();
                var component = ModComponents.KINGDOM.get(player);

                KingdomTagManager.updatePlayerDisplay(player, component.getKingdom(), component.getSoulState());

                // Refresh attribues on join
                SkillActions.refreshAttributes(player);

                // If they havent picked a kingdom (the default "none"), tell them to open the screen
                if (component.getKingdom() == Kingdom.NONE) {
                    ServerPlayNetworking.send(player, new OpenKingdomScreenPayload());
                }
            });
        }));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive)-> {
            var component = ModComponents.KINGDOM.get(newPlayer);

            KingdomTagManager.updatePlayerDisplay(newPlayer, component.getKingdom(), component.getSoulState());

            SkillActions.refreshAttributes(newPlayer);
        });
    }
}
