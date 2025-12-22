package abyssus.pandorae.component;

import abyssus.pandorae.AbyssusPandorae;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<KingdomComponent> KINGDOM = ComponentRegistry.getOrCreate(Identifier.of(AbyssusPandorae.MOD_ID, "kingdom"), KingdomComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(KINGDOM, PlayerKingdomComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
