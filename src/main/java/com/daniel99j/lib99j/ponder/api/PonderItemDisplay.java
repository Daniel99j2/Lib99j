package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.ponder.impl.PonderItemDisplayInternal;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class PonderItemDisplay extends GenericEntityElement {
    public PonderScene scene;
    private Vector2i pos;
    private Vec2 size;
    public int life;
    private final List<ItemStack> items;
    private final int line;

    private final ArrayList<VirtualElement> elements = new ArrayList<>();

    public PonderItemDisplay(int life, Vector2i pos, Vec2 size, List<ItemStack> items, PonderScene scene, int line) {
        this.items = items;
        this.life = life;
        this.size = size;
        this.pos = pos;
        this.scene = scene;
        this.line = line;
        this.update();
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.MARKER;
    }

    @Override
    public void tick() {
        super.tick();
        if(this.scene.getMode().isPaused()) return;
        this.life--;
        if(this.life <= 0) {
            this.getHolder().removeElement(this);
        };
    }

    @Override
    public void setHolder(ElementHolder holder) {
        if(holder == null) {
            for (VirtualElement element : this.elements) {
                this.getHolder().removeElement(element);
            }
        }
        super.setHolder(holder);
        if(holder != null) update();
    }

    public void setPos(Vector2i pos) {
        this.pos = pos;
        this.update();
    }

    public Vector2i getPos() {
        return pos;
    }

    public void setSize(Vec2 size) {
        this.size = size;
        this.update();
    }

    private void update() {
        if(this.getHolder() == null) return;
        for (VirtualElement element : this.elements) {
            this.getHolder().removeElement(element);
        }
        this.elements.clear();

        int offsetSize = 50;
        int offset = offsetSize+15;
        for (ItemStack item : this.items) {
            this.elements.add(new PonderItemDisplayInternal(this.scene, 1000000, new Vector2i(this.pos).add(offset, 50), this.size, item));

            offset+=offsetSize;
        }

        PonderTextDisplay display = new PonderTextDisplay(1000000, new Vector2i(this.pos), this.size, Component.literal("       ".repeat(this.items.size())+"\n \n "), this.scene);
        display.scene = this.scene;
        this.elements.add(display);

        for (VirtualElement element : this.elements) {
            this.getHolder().addElement(element);
        }
    }
}
