package abyssus.pandorae.networking;

import abyssus.pandorae.component.Kingdom;
import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.util.KingdomTagManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModNetworking {
    public static void register() {
        // register packet type
        PayloadTypeRegistry.playC2S().register(KingdomSelectPayload.ID, KingdomSelectPayload.CODEC);

        //Handle packet when it arives at the server
        ServerPlayNetworking.registerGlobalReceiver(KingdomSelectPayload.ID, (payload, context) -> {
            context.server().execute(() -> {

                Kingdom selectedKingdom = payload.kingdom();
                // Save the data to Cardinal Components
                ModComponents.KINGDOM.get(context.player()).setKingdom(selectedKingdom);

                ServerPlayerEntity player = context.player();
                KingdomTagManager.updatePlayerDisplay(player, selectedKingdom);

                // send a message to the player confirming it worked
                context.player().sendMessage(Text.literal("You have joined the ").append(Text.translatable(selectedKingdom.getTranslationKey()).formatted(Formatting.AQUA)), false);

                ModComponents.KINGDOM.sync(context.player());
            });
        });

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
