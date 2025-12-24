package abyssus.pandorae.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AbilityPurchasePayload(String slotId) implements CustomPayload {
    public static final Id<AbilityPurchasePayload> ID = new Id<>(Identifier.of("abyssus-pandorae", "ability_purchase"));
    public static final PacketCodec<RegistryByteBuf, AbilityPurchasePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, AbilityPurchasePayload::slotId,
            AbilityPurchasePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
