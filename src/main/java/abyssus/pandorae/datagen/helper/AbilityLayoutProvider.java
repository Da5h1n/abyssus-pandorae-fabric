package abyssus.pandorae.datagen.helper;

import org.joml.Vector2i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityLayoutProvider {
    private final Map<String, Vector2i> coords = new HashMap<>();

    public void generateLayout(List<AbilityDataBuilder> builders) {
        // Find root nodes (no prerequisites)
        List<AbilityDataBuilder> roots = builders.stream()
                .filter(b -> b.getPrerequisites().isEmpty())
                .toList();

        int currentX = 0;
        for (AbilityDataBuilder root : roots) {
            // spread horisontally
            assignCoords(root, builders, currentX, 0);
            currentX += 2;
        }
    }

    private void assignCoords(AbilityDataBuilder node, List<AbilityDataBuilder> all, int x, int y) {
        node.setGridX(x);
        node.setGridY(y);

        List<AbilityDataBuilder> children = all.stream()
                .filter(b -> b.getPrerequisites().contains(node.getId()))
                .toList();

        int childCount = children.size();
        for (int i = 0; i < childCount; i++) {
            // offset so no overlap
            int xOffset = i - (childCount / 2);
            assignCoords(children.get(i), all, x + xOffset, y + 1);
        }
    }
}
