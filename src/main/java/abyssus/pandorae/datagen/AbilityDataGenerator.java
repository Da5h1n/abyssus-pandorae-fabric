package abyssus.pandorae.datagen;

import abyssus.pandorae.AbyssusPandorae;
import abyssus.pandorae.datagen.helper.AbilityDataBuilder;
import abyssus.pandorae.datagen.helper.AbilityLayoutProvider;
import abyssus.pandorae.gui.stats.AbilityData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AbilityDataGenerator implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final FabricDataOutput output;

    public AbilityDataGenerator(FabricDataOutput output) {
        this.output = output;
    }

    private void add(List<AbilityDataBuilder> builders, String id, String name, String desc, int cost,
                     List<String> pre, List<String> con, String kingdom,
                     AbilityData.AbilityType type, String actionId) {
        builders.add(new AbilityDataBuilder(id, name, desc, cost, pre, con, kingdom, type, actionId));
    }

    private void add(List<AbilityDataBuilder> builders, String id, String name, String desc, int cost,
                     List<String> pre, List<String> con, String kingdom) {
        builders.add(new AbilityDataBuilder(id, name, desc, cost, pre, con, kingdom,
                AbilityData.AbilityType.PASSIVE, "none"));
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        List<AbilityDataBuilder> builders = new ArrayList<>();

        //DEFINE ABILITIES
// Left Kingdom Abilities
        // add(builders, "SLOTID", "SLOTNAME", "DESCRIPTION", COST(INT), List.of("PREREQUISITES"), List.of("CONFLICTS"), "KINGDOM", *ABILITY TYPE*, *ABILITY ID*); * ARE OPTIONAL
        add(builders, "slot_1", "slot_1", "TBD", 10, List.of(), List.of(), "left");
        add(builders, "slot_2", "slot_2", "TBD", 25, List.of("slot_1"), List.of(), "left");
        add(builders, "slot_3", "slot_3", "TBD", 20, List.of("slot_1"), List.of(), "left");
        add(builders, "slot_4", "slot_4", "TBD", 25, List.of("slot_2"), List.of(), "left");
        add(builders, "slot_5", "Harpy", "TBD", 25, List.of("slot_2"), List.of(), "left");
        add(builders, "slot_6", "slot_6", "TBD", 25, List.of("slot_3"), List.of(), "left");
        add(builders, "slot_7", "Bat", "TBD", 25, List.of("slot_3"), List.of(), "left");
        add(builders, "slot_8", "Flying Squirrel", "TBD", 25, List.of("slot_4"), List.of(), "left");
        add(builders, "slot_9", "Total Aerokinesis", "TBD", 25, List.of("slot_4"), List.of(), "left");
        add(builders, "slot_10", "slot_10", "TBD", 25, List.of("slot_5"), List.of(), "left");
        add(builders, "slot_11", "Storm", "TBD", 25, List.of("slot_5"), List.of(), "left");
        add(builders, "slot_12", "Tornado", "TBD", 25, List.of("slot_6"), List.of(), "left");
        add(builders, "slot_13", "Fog", "TBD", 25, List.of("slot_6"), List.of(), "left");
        add(builders, "slot_14", "Wind Spirit", "TBD", 25, List.of("slot_7"), List.of(), "left");
        add(builders, "slot_15", "Banshee", "TBD", 25, List.of("slot_7"), List.of(), "left");
        add(builders, "slot_16", "slot_16", "TBD", 25, List.of("slot_8"), List.of(), "left");
        add(builders, "slot_17", "slot_17", "TBD", 20, List.of("slot_8"), List.of(), "left");
        add(builders, "slot_18", "slot_18", "TBD", 20, List.of("slot_9"), List.of(), "left");
        add(builders, "slot_19", "slot_19", "TBD", 20, List.of("slot_9"), List.of(), "left");
        add(builders, "slot_20", "slot_20", "TBD", 20, List.of("slot_10"), List.of(), "left");
        add(builders, "slot_21", "slot_21", "TBD", 20, List.of("slot_10"), List.of(), "left");
        add(builders, "slot_22", "slot_22", "TBD", 20, List.of("slot_11"), List.of(), "left");
        add(builders, "slot_23", "slot_23", "TBD", 20, List.of("slot_11"), List.of(), "left");
        add(builders, "slot_24", "slot_24", "TBD", 20, List.of("slot_12"), List.of(), "left");
        add(builders, "slot_25", "slot_25", "TBD", 20, List.of("slot_12"), List.of(), "left");
        add(builders, "slot_26", "slot_26", "TBD", 20, List.of("slot_13"), List.of(), "left");
        add(builders, "slot_27", "slot_27", "TBD", 20, List.of("slot_13"), List.of(), "left");
        add(builders, "slot_28", "slot_28", "TBD", 20, List.of("slot_14"), List.of(), "left");
        add(builders, "slot_29", "slot_29", "TBD", 20, List.of("slot_14"), List.of(), "left");
        add(builders, "slot_30", "slot_30", "TBD", 20, List.of("slot_15"), List.of("slot_31"), "left");
        add(builders, "slot_31", "slot_31", "TBD", 20, List.of("slot_15"), List.of("slot_30"), "left");
        // TEST PLACHOLDER BELOW:
        add(builders, "slot_1", "Vitality", "Increases Max Health", 20, List.of(), List.of(), "left", AbilityData.AbilityType.PASSIVE, "health_boost");
        add(builders, "slot_2", "Quick Dash", "Burst Forward", 20, List.of("slot_1"), List.of(), "left", AbilityData.AbilityType.ACTIVE, "dash");


        AbilityLayoutProvider layoutProvider = new AbilityLayoutProvider();
        layoutProvider.generateLayout(builders);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        DataOutput.PathResolver pathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "abilities");

        for (AbilityDataBuilder builder : builders) {
            AbilityData data = builder.build();
            String fileName = data.kingdom() + "_" + data.id();
            Path path = pathResolver.resolveJson(Identifier.of(AbyssusPandorae.MOD_ID, fileName));
            futures.add(DataProvider.writeToPath(writer, GSON.toJsonTree(data), path));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Ability Data Generation";
    }
}
