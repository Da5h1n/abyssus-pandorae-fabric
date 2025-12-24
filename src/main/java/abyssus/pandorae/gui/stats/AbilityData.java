package abyssus.pandorae.gui.stats;

import java.util.List;

public record AbilityData(
        String id,
        String name,
        int cost,
        List<String> prerequisites,
        String action_type
) {}
