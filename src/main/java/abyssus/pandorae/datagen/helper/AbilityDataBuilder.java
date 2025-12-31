package abyssus.pandorae.datagen.helper;

import abyssus.pandorae.gui.stats.AbilityData;

import java.util.List;

public class AbilityDataBuilder {
    private String id;
    private String name;
    private String description;
    private int cost;
    private List<String> prerequisites;
    private List<String> conflicts;
    private String kingdom;
    private int gridX;
    private int gridY;

    public AbilityDataBuilder(String id, String name, String description, int cost, List<String> prerequisites, List<String> conflicts, String kingdom) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.prerequisites = prerequisites;
        this.conflicts = conflicts;
        this.kingdom = kingdom;
    }

    public String getId() {return id;}
    public List<String> getPrerequisites() { return prerequisites; }
    public void setGridX(int x) { this.gridX = x; }
    public void setGridY(int y) { this.gridY = y; }

    public AbilityData build(){
        return new AbilityData(id, name, description, cost, prerequisites, conflicts, kingdom, gridX, gridY);
    }
}
