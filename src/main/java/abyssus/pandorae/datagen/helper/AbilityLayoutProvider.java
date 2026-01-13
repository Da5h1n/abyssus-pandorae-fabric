package abyssus.pandorae.datagen.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbilityLayoutProvider {
    private final Map<String, Integer> branchWidths = new HashMap<>();

    public void generateLayout(List<AbilityDataBuilder> builders) {
        Map<String, List<AbilityDataBuilder>> kingdomGroups = builders.stream().collect(Collectors.groupingBy(AbilityDataBuilder::getKingdom));

        kingdomGroups.forEach((kingdom, kingdomBuiders) -> {
            branchWidths.clear();

            List<AbilityDataBuilder> roots = kingdomBuiders.stream().filter(b -> b.getPrerequisites().isEmpty()).toList();

            int totalTreeWidth = 0;
            for (AbilityDataBuilder root : roots) {
                totalTreeWidth += calculateWidth(root, kingdomBuiders) + 2;
            }
            if (!roots.isEmpty()) totalTreeWidth -= 2;

            int currentX = -(totalTreeWidth / 2);

            for (AbilityDataBuilder root : roots) {
                int rootWidth = calculateWidth(root, kingdomBuiders);
                assignCoords(root, kingdomBuiders, currentX + (rootWidth / 2), 0);
                currentX += rootWidth + 2;
            }
        });
    }

    private int calculateWidth(AbilityDataBuilder node, List<AbilityDataBuilder> all) {
        List<AbilityDataBuilder> children = getChildren(node, all);
        if (children.isEmpty()) {
            return 1;
        }

        int totalWidth = 0;
        for (AbilityDataBuilder child : children) {
            totalWidth += calculateWidth(child, all);
        }
        totalWidth += (children.size() - 1);
        branchWidths.put(node.getId(), totalWidth);
        return totalWidth;
    }

    private void assignCoords(AbilityDataBuilder node, List<AbilityDataBuilder> all, int x, int y) {
        node.setGridX(x);
        node.setGridY(y);

        List<AbilityDataBuilder> children = getChildren(node, all);
        if (children.isEmpty()) return;

        int totalWidth = branchWidths.getOrDefault(node.getId(), children.size());
        int startX = x - (totalWidth / 2);

        int currentOffset = 0;
        for (AbilityDataBuilder child : children) {
            int childWidth = branchWidths.getOrDefault(child.getId(), 1);

            assignCoords(child, all, startX + currentOffset + (childWidth / 2), y + 1);
            currentOffset += childWidth + 1;
        }
    }

    private List<AbilityDataBuilder> getChildren(AbilityDataBuilder node, List<AbilityDataBuilder> all) {
        return all.stream()
                .filter(b -> b.getPrerequisites().contains(node.getId()))
                .collect(Collectors.toList());
    }
}
