package app.unv.gtomaid.twist;

import app.unv.gtomaid.GTOMaid;
import app.unv.gtomaid.config.GTOMaidConfig;
import com.github.tartaricacid.touhoulittlemaid.api.task.FunctionCallSwitchResult;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public final class TaskTimeTwist implements IMaidTask {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(GTOMaid.MOD_ID, "time_twist");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return GTTimeTwistLogic.getTimeTwisterStack();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return null;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        TwistScheduleState scheduleState = new TwistScheduleState();
        return Lists.newArrayList(
                Pair.of(5, GTTimeTwistLogic.createMoveTask(scheduleState)),
                Pair.of(6, GTTimeTwistLogic.createActionTask(scheduleState)));
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Collections.singletonList(
                Pair.of("has_time_twister", m -> GTTimeTwistLogic.hasTimeTwisterInHand(m)));
    }

    @Override
    public FunctionCallSwitchResult onFunctionCallSwitch(EntityMaid maid) {
        if (!GTOMaidConfig.ENABLE_TIME_TWIST.get()) {
            return FunctionCallSwitchResult.MISSING_REQUIRED_ITEM;
        }
        if (GTTimeTwistLogic.hasTimeTwisterInHand(maid)) {
            return FunctionCallSwitchResult.NO_CHANGE;
        }
        return FunctionCallSwitchResult.MISSING_REQUIRED_ITEM;
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }

    @Override
    public String getMaidActionSummary() {
        return "Accelerate GT machines using the Time Twister item";
    }
}
