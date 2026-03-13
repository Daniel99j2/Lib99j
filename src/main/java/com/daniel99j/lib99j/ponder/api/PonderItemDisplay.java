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

import java.util.ArrayList;
import java.util.List;

public class PonderItemDisplay extends GenericEntityElement {
    public PonderScene scene;
    private Vec2 pos;
    private Vec2 size;
    public int life;
    private List<ItemStack> items;

    private ArrayList<VirtualElement> elements = new ArrayList<>();

    public PonderItemDisplay(int life, Vec2 pos, Vec2 size, List<ItemStack> items) {
        this.items = items;
        this.pos = Vec2.ZERO;
        this.life = life;
        this.size = size;
        this.pos = pos;
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

    public void setPos(Vec2 pos) {
        this.pos = pos;
        this.update();
    }

    public Vec2 getPos() {
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

        float offsetSize = 0.03f;
        float offset = 0;
        for (ItemStack item : this.items) {
            this.elements.add(new PonderItemDisplayInternal(this.scene, 1000000, this.pos.add(new Vec2(offset, -0.035f)), this.size, item));

            offset+=offsetSize;
        }

        //TODO
//        PonderTextDisplay display = new PonderTextDisplay( 1000000, this.pos.add(new Vec2((offset-offsetSize)/2, 0)), this.size, List.of(Component.literal("       ".repeat(this.items.size())), Component.empty(), Component.empty()));
//        display.scene = this.scene;
//        this.elements.add(display);

        for (VirtualElement element : this.elements) {
            this.getHolder().addElement(element);
        }
    }
}
