package abyssus.pandorae.networking;

import abyssus.pandorae.AbyssusPandorae;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record OpenKingdomScreenPayload() implements CustomPayload {
    public static final Id<OpenKingdomScreenPayload> ID = new Id<>(AbyssusPandorae.identifier("open_screen"));
    public static final PacketCodec<RegistryByteBuf, OpenKingdomScreenPayload> CODEC = PacketCodec.unit(new OpenKingdomScreenPayload());

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
