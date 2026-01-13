package abyssus.pandorae.gui.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record AbilityData(
        String id,
        String name,
        String description,
        int cost,
        List<String> prerequisites,
        List<String> conflicts,
        String kingdom,
        int gridX,
        int gridY,
        AbilityType type,
        String actionId
) {
    public enum AbilityType { PASSIVE, ACTIVE }

    public static final Codec<AbilityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(AbilityData::id),
            Codec.STRING.fieldOf("name").forGetter(AbilityData::name),
            Codec.STRING.optionalFieldOf("description", "No description provided").forGetter(AbilityData::description),
            Codec.INT.fieldOf("cost").forGetter(AbilityData::cost),
            Codec.STRING.listOf().fieldOf("prerequisites").forGetter(AbilityData::prerequisites),
            Codec.STRING.listOf().fieldOf("conflicts").forGetter(AbilityData::conflicts),
            Codec.STRING.fieldOf("kingdom").forGetter(AbilityData::kingdom),
            Codec.INT.fieldOf("gridX").forGetter(AbilityData::gridX),
            Codec.INT.fieldOf("gridY").forGetter(AbilityData::gridY),
            Codec.STRING.fieldOf("type").xmap(AbilityType::valueOf, AbilityType::name).forGetter(AbilityData::type),
            Codec.STRING.fieldOf("actionId").forGetter(AbilityData::actionId)
    ).apply(instance, AbilityData::new));
}
