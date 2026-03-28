package com.daniel99j.lib99j.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class GoToThenStopGoal extends Goal {
    private final PathfinderMob mob;
    private final BlockPos pos;

    public GoToThenStopGoal(PathfinderMob mob, BlockPos pos) {
        this.mob = mob;
        this.pos = pos;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.mob.isPathFinding()) {
            this.mob.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.0);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.blockPosition().equals(pos);
    }

    @Override
    public void stop() {
        super.stop();
        mob.removeFreeWill();
    }
}
