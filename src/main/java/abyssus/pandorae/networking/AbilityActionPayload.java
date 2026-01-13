package abyssus.pandorae.networking;

import abyssus.pandorae.AbyssusPandorae;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record AbilityActionPayload(String actionId) implements CustomPayload {
    public static final Id<AbilityActionPayload> ID = new Id<>(AbyssusPandorae.identifier("ability_action"));

    public static final PacketCodec<RegistryByteBuf, AbilityActionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, AbilityActionPayload::actionId,
            AbilityActionPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
