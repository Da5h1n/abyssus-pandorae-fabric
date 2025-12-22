package abyssus.pandorae;

import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.networking.KingdomSelectPayload;
import abyssus.pandorae.networking.OpenKingdomScreenPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public class AbyssusPandorae implements ModInitializer {
	public static final String MOD_ID = "abyssus-pandorae";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        // register packet type
        PayloadTypeRegistry.playC2S().register(KingdomSelectPayload.ID, KingdomSelectPayload.CODEC);

        //Handle packet when it arives at the server
        ServerPlayNetworking.registerGlobalReceiver(KingdomSelectPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                // Save the data to Cardinal Components
                ModComponents.KINGDOM.get(context.player()).setKingdom(payload.kingdomId());

                // send a message to the player confirming it worked
                context.player().sendMessage(Text.literal("You have joined the " + payload.kingdomId()),false);
            });
        });

        // Register the command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("kingdom")
                    .then(literal("check").executes(context -> {
                        // Get the kingdom from the component
                        String kingdom = ModComponents.KINGDOM.get(context.getSource().getPlayer()).getKingdom();

                        context.getSource().sendFeedback(() -> Text.literal("Current Kingdom: §6" + kingdom), false);
                        return 1;
                    })));
        });

        // register the S2C packet
        PayloadTypeRegistry.playS2C().register(OpenKingdomScreenPayload.ID, OpenKingdomScreenPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            server.execute(() -> {
                var player = handler.getPlayer();
                String currentKingdom = ModComponents.KINGDOM.get(player).getKingdom();

                // If they havent picked a kingdom (the default "none"), tell them to open the screen
                if ("none".equals(currentKingdom)) {
                    ServerPlayNetworking.send(player, new OpenKingdomScreenPayload());
                }
            });
        }));

    }

    public static Identifier identifier(String path) {
        return Identifier.of(AbyssusPandorae.MOD_ID, path);
    }
}