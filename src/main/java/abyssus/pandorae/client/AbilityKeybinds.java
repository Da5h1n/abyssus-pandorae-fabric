package abyssus.pandorae.client;

import abyssus.pandorae.component.Kingdom;
import abyssus.pandorae.component.ModComponents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public class AbilityKeybinds {

    public static final KeyBinding.Category ABYSSUSABILITIES = KeyBinding.Category.create(Identifier.of("key.category.abyssus.abilities"));

    private static final Map<String, KeyBinding> ABILITY_BINDS = new LinkedHashMap<>();

    public static void register() {
        // register slots into map
        bindSlot("slot_1", GLFW.GLFW_KEY_V);
        bindSlot("slot_2", GLFW.GLFW_KEY_B);
        bindSlot("slot_3", GLFW.GLFW_KEY_N);
        bindSlot("slot_4", GLFW.GLFW_KEY_M);
    }

    private static void bindSlot(String id, int defaultKey) {
        KeyBinding binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.abyssus." + id,
                InputUtil.Type.KEYSYM,
                defaultKey,
                ABYSSUSABILITIES
        ));
        ABILITY_BINDS.put(id, binding);
    }

    public static void handleInput(MinecraftClient client) {
        if (client.player == null) return;

        var component = ModComponents.KINGDOM.get(client.player);

        for (Map.Entry<String, KeyBinding> entry : ABILITY_BINDS.entrySet()) {
            String slotId = entry.getKey();
            KeyBinding binding = entry.getValue();

            // Check if the key was pressed
            while (binding.wasPressed()) {
                // only trigger if its purchased
                if (component.hasAbility(slotId)) {
                    triggerAbility(client, slotId);
                } else {
                    client.player.sendMessage(Text.literal("§cThis slot is not yet unlocked!"), true);
                }
            }
        }
    }

    private static void triggerAbility(MinecraftClient client,String slotId) {
        // SEND packet to server "Player used slot_1"
        // server checks kingdom and performs the correct action
        client.player.sendMessage(Text.literal("Triggered Ability in: " + slotId), true);
    }
}
