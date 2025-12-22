package abyssus.pandorae;

import abyssus.pandorae.gui.kingdoms.ChooseKingdomScreen;
import abyssus.pandorae.networking.OpenKingdomScreenPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AbyssusPandoraeClient implements ClientModInitializer {

    public static KeyBinding OPEN_FAITH_SCREEN;

    public static final KeyBinding.Category ABYSSUSPANDORAE = KeyBinding.Category.create(Identifier.of("key.category_abyssus_pandorae"));

    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(OpenKingdomScreenPayload.ID, ((payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new ChooseKingdomScreen());
            });
        }));

        OPEN_FAITH_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                        "key.abyssus_pandorae.open_faith",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_G,
                        ABYSSUSPANDORAE
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (OPEN_FAITH_SCREEN.wasPressed()) {
                minecraftClient.setScreen(new ChooseKingdomScreen());
            }
        });

    }
}
