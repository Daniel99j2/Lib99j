package com.daniel99j.lib99j.api;

import de.tomalbrc.bil.api.AnimatedHolder;
import de.tomalbrc.bil.api.Animator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"unused"})
public class AnimationUtils {
    //most of this is from Tom's Mobs https://github.com/tomalbrc/toms-mobs/blob/1.21.5/src/main/java/de/tomalbrc/toms_mobs/util/AnimationHelper.java
    //it has been copied for porting
    public static void updateWalkAnimation(LivingEntity entity, AnimatedHolder holder) {
        updateWalkAnimation(entity, holder, 0);
    }

    public static void updateWalkAnimation(LivingEntity entity, AnimatedHolder holder, int priority) {
        Animator animator = holder.getAnimator();
        if (entity.walkAnimation.isMoving() && !entity.getDeltaMovement().closerThan(Vec3.ZERO, 0.02)) {
            animator.playAnimation("walk", priority);
            animator.pauseAnimation("idle");
        } else {
            animator.pauseAnimation("walk");
            animator.playAnimation("idle", priority);
        }
    }

    public static void updateHurtColor(LivingEntity entity, AnimatedHolder holder) {
        if (entity.hurtTime > 0 || entity.deathTime > 0)
            holder.setColor(0xff7e7e);
        else
            holder.clearColor();
    }
}
