package com.daniel99j.lib99j.ponder.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;

@ApiStatus.Internal
public class PonderDevEdits {
    public boolean inBlocKEditMode = false;
    public ArrayList<BlockEdit> blockEdits = new ArrayList<>();

    public record BlockEdit(BlockPos pos, Block block, EditType type) {}

    public enum EditType {
        REMOVE,
        ADD,
        CHANGE
    }
}
