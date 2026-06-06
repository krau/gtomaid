package app.unv.gtomaid.twist;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/** 时间扭曲任务的共享状态：当前分配的机器 + 本轮跳过集合（轮内去重，非永久黑名单）。 */
public final class TwistScheduleState {

    private final Set<Long> skipThisRound = new HashSet<>();

    @Nullable
    private BlockPos currentController;
    @Nullable
    private BlockPos currentStand;
    private long assignedAtTick = Long.MIN_VALUE;

    public void markUnaccelerable(long posLong) {
        skipThisRound.add(posLong);
    }

    public void markVisited(long posLong) {
        skipThisRound.add(posLong);
    }

    public boolean shouldSkip(long posLong) {
        return skipThisRound.contains(posLong);
    }

    /** 本轮是否跳过过任何机器。 */
    public boolean hasSkipped() {
        return !skipThisRound.isEmpty();
    }

    /** 开启新一轮：清空跳过集合。 */
    public void resetRound() {
        skipThisRound.clear();
    }

    public void assign(BlockPos controller, BlockPos stand, long gameTime) {
        this.currentController = controller.immutable();
        this.currentStand = stand.immutable();
        this.assignedAtTick = gameTime;
    }

    public void clearAssignment() {
        this.currentController = null;
        this.currentStand = null;
        this.assignedAtTick = Long.MIN_VALUE;
    }

    @Nullable
    public BlockPos getCurrentController() {
        return currentController;
    }

    @Nullable
    public BlockPos getCurrentStand() {
        return currentStand;
    }

    public boolean hasAssignment() {
        return currentController != null && currentStand != null;
    }

    public long getAssignedAtTick() {
        return assignedAtTick;
    }
}
