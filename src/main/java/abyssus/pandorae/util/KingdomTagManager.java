package abyssus.pandorae.util;

import abyssus.pandorae.AbyssusPandorae;
import abyssus.pandorae.component.Kingdom;
import abyssus.pandorae.component.SoulState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mutable;

public class KingdomTagManager {

    public static final Identifier ICON_FONT = Identifier.of(AbyssusPandorae.MOD_ID, "icon_font");

    public static void updatePlayerDisplay(ServerPlayerEntity player, Kingdom kingdom, SoulState state) {
        Scoreboard scoreboard = player.getEntityWorld().getScoreboard();

        String teamName = "plt_" + player.getUuidAsString().substring(0, 16);
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.addTeam(teamName);
        }

        MutableText soulIcon = Text.literal(state.getIconChar()).formatted(Formatting.WHITE);

        team.setPrefix(soulIcon);

        team.setColor(kingdom.getColour());

        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);

    }
}