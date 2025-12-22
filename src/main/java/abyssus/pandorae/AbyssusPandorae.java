package abyssus.pandorae;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbyssusPandorae implements ModInitializer {
	public static final String MOD_ID = "abyssus-pandorae";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

    }

    public static Identifier identifier(String path) {
        return Identifier.of(AbyssusPandorae.MOD_ID, path);
    }
}