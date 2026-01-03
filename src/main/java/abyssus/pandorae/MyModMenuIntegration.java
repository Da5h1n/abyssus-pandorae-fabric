package abyssus.pandorae;

import abyssus.pandorae.gui.config.Configscreen;
import abyssus.pandorae.gui.stats.KingdomStatsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class MyModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new Configscreen(parent);
    }
}
