package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface GoalSelectorAccessor {
    @Mutable
    @Accessor("goalSelector")
    GoalSelector getGoalSelector();
}