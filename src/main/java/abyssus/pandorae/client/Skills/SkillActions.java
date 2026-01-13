package abyssus.pandorae.client.Skills;

import abyssus.pandorae.AbyssusPandorae;
import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.util.AbilityLoader;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SkillActions {
    private static final Map<String, Consumer<PlayerEntity>> ACTIONS = new HashMap<>();
    private static final Map<String, Consumer<PlayerEntity>> TICKING_PASSIVES = new HashMap<>();

    private static final Map<String, AttributeModifierData> ATTRIBUTE_PASSIVES = new HashMap<>();

    public static void register() {

        // ACTIVE ABILITIES
        ACTIONS.put("dash", player -> {
            Vec3d rotation = player.getRotationVec(1.0f);
            // apply the burst
            player.addVelocity(rotation.x * 2, 0.5, rotation.z * 2);

            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.velocityDirty = true;
                serverPlayer.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(player));
            }
        });

        //  TICKING PASSIVES
        TICKING_PASSIVES.put("health_regen", player -> {
            if (player.age % 40 == 0 && player.getHealth() < player.getMaxHealth()) { // EVERY 2 seconds
                player.heal(1.0f);
            }
        });

        // ATTRIBUTE PASSIVES
        ATTRIBUTE_PASSIVES.put("health_boost", new AttributeModifierData(
                EntityAttributes.MAX_HEALTH,
                4.0, // ADDS 2 hearts to max health
                EntityAttributeModifier.Operation.ADD_VALUE
        ));

        ATTRIBUTE_PASSIVES.put("fleet_foot", new AttributeModifierData(
                EntityAttributes.MOVEMENT_SPEED,
                0.05,
                EntityAttributeModifier.Operation.ADD_VALUE
        ));
    }

    public static void refreshAttributes(ServerPlayerEntity player) {
        var component = ModComponents.KINGDOM.get(player);

        ATTRIBUTE_PASSIVES.forEach((actionId, data) -> {
            Identifier modifierId = Identifier.of(AbyssusPandorae.MOD_ID, actionId);
            EntityAttributeInstance instance = player.getAttributeInstance(data.attribute);

            if (instance != null) {
                instance.removeModifier(modifierId);

                boolean ownsSkill = component.getPurchasedAbilities().stream().map(AbilityLoader::get).anyMatch(ability -> ability != null && actionId.equals(ability.actionId()));

                if (ownsSkill) {
                    instance.addPersistentModifier(new EntityAttributeModifier(modifierId, data.value, data.operation));
                }
            }
        });
    }

    private record AttributeModifierData(
            RegistryEntry<EntityAttribute> attribute,
            double value,
            EntityAttributeModifier.Operation operation
    ) {}

    public static void applyAttribute(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attribute, String name, double value, EntityAttributeModifier.Operation operation) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance != null) {
            Identifier id = Identifier.of(AbyssusPandorae.MOD_ID, name);
            instance.removeModifier(id);
            instance.addPersistentModifier(new EntityAttributeModifier(id, value, operation));
        }
    }

    public static void tickPassive(String actionId, PlayerEntity player) {
        if (TICKING_PASSIVES.containsKey(actionId)) {
            TICKING_PASSIVES.get(actionId).accept(player);
        }
    }

    public static void execute(String actionId, PlayerEntity player) {
        if (ACTIONS.containsKey(actionId)) {
            ACTIONS.get(actionId).accept(player);
        }
    }
}
