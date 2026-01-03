package abyssus.pandorae.item.custom;

import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.component.SoulState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class SoulManipulatorItem extends Item {
    public SoulManipulatorItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            var component = ModComponents.KINGDOM.get(player);

            component.setSoulState(SoulState.PROTECTED);
            component.setFaith(1);

            player.sendMessage(Text.translatable("text.abyssus-pandorae.SoulManipulatorItem.use"), true);

            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            return ActionResult.CONSUME.withNewHandStack(itemStack);
        }

        return ActionResult.SUCCESS;
    }


}
