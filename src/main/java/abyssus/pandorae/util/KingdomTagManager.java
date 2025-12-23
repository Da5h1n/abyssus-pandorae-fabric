package abyssus.pandorae.util;

import abyssus.pandorae.component.Kingdom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;

public class KingdomTagManager {

    public static void updatePlayerDisplay(ServerPlayerEntity player, Kingdom kingdom) {
        // Handle name colour
        Scoreboard scoreboard = player.getEntityWorld().getScoreboard();
        String teamName = "kingdom_" + kingdom.asString();
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.addTeam(teamName);
        }

        team.setColor(kingdom.getColour());
        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);

        // Show kingdom tag
//        spawnKingdomTag(player, kingdom);
    }

//    private static void spawnKingdomTag(ServerPlayerEntity player, Kingdom kingdom) {
//        removeOldTags(player);
//
//        if (kingdom == Kingdom.NONE) return;
//
//        ServerWorld world = (ServerWorld) player.getEntityWorld();
//
//        DisplayEntity.TextDisplayEntity display = EntityType.TEXT_DISPLAY.create(world, SpawnReason.TRIGGERED);
//        if (display != null) {
//            display.setText(Text.translatable(kingdom.getTranslationKey()).formatted(kingdom.getColour()));
//            display.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
//            display.setNoGravity(true);
//            display.setInvisible(false);
//            display.setGlowing(false);
//
//            // tag follow player
//            display.startRiding(player);
//            world.spawnEntity(display);
//        }
//    }

    private static void removeOldTags(ServerPlayerEntity player) {
        for (Entity passenger : player.getPassengerList()) {
            if (passenger instanceof DisplayEntity.TextDisplayEntity) {
                passenger.discard();
            }
        }
    }

}
