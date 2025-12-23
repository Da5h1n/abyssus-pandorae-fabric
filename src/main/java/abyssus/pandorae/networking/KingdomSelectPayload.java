package abyssus.pandorae.networking;

import abyssus.pandorae.AbyssusPandorae;
import abyssus.pandorae.component.Kingdom;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import static abyssus.pandorae.component.ModComponents.KINGDOM;

public record KingdomSelectPayload(Kingdom kingdom) implements CustomPayload {
    public static final Id<KingdomSelectPayload> ID = new Id<>(AbyssusPandorae.identifier("select_kingdom"));

    public static final PacketCodec<RegistryByteBuf, KingdomSelectPayload> CODEC = PacketCodec.tuple(Kingdom.CODEC, KingdomSelectPayload::kingdom, KingdomSelectPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {return ID;}
}
