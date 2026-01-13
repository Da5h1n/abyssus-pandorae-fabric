package abyssus.pandorae.client;

import abyssus.pandorae.component.Kingdom;
import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.networking.AbilityActionPayload;
import abyssus.pandorae.util.AbilityLoader;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
        bindSlot("slot_5", GLFW.GLFW_KEY_L);
        bindSlot("slot_6", GLFW.GLFW_KEY_K);
        bindSlot("slot_7", GLFW.GLFW_KEY_T);
        bindSlot("slot_8", GLFW.GLFW_KEY_Y);
        bindSlot("slot_9", GLFW.GLFW_KEY_F);
        bindSlot("slot_10", GLFW.GLFW_KEY_Z);
        bindSlot("slot_11", GLFW.GLFW_KEY_C);
        bindSlot("slot_12", GLFW.GLFW_KEY_X);
        bindSlot("slot_13", GLFW.GLFW_KEY_SLASH);
        bindSlot("slot_14", GLFW.GLFW_KEY_BACKSLASH);
        bindSlot("slot_15", GLFW.GLFW_KEY_SEMICOLON);
        bindSlot("slot_16", GLFW.GLFW_KEY_SEMICOLON);
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
                    client.player.sendMessage(Text.translatable("key.trigger.ability").formatted(Formatting.RED), true);
                }
            }
        }
    }

    private static void triggerAbility(MinecraftClient client,String slotId) {
        var component = ModComponents.KINGDOM.get(client.player);

        var abilityData = AbilityLoader.get(slotId);

        if (abilityData != null && abilityData.actionId() != null) {
            ClientPlayNetworking.send(new AbilityActionPayload(abilityData.actionId()));

            client.player.sendMessage(Text.literal("Triggered: " + abilityData.name()).formatted(Formatting.GREEN), true);
        }
    }
}
