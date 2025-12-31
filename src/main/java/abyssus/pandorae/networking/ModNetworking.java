package abyssus.pandorae.networking;

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
        // register Kingdom selection payload
        PayloadTypeRegistry.playC2S().register(KingdomSelectPayload.ID, KingdomSelectPayload.CODEC);

        //Handle kingdom selection payload
        ServerPlayNetworking.registerGlobalReceiver(KingdomSelectPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                Kingdom selectedKingdom = payload.kingdom();

                var component = ModComponents.KINGDOM.get(player);
                component.setKingdom(selectedKingdom); // Save the data to Cardinal Components

                KingdomTagManager.updatePlayerDisplay(player, selectedKingdom, component.getSoulState());
                // send a message to the player confirming it worked
                context.player().sendMessage(Text.literal("You have joined the ").append(Text.translatable(selectedKingdom.getTranslationKey()).formatted(Formatting.AQUA)), false);
                ModComponents.KINGDOM.sync(player);
            });
        });

        //register client purchase payload
        PayloadTypeRegistry.playC2S().register(AbilityPurchasePayload.ID, AbilityPurchasePayload.CODEC);

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

                boolean prereqMet = ability.prerequisites().isEmpty() ||
                        ability.prerequisites().stream().allMatch(component::hasAbility);
                boolean hasFaithThreshold = component.getFaith() >= ability.cost();
                boolean hasConflict = ability.conflicts() != null && ability.conflicts().stream().anyMatch(component::hasAbility);

                if (prereqMet && hasFaithThreshold && !hasConflict && !component.hasAbility(slotId)) {
                    component.purchaseAbility(slotId);
                    player.sendMessage(Text.literal("§6[Abyssus] §aYou have mastered " + ability.name() + "!"), true);
                } else if (!prereqMet) {
                    player.sendMessage(Text.literal("§cYou must unlock the previous ability first!"), true);
                } else if (!hasFaithThreshold) {
                    player.sendMessage(Text.literal("§Your Faith (" + component.getFaith() + "/" + ability.cost() + ") is too low to unlock this!"), true);
                }
            });
        }));

        // register the S2C packet
        PayloadTypeRegistry.playS2C().register(OpenKingdomScreenPayload.ID, OpenKingdomScreenPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            server.execute(() -> {
                ServerPlayerEntity player = handler.getPlayer();
                var component = ModComponents.KINGDOM.get(player);
                Kingdom currentKingdom = component.getKingdom();

                KingdomTagManager.updatePlayerDisplay(player, currentKingdom, component.getSoulState());

                // If they havent picked a kingdom (the default "none"), tell them to open the screen
                if (currentKingdom == Kingdom.NONE) {
                    ServerPlayNetworking.send(player, new OpenKingdomScreenPayload());
                }
            });
        }));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive)-> {
            var component = ModComponents.KINGDOM.get(newPlayer);

            KingdomTagManager.updatePlayerDisplay(
                    newPlayer,
                    component.getKingdom(),
                    component.getSoulState()
            );
        });
    }
}
