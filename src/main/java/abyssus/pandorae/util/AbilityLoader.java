package abyssus.pandorae.util;

import abyssus.pandorae.component.Kingdom;
import abyssus.pandorae.gui.stats.AbilityData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AbilityLoader {
    private static final Gson GSON = new Gson();

    public static List<AbilityData> loadForKingdom(Kingdom kingdom) {
        if (kingdom == Kingdom.NONE) return new ArrayList<>();

        String path = "/assets/abyssus-pandorae/abilities/" + kingdom.asString().toLowerCase() + ".json";

        try (InputStream is = AbilityLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Could not find ability file: " + path);
                return new ArrayList<>();
            }

            InputStreamReader reader = new InputStreamReader(is);
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            // map abilities array to our list of records
            List<AbilityData> abilities = GSON.fromJson(json.get("abilities"), new TypeToken<List<AbilityData>>(){}.getType());

            // return empty list
            return  abilities != null ? abilities : new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
