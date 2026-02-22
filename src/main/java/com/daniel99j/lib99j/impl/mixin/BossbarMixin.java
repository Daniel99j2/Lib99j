package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.GameProperties;
import com.daniel99j.lib99j.impl.BossBarVisibility;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BossEvent.class)
public abstract class BossbarMixin implements BossBarVisibility {
    @Unique
    private boolean lib99j$visible = true;

    @Override
    public void lib99j$setVisible(boolean visible) {
        if(!visible && !GameProperties.isHideableBossBar()) {
            throw new IllegalStateException("Bossbar hiding is not enabled! Enable it through GameProperties.hideableBossBar!");
        }
        this.lib99j$visible = visible;
    }

    @Override
    public boolean lib99j$isVisible() {
        return this.lib99j$visible;
    }
}