package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.api.PonderCoordUtil;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class PonderItemDisplayInternal extends ItemDisplayElement {
    private final PonderScene scene;
    private Vector2i pos;
    private int life;

    public PonderItemDisplayInternal(PonderScene scene, int life, Vector2i pos, Vec2 scale, ItemStack stack) {
        this.scene = scene;
        this.pos = pos;
        this.life = life;
        this.setItem(stack);
        this.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.setBrightness(Brightness.FULL_BRIGHT);
        this.setItemDisplayContext(ItemDisplayContext.GUI);
        this.setViewRange(0.01f);
        Quaternionf rot = new Quaternionf();
        rot.rotateY((float) Math.toRadians(180));
        this.setLeftRotation(rot);
        this.setPos(pos);
        this.setSize(scale);
        this.setInvisible(true);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.scene.getMode().isPaused()) return;
        this.life--;
        if(this.life <= 0) this.scene.getElementHolder().removeElement(this);
    }

    @Override
    public Vec3 getCurrentPos() {
        BlockDisplayElement element = ((Lib99jPlayerUtilController) scene.player).lib99j$getCamera();
        if(!scene.getMode().hasInGameGui() || element == null) return new Vec3(0, -10000, 0);
        return element.getCurrentPos();
    }

    public void setPos(Vector2i pos) {
        this.pos = pos;
        Vector3f scale = new Vector3f(0.01f, 0.01f, 0);
        Vec2 coords = PonderCoordUtil.pixelsToWorld(new Vector2i(this.pos).add(28, 28));
        this.setTranslation(new Vector3f(coords.x, coords.y, -0.199999999f).add(new Vector3f(0.5f, -0.5f, 0).mul(scale)));
    }

    public Vector2i getPos() {
        return pos;
    }

    public void setSize(Vec2 size) {
        Vector3f scale = new Vector3f(0.024f * size.x / 2, 0.024f * size.y / 2, 0.00000001f);
        this.setScale(scale);
    }
}
