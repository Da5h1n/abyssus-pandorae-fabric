package abyssus.pandorae.networking;

import abyssus.pandorae.component.Kingdom;
import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.gui.stats.AbilityData;
import abyssus.pandorae.util.AbilityLoader;
import abyssus.pandorae.util.KingdomTagManager;
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
                Kingdom selectedKingdom = payload.kingdom();
                ModComponents.KINGDOM.get(context.player()).setKingdom(selectedKingdom);// Save the data to Cardinal Components
                ServerPlayerEntity player = context.player();
                KingdomTagManager.updatePlayerDisplay(player, selectedKingdom);
                // send a message to the player confirming it worked
                context.player().sendMessage(Text.literal("You have joined the ").append(Text.translatable(selectedKingdom.getTranslationKey()).formatted(Formatting.AQUA)), false);
                ModComponents.KINGDOM.sync(context.player());
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

                boolean prereqMet = ability.prerequisites().isEmpty() || ability.prerequisites().stream().allMatch(component::hasAbility);

                if (prereqMet && component.getFaith() >= ability.cost() && !component.hasAbility(slotId)) {
                    component.setFaith(component.getFaith() - ability.cost());
                    component.purchaseAbility(slotId);
                    player.sendMessage(Text.literal("§6[Abyssus] §aUnlocked " + ability.name() + "!"), true);
                } else if (!prereqMet) {
                    player.sendMessage(Text.literal("§cYou must unlock the previous ability first!"), true);
                } else {
                    player.sendMessage(Text.literal("§cPurchase failed."), true);
                }
            });
        }));

        // register the S2C packet
        PayloadTypeRegistry.playS2C().register(OpenKingdomScreenPayload.ID, OpenKingdomScreenPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            server.execute(() -> {
                var player = handler.getPlayer();
                Kingdom currentKingdom = ModComponents.KINGDOM.get(player).getKingdom();

                KingdomTagManager.updatePlayerDisplay(player, currentKingdom);

                // If they havent picked a kingdom (the default "none"), tell them to open the screen
                if (currentKingdom == Kingdom.NONE) {
                    ServerPlayNetworking.send(player, new OpenKingdomScreenPayload());
                }
            });
        }));
    }
}
