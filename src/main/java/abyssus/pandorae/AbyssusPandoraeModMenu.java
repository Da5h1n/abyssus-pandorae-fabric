package abyssus.pandorae;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class AbyssusPandoraeModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("title.abyssus-pandorae.config"))
                    .setSavingRunnable(() -> AbyssusPandorae.config.save());

            ConfigCategory general = builder.getOrCreateCategory(Text.literal("Visuals"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startColorField(Text.literal("Dot Colour"), AbyssusPandorae.config.dotColour)
                    .setDefaultValue(0xFFFFAA00)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.dotColour = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Path Inactive Colour"), AbyssusPandorae.config.PathlineOff)
                    .setDefaultValue(0xFF555555)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.PathlineOff = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Path Active Colour"), AbyssusPandorae.config.PathlineOn)
                    .setDefaultValue(0xFFFFAA00)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.PathlineOn = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Conflict Path Inactive Colour"), AbyssusPandorae.config.Conflictlineoff)
                    .setDefaultValue(0xFF770000)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.Conflictlineoff = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Conflict Path Active Colour"), AbyssusPandorae.config.Conflictlineon)
                    .setDefaultValue(0xFFFF0000)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.Conflictlineon = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Inactive Skill Border Colour"), AbyssusPandorae.config.InactiveskillBorder)
                    .setDefaultValue(0xFFAAAAAA)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.InactiveskillBorder = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Active Skill Border Colour"), AbyssusPandorae.config.ActiveskillBorder)
                    .setDefaultValue(0xFFFFAA00)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.ActiveskillBorder = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Header Border Colour"), AbyssusPandorae.config.headerBorder)
                    .setDefaultValue(0xFFAAAAAA)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.headerBorder = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Header Background Colour"), AbyssusPandorae.config.headerbg)
                    .setDefaultValue(0xFF555555)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.headerbg = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Confirm Dialog Border Colour"), AbyssusPandorae.config.confirmdialogBg)
                    .setDefaultValue(0xFF000000)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.confirmdialogBg = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Confirm Dialog Background Colour"), AbyssusPandorae.config.confirmdialogborder)
                    .setDefaultValue(0xFFFFAA00)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.confirmdialogborder = newValue)
                    .build());

            general.addEntry(entryBuilder.startColorField(Text.literal("Background Darken Colour"), AbyssusPandorae.config.skillTreeBg)
                    .setDefaultValue(0xAA000000)
                    .setAlphaMode(true)
                    .setSaveConsumer(newValue -> AbyssusPandorae.config.skillTreeBg = newValue)
                    .build());

            return builder.build();
        };
    }
}
