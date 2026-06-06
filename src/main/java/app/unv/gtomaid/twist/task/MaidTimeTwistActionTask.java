package app.unv.gtomaid.twist.task;

import app.unv.gtomaid.config.GTOMaidConfig;
import app.unv.gtomaid.twist.GTTimeTwistLogic;
import app.unv.gtomaid.twist.TwistScheduleState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MaidTimeTwistActionTask extends Behavior<EntityMaid> {

    private final TwistScheduleState scheduleState;
    private int swingCooldown;
    private int clicksAtCurrentTarget;
    private int ticksAtStand;

    public MaidTimeTwistActionTask(double closeEnoughDist, TwistScheduleState scheduleState) {
        super(ImmutableMap.of(InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT));
        this.scheduleState = scheduleState;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!GTOMaidConfig.ENABLE_TIME_TWIST.get())
            return false;
        if (!scheduleState.hasAssignment())
            return false;
        if (GTTimeTwistLogic.findTimeTwisterInHand(maid).isEmpty())
            return false;
        return hasArrivedAtStand(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!GTOMaidConfig.ENABLE_TIME_TWIST.get())
            return false;
        if (!scheduleState.hasAssignment())
            return false;
        if (GTTimeTwistLogic.findTimeTwisterInHand(maid).isEmpty())
            return false;
        return ticksAtStand < GTOMaidConfig.MAX_TICKS_AT_STAND.get();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        super.start(level, maid, gameTime);
        clicksAtCurrentTarget = 0;
        swingCooldown = 0;
        ticksAtStand = 0;
        maid.setSwingingArms(true);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        super.stop(level, maid, gameTime);
        maid.setSwingingArms(false);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        ticksAtStand++;

        BlockPos controller = scheduleState.getCurrentController();
        BlockPos stand = scheduleState.getCurrentStand();
        if (controller == null || stand == null) {
            clearAssignmentAndMemories(maid);
            return;
        }

        if (!GTTimeTwistLogic.isAcceleratableMachine(level, controller)) {
            scheduleState.markUnaccelerable(controller.asLong());
            clearAssignmentAndMemories(maid);
            return;
        }

        // 朝向 = 控制器指向站位的那一面。
        Direction face = faceFromStandToController(stand, controller);
        lookAtFace(maid, controller, face);

        if (swingCooldown > 0) {
            swingCooldown--;
            return;
        }

        if (clicksAtCurrentTarget >= GTOMaidConfig.MAX_CLICKS_PER_MACHINE.get()) {
            finishVisited(maid, controller);
            return;
        }

        ServerPlayer owner = maid.getOwner() instanceof ServerPlayer sp ? sp : null;
        if (owner == null) {
            finishVisited(maid, controller);
            return;
        }

        ItemStack twister = GTTimeTwistLogic.findTimeTwisterInHand(maid);
        if (twister.isEmpty()) {
            finishVisited(maid, controller);
            return;
        }

        Vec3 hitVec = Vec3.atCenterOf(controller).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        BlockHitResult hitResult = new BlockHitResult(hitVec, face, controller, false);
        UseOnContext context = new UseOnContext(level, owner, InteractionHand.MAIN_HAND, twister, hitResult);
        InteractionResult result = twister.onItemUseFirst(context);

        if (result.consumesAction()) {
            maid.swing(InteractionHand.MAIN_HAND);
            swingCooldown = GTOMaidConfig.SWING_COOLDOWN_TICKS.get();
            clicksAtCurrentTarget++;
            if (clicksAtCurrentTarget >= GTOMaidConfig.MAX_CLICKS_PER_MACHINE.get())
                finishVisited(maid, controller);
        } else {
            // 加速被拒绝（没 EU 或机器不再合格）：跳过并换下一个。
            scheduleState.markUnaccelerable(controller.asLong());
            clearAssignmentAndMemories(maid);
        }
    }

    private boolean hasArrivedAtStand(EntityMaid maid) {
        BlockPos stand = scheduleState.getCurrentStand();
        if (stand == null)
            return false;
        double dx = maid.getX() - (stand.getX() + 0.5);
        double dz = maid.getZ() - (stand.getZ() + 0.5);
        double dy = Math.abs(maid.getY() - stand.getY());
        double arriveH = GTOMaidConfig.ARRIVE_HORIZONTAL.get();
        return dx * dx + dz * dz <= arriveH * arriveH && dy <= GTOMaidConfig.ARRIVE_VERTICAL.get();
    }

    /** 返回控制器上指向站位的那一面：取水平偏移大的轴，正上/下方时默认 NORTH。 */
    private Direction faceFromStandToController(BlockPos stand, BlockPos controller) {
        int dx = stand.getX() - controller.getX();
        int dz = stand.getZ() - controller.getZ();
        if (Math.abs(dx) >= Math.abs(dz)) {
            if (dx > 0)
                return Direction.EAST;
            if (dx < 0)
                return Direction.WEST;
        } else {
            if (dz > 0)
                return Direction.SOUTH;
            if (dz < 0)
                return Direction.NORTH;
        }
        return Direction.NORTH;
    }

    private void finishVisited(EntityMaid maid, BlockPos controllerPos) {
        scheduleState.markVisited(controllerPos.asLong());
        clearAssignmentAndMemories(maid);
    }

    private void lookAtFace(EntityMaid maid, BlockPos target, Direction face) {
        Vec3 lookTarget = Vec3.atCenterOf(target).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        double dx = lookTarget.x - maid.getX();
        double dy = lookTarget.y - maid.getEyeY();
        double dz = lookTarget.z - maid.getZ();
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        if (horizDist > 0.01) {
            float yaw = (float) (Math.atan2(-dx, dz) * 180.0 / Math.PI);
            float pitch = (float) (-(Math.atan2(dy, horizDist) * 180.0 / Math.PI));
            maid.setYRot(yaw);
            maid.yRotO = yaw;
            maid.setXRot(pitch);
            maid.xRotO = pitch;
        }
    }

    private void clearAssignmentAndMemories(EntityMaid maid) {
        scheduleState.clearAssignment();
        maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }
}
