package app.unv.gtomaid.twist;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class GTTimeTwistLogic {

    private static final ResourceLocation TIME_TWISTER_ID = ResourceLocation.fromNamespaceAndPath("gtocore",
            "time_twister");

    private GTTimeTwistLogic() {
    }

    public static boolean isTimeTwister(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(TIME_TWISTER_ID);
    }

    public static boolean hasTimeTwisterInHand(EntityMaid maid) {
        return isTimeTwister(maid.getMainHandItem()) || isTimeTwister(maid.getOffhandItem());
    }

    public static ItemStack findTimeTwisterInHand(EntityMaid maid) {
        ItemStack mainHand = maid.getMainHandItem();
        if (isTimeTwister(mainHand))
            return mainHand;
        ItemStack offHand = maid.getOffhandItem();
        if (isTimeTwister(offHand))
            return offHand;
        return ItemStack.EMPTY;
    }

    public static ItemStack getTimeTwisterStack() {
        return ForgeRegistries.ITEMS.getValue(TIME_TWISTER_ID).getDefaultInstance();
    }

    public static boolean isAcceleratableMachine(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockState(pos).getBlock() instanceof MetaMachineBlock))
            return false;
        var be = level.getBlockEntity(pos);
        if (be == null)
            return false;
        RecipeLogic rl = GTCapabilityHelper.getRecipeLogic(be);
        if (rl == null || !rl.isWorking() || rl.getLastRecipe() == null)
            return false;
        return rl.getLastRecipe().getInputEUt() > 0;
    }

    public static BehaviorControl<? super EntityMaid> createMoveTask(TwistScheduleState scheduleState) {
        return new app.unv.gtomaid.twist.task.MaidTimeTwistMoveTask(0.6f, scheduleState);
    }

    public static BehaviorControl<? super EntityMaid> createActionTask(TwistScheduleState scheduleState) {
        return new app.unv.gtomaid.twist.task.MaidTimeTwistActionTask(5.0, scheduleState);
    }
}
