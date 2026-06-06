package app.unv.gtomaid.twist.task;

import app.unv.gtomaid.config.GTOMaidConfig;
import app.unv.gtomaid.twist.GTTimeTwistLogic;
import app.unv.gtomaid.twist.TwistScheduleState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** 挑选最近的可加速 GT 机器，并寻路到附近的站位。 */
public class MaidTimeTwistMoveTask extends Behavior<EntityMaid> {

    private final TwistScheduleState scheduleState;
    private int scanCooldown;
    private int wanderCooldown;

    public MaidTimeTwistMoveTask(float movementSpeed, TwistScheduleState scheduleState) {
        super(Map.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
        this.scheduleState = scheduleState;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!GTOMaidConfig.ENABLE_TIME_TWIST.get()) {
            return false;
        }
        // 仅当 WALK_TARGET 与 TARGET_POS 都缺失时才会触发，同时清理过期的分配。
        long now = level.getGameTime();
        if (scheduleState.hasAssignment()
                && now - scheduleState.getAssignedAtTick() > GTOMaidConfig.ASSIGNMENT_TIMEOUT_TICKS.get()) {
            BlockPos stale = scheduleState.getCurrentController();
            if (stale != null) {
                scheduleState.markUnaccelerable(stale.asLong());
            }
            scheduleState.clearAssignment();
        } else if (scheduleState.hasAssignment()) {
            scheduleState.clearAssignment();
        }
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        int scanInterval = GTOMaidConfig.SCAN_INTERVAL.get();
        scanCooldown = scanInterval + maid.getRandom().nextInt(scanInterval);
        return GTTimeTwistLogic.hasTimeTwisterInHand(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Assignment found = pickNextMachine(level, maid);

        if (found == null && scheduleState.hasSkipped()) {
            scheduleState.resetRound();
            found = pickNextMachine(level, maid);
        }

        if (found != null) {
            scheduleState.assign(found.controller, found.stand, gameTime);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, found.stand,
                    GTOMaidConfig.MOVE_SPEED.get().floatValue(), 0);
            maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(found.stand));
            scanCooldown = 10;
        } else if (wanderCooldown <= 0) {
            wanderRandomly(maid);
            wanderCooldown = GTOMaidConfig.WANDER_RETRY_INTERVAL.get();
        }
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (wanderCooldown > 0)
            wanderCooldown--;
    }

    @Nullable
    private Assignment pickNextMachine(ServerLevel level, EntityMaid maid) {
        List<BlockPos> machines = scanAllMachines(level, maid);
        if (machines.isEmpty())
            return null;
        machines.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(maid.position())));
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid);
        try {
            for (BlockPos controller : machines) {
                BlockPos stand = findStand(level, maid, controller, pathFinding);
                if (stand != null)
                    return new Assignment(controller, stand);
                scheduleState.markUnaccelerable(controller.asLong());
            }
        } finally {
            pathFinding.finish();
        }
        return null;
    }

    private List<BlockPos> scanAllMachines(ServerLevel level, EntityMaid maid) {
        BlockPos center = maid.hasRestriction() ? maid.getRestrictCenter() : maid.blockPosition();
        int range = (int) maid.getRestrictRadius();
        int verticalSearchRange = GTOMaidConfig.VERTICAL_SEARCH_RANGE.get();
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        List<BlockPos> result = new ArrayList<>();

        for (int y = -verticalSearchRange; y <= verticalSearchRange; y++) {
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    mutable.setWithOffset(center, x, y, z);
                    if (!maid.isWithinRestriction(mutable))
                        continue;
                    if (scheduleState.shouldSkip(mutable.asLong()))
                        continue;
                    if (!GTTimeTwistLogic.isAcceleratableMachine(level, mutable))
                        continue;
                    if (!canPathReachNear(pathFinding, mutable))
                        continue;
                    if (!checkOwnerPos(maid, mutable))
                        continue;
                    result.add(mutable.immutable());
                }
            }
        }
        pathFinding.finish();
        return result;
    }

    private boolean canPathReachNear(MaidPathFindingBFS pathFinding, BlockPos pos) {
        // 与 findStand 用的盒子一致，先做粗筛。
        int standRadius = GTOMaidConfig.STAND_RADIUS.get();
        int standVerticalRadius = GTOMaidConfig.STAND_VERTICAL_RADIUS.get();
        for (int x = -standRadius; x <= standRadius; x++) {
            for (int y = -standVerticalRadius; y <= standVerticalRadius; y++) {
                for (int z = -standRadius; z <= standRadius; z++) {
                    if (pathFinding.canPathReach(pos.offset(x, y, z)))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean checkOwnerPos(EntityMaid maid, BlockPos pos) {
        if (maid.isHomeModeEnable())
            return true;
        return maid.getOwner() != null
                && pos.closerToCenterThan(maid.getOwner().position(), GTOMaidConfig.OWNER_DISTANCE.get());
    }

    // 在控制器附近找一个女仆能走到的站位，取距离控制器最近的可达点；找不到返回 null
    @Nullable
    private BlockPos findStand(ServerLevel level, EntityMaid maid, BlockPos controller,
            MaidPathFindingBFS pathFinding) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDistToController = Double.MAX_VALUE;
        int standRadius = GTOMaidConfig.STAND_RADIUS.get();
        int standVerticalRadius = GTOMaidConfig.STAND_VERTICAL_RADIUS.get();

        for (int dy = -standVerticalRadius; dy <= standVerticalRadius; dy++) {
            for (int dx = -standRadius; dx <= standRadius; dx++) {
                for (int dz = -standRadius; dz <= standRadius; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0)
                        continue;
                    cursor.setWithOffset(controller, dx, dy, dz);
                    if (!isStandable(level, cursor))
                        continue;
                    if (!pathFinding.canPathReach(cursor))
                        continue;
                    double dist = cursor.distToCenterSqr(Vec3.atCenterOf(controller));
                    if (dist < bestDistToController) {
                        bestDistToController = dist;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    /** 可站立：脚下有支撑、脚部和头部为空。 */
    private boolean isStandable(ServerLevel level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.getCollisionShape(level, pos.below()).isEmpty())
            return false;
        BlockState feet = level.getBlockState(pos);
        if (!feet.getCollisionShape(level, pos).isEmpty())
            return false;
        BlockState head = level.getBlockState(pos.above());
        return head.getCollisionShape(level, pos.above()).isEmpty();
    }

    private void wanderRandomly(EntityMaid maid) {
        Vec3 target = net.minecraft.world.entity.ai.util.LandRandomPos.getPos(maid,
                GTOMaidConfig.WANDER_RADIUS.get(), 3);
        if (target != null) {
            maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                    new WalkTarget(target, GTOMaidConfig.WANDER_SPEED.get().floatValue(), 0));
        }
    }

    /** (控制器, 站位) 二元组。 */
    private record Assignment(BlockPos controller, BlockPos stand) {
    }
}
