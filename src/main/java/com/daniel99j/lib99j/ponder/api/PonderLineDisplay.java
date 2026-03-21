package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.impl.PonderGuiTextures;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class PonderLineDisplay extends TextDisplayElement {
    public PonderScene scene;
    private Vector2i pos;
    private int life;

    public PonderLineDisplay(int life, Vector2i pos, Vec2 scale, PonderScene scene, int offset, int width) {
        offset = offset/2;
        width = width/2;
        this.pos = pos;
        this.life = life;
        this.scene = scene;
        this.setText(GuiUtils.getSpace(offset).append(Component.literal(PonderGuiTextures.WHITE_LINE.string().repeat(width)).append(GuiUtils.getSpace(800-width-offset))).withStyle(GuiUtils.GUI_FONT_STYLE));
        //this.setText(GuiUtils.getSpace(1).append("a"));
        this.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.setBrightness(Brightness.FULL_BRIGHT);
        this.setTextAlignment(Display.TextDisplay.Align.CENTER);
        this.setViewRange(0.01f);
        this.setBackground(0x00000000);
        this.setSize(scale);
        this.setPos(pos);
        this.setLineWidth(999999999);
        this.setInvisible(true);
        this.setSeeThrough(false);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.scene.getMode().isPaused()) return;
        this.life--;
        if(this.life <= 0) {
            this.scene.getElementHolder().removeElement(this);
        };
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
        Vec2 coords = PonderCoordUtil.pixelsToWorld(this.pos);

        Vec2 offset = new Vec2(800/40.0f/2*this.getScale().x(),  -10.0f/40*this.getScale().y());
        //offset = Vec2.ZERO;

        this.setTranslation(new Vector3f(coords.x, coords.y, -0.2f).add(new Vector3f(0.5f, -0.5f, 0).mul(scale)).add(offset.x, offset.y, 0));
    }

    public Vector2i getPos() {
        return pos;
    }

    public void setSize(Vec2 size) {
        Vector3f scale = new Vector3f(0.024f * size.x, 0.024f * size.y, 0);
        this.setScale(scale);
    }
}
