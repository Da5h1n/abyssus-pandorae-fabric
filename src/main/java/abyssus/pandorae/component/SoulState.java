package abyssus.pandorae.component;

import net.minecraft.util.StringIdentifiable;

public enum SoulState implements StringIdentifiable {
    PROTECTED("protected","Protected Soul", "\uE001"),
    FRACTURED("fractured","Fractured Soul", "\uE002"),
    REKINDLED("rekindled","Rekindled Soul", "\uE003"),
    INCURABLE("incurable","Incurable Soul", "\uE004");

    private final String id;
    private final String displayName;
    private final String iconChar;

    SoulState(String id, String displayName, String iconChar) {
        this.id = id;
        this.displayName = displayName;
        this.iconChar = iconChar;
    }

    public String getDisplayName() {
        return displayName;
    }
    public String getIconChar() { return iconChar; }

    @Override
    public String asString() {return this.id;}
}
