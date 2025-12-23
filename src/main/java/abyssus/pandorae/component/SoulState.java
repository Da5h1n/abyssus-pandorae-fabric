package abyssus.pandorae.component;

import net.minecraft.util.StringIdentifiable;

public enum SoulState implements StringIdentifiable {
    FRACTURED("fractured","Fractured Soul"),
    PROTECTED("protected","Protected Soul"),
    REKINDLED("rekindled","Rekindled Soul"),
    INCURABLE("incurable","Incurable Soul");

    private final String id;
    private final String displayName;

    SoulState(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String asString() {return this.id;}
}
