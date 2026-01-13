package abyssus.pandorae.gui.config;

import abyssus.pandorae.AbyssusPandorae;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class ModConfig {
    public int dotColour = 0xFFAA00;

    public int PathlineOff = 0xFF555555;
    public int PathlineOn = 0xFFFFAA00;

    public int Conflictlineoff = 0xFF770000;
    public int Conflictlineon = 0xFFFF0000;

    public int InactiveskillBorder = 0xFFAAAAAA;
    public int ActiveskillBorder = 0xFFFFAA00;

    public int headerBorder = 0xFFAAAAAA;
    public int headerbg = 0xFF555555;

    public int confirmdialogBg = 0xFF000000;
    public int confirmdialogborder = 0xFFFFAA00;

    public int skillTreeBg = 0xAA000000;


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(AbyssusPandorae.MOD_ID + ".json").toFile();

    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ModConfig(); // return defaults
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)){
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
