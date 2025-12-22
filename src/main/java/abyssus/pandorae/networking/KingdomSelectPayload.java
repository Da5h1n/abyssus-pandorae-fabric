package abyssus.pandorae.networking;

import abyssus.pandorae.AbyssusPandorae;
import net.minecraft.block.AbstractBlock;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record KingdomSelectPayload(String kingdomId) implements CustomPayload {
    public static final Id<KingdomSelectPayload> ID = new Id<>(AbyssusPandorae.identifier("select_kingdom"));
    public static final PacketCodec<RegistryByteBuf, KingdomSelectPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, KingdomSelectPayload::kingdomId, KingdomSelectPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {return ID;}
}
