package abyssus.pandorae;


import abyssus.pandorae.client.AbilityKeybinds;
import abyssus.pandorae.gui.kingdoms.ChooseKingdomScreen;
import abyssus.pandorae.gui.stats.KingdomStatsScreen;
import abyssus.pandorae.networking.OpenKingdomScreenPayload;
import abyssus.pandorae.util.AbilityLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AbyssusPandoraeClient implements ClientModInitializer {

    public static KeyBinding OPEN_KINGDOM_SCREEN;
    public static KeyBinding OPEN_STATS_SCREEN;

    public static final KeyBinding.Category ABYSSUSPANDORAE = KeyBinding.Category.create(Identifier.of("key.category_abyssus_pandorae"));

    @Override
    public void onInitializeClient() {

        ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(
                Identifier.of(AbyssusPandorae.MOD_ID, "abilities_loader"),
                new AbilityLoader()
        );

        AbilityKeybinds.register();
        ClientTickEvents.END_CLIENT_TICK.register(AbilityKeybinds::handleInput);

        ClientPlayNetworking.registerGlobalReceiver(OpenKingdomScreenPayload.ID, ((payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new ChooseKingdomScreen());
            });
        }));

        OPEN_KINGDOM_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                        "key.abyssus_pandorae.open_kingdom",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_KP_0,
                        ABYSSUSPANDORAE
                )
        );

        OPEN_STATS_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                        "key.abyssus_pandorae.open_stats",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_G,
                        ABYSSUSPANDORAE
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (OPEN_KINGDOM_SCREEN.wasPressed()) {
                minecraftClient.setScreen(new ChooseKingdomScreen());
            } else if (OPEN_STATS_SCREEN.wasPressed()) {
                minecraftClient.setScreen(new KingdomStatsScreen());
            }
        });

    }
}
