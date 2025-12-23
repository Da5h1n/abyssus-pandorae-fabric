package abyssus.pandorae.mixin;

import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.util.KingdomTagManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    // This injects into the copyFrom method, which runs when a player respawns or returns from the end
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void abyssus$afterRespawn(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity newPlayer = (ServerPlayerEntity) (Object) this;

        // Re-apply the tag and team color to the new player body
        var kingdom = ModComponents.KINGDOM.get(newPlayer).getKingdom();
        KingdomTagManager.updatePlayerDisplay(newPlayer, kingdom);
    }

    // This runs when the player finishes changing dimensions
// 2. Handle Dimension Changes & Teleports (Updated for 1.21.1)
    @Inject(method = "teleportTo", at = @At("TAIL"))
    private void abyssus$afterTeleport(TeleportTarget teleportTarget, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        var kingdom = ModComponents.KINGDOM.get(player).getKingdom();

        // Re-apply the display logic in the new location/dimension
        KingdomTagManager.updatePlayerDisplay(player, kingdom);
    }

}
