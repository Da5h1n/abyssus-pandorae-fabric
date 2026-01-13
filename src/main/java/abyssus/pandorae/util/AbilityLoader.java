package abyssus.pandorae.util;

import abyssus.pandorae.AbyssusPandorae;
import abyssus.pandorae.component.Kingdom;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import abyssus.pandorae.gui.stats.AbilityData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AbilityLoader extends JsonDataLoader<AbilityData> implements ResourceReloader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<Identifier, AbilityData> ABILITIES = new HashMap<>();

    public AbilityLoader() {
        super(AbilityData.CODEC, ResourceFinder.json("abilities"));
    }


    @Override
    protected void apply(Map<Identifier, AbilityData> prepared, ResourceManager manager, Profiler profiler) {
        ABILITIES.clear();
        ABILITIES.putAll(prepared);

        AbyssusPandorae.LOGGER.info("Sucsessfully loaded {} abilities via Codec.", ABILITIES.size());
    }

    public static List<AbilityData> loadForKingdom(Kingdom kingdom) {
        if (kingdom == Kingdom.NONE) return List.of();

        String targetKingdom = kingdom.asString().toLowerCase();
        return ABILITIES.values().stream()
                .filter(a -> a.kingdom().equalsIgnoreCase(targetKingdom))
                .toList();
    }

    public static AbilityData get(String id) {
        return ABILITIES.values().stream().filter(data -> data.id().equals(id)).findFirst().orElse(null);
    }
}
