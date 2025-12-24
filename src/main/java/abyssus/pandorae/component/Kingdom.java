package abyssus.pandorae.component;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

public enum Kingdom implements StringIdentifiable {


    NONE("none","abyssus-pandorae.kingdom.none", Formatting.GRAY),
    LEFT("left","abyssus-pandorae.kingdom.left_kingdom", Formatting.RED),
    CENTER("center","abyssus-pandorae.kingdom.center_kingdom", Formatting.GREEN),
    RIGHT("right","abyssus-pandorae.kingdom.right_kingdom", Formatting.BLUE);

    private final String id;
    private final String translationKey;
    private final Formatting colour;

    Kingdom(String id, String translationKey, Formatting colour) {
        this.id = id;
        this.translationKey = translationKey;
        this.colour = colour;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Formatting getColour() {
        return colour;
    }

    @Override
    public String asString() {
        return this.id;
    }

    public static final PacketCodec<RegistryByteBuf, Kingdom> CODEC = PacketCodecs.codec(StringIdentifiable.createCodec(Kingdom::values)).cast();
}
