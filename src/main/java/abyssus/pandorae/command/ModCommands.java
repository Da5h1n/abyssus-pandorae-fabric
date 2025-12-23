package abyssus.pandorae.command;

import abyssus.pandorae.component.Kingdom;
import abyssus.pandorae.component.KingdomComponent;
import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.component.SoulState;
import abyssus.pandorae.util.KingdomTagManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("kingdom")
                    .then(literal("check")
                            .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                return sendProfile(context.getSource(), target);
                            })))
                    .then(literal("setKingdom")
                            .then(argument("player", EntityArgumentType.player())
                                    .then(argument("name", StringArgumentType.word())
                                            .suggests(((context, builder) -> {
                                                for (Kingdom k : Kingdom.values()) {
                                                    builder.suggest(k.asString());
                                                }
                                                return builder.buildFuture();
                                            }))
                                            .executes(context -> {
                                        String input = StringArgumentType.getString(context, "name").toUpperCase();
                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                        try {
                                            Kingdom kingdom = Kingdom.valueOf(input);
                                            ModComponents.KINGDOM.get(target).setKingdom(kingdom);
                                            KingdomTagManager.updatePlayerDisplay(target, kingdom);

                                            // Text.translatable as feedback
                                            context.getSource().sendFeedback(() -> Text.literal("Set kingdom for " + target.getName().getString() + " to ").append(Text.translatable(kingdom.getTranslationKey())),true);
                                            return 1;
                                        } catch (IllegalArgumentException e) {
                                            context.getSource().sendError(Text.literal("Invalid Kingdom!"));
                                            return 0;
                                        }
                                    }))))
                    .then(literal("setfaith")
                            .then(argument("player", EntityArgumentType.player())
                                    .then(argument("amount", IntegerArgumentType.integer(0,200)).executes(context -> {
                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                        ModComponents.KINGDOM.get(target).setFaith(amount);
                                        context.getSource().sendFeedback(()-> Text.literal("Set faith for " + target.getName().getString() + " to " + amount),true);
                                        return 1;
                                    }))))
                    .then(literal("setsoul")
                            .then(argument("player", EntityArgumentType.player())
                                    .then(argument("stateName", StringArgumentType.word())
                                    // this part add tab completion
                                            .suggests((context, builder) -> {
                                                for (SoulState state : SoulState.values()) {
                                                    builder.suggest(state.name().toLowerCase());
                                                }
                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {
                                                String input = StringArgumentType.getString(context, "stateName").toUpperCase();
                                                ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                                try {
                                                    SoulState state = SoulState.valueOf(input);
                                                    ModComponents.KINGDOM.get(target).setSoulState(state);

                                                    context.getSource().sendFeedback(()-> Text.literal("Set soul for " + target.getName().getString() + " to " + state.getDisplayName()), true);
                                                } catch (IllegalArgumentException e) {
                                                    context.getSource().sendError(Text.literal("Invalid Soul State! Try: fractured, protected, rekindled, or incurable"));
                                                }
                                                return 1;
                                            }))))

            );

        }));
    }

    private static int sendProfile(ServerCommandSource source, ServerPlayerEntity target) {
        KingdomComponent component = ModComponents.KINGDOM.get(target);

        // Build header
        Text header = Text.literal("§b--- ").append(target.getName()).append(" §b---");

        // Build Kingdom line
        Text kingdomLine = Text.literal("\n§7Kingdom: §f")
                .append(Text.translatable(component.getKingdom().getTranslationKey()));

        // Build Soul line
        Text soulLine = Text.literal("\n§7Soul: §e")
                .append(Text.literal(component.getSoulState().getDisplayName()));

        //Build faith line
        Text faithline = Text.literal("\n§7Faith: §a" + component.getFaith() + "/200");

        //combine
        Text finalProfile = Text.empty()
                .append(header)
                .append(kingdomLine)
                .append(soulLine)
                .append(faithline);

        source.sendFeedback(() -> finalProfile, false);
        return 1;
    }
}
