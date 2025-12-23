package abyssus.pandorae.component;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;

public enum Kingdom implements StringIdentifiable {

    NONE("none","abyssus-pandorae.kingdom.none"),
    LEFT("left","abyssus-pandorae.kingdom.left_kingdom"),
    CENTER("center","abyssus-pandorae.kingdom.center_kingdom"),
    RIGHT("right","abyssus-pandorae.kingdom.right_kingdom");

    private final String id;
    private final String translationKey;

    Kingdom(String id,String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public String asString() {
        return this.id;
    }

    public static final PacketCodec<RegistryByteBuf, Kingdom> CODEC = PacketCodecs.codec(StringIdentifiable.createCodec(Kingdom::values)).cast();
}
