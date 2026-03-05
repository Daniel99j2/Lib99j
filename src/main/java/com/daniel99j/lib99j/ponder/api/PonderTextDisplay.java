package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.impl.PonderGuiTextures;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class PonderTextDisplay extends TextDisplayElement {
    private final PonderScene scene;
    private Vec2 pos;
    private int life;

    public PonderTextDisplay(PonderScene scene, int life, Vec2 pos, Vec2 scale, Component... lines) {
        this.scene = scene;
        this.pos = Vec2.ZERO;
        this.life = life;
        this.setLines(lines);
        this.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.setBrightness(Brightness.FULL_BRIGHT);
        this.setTextAlignment(Display.TextDisplay.Align.LEFT);
        this.setViewRange(0.01f);
        this.setBackground(0xff000000);
        this.setPos(pos);
        this.setSize(scale);
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

    @Override
    public void setTranslation(Vector3fc vector3f) {
        super.setTranslation(vector3f);
    }

    public void setPos(Vec2 pos) {
        this.pos = pos;
        Vector3f scale = new Vector3f(0.01f, 0.01f, 0);
        Vec2 coords = PonderScene.transformWorldToScreenCoord(this.pos);
        this.setTranslation(new Vector3f(coords.x, coords.y, -0.2f).add(new Vector3f(0.5f, -0.5f, 0).mul(scale)));
    }

    public Vec2 getPos() {
        return pos;
    }

    public void setSize(Vec2 size) {
        Vector3f scale = new Vector3f(0.01f * size.x, 0.01f * size.y, 0);
        this.setScale(scale);
    }

    @Override
    public void setText(Component text) {
        throw new IllegalArgumentException("Use setLines() instead");
    }

    public void setLines(Component... lines) {
        int largestWidth = 0;

        for (Component line : lines) {
            if (FabricLoader.getInstance().isDevelopmentEnvironment() && line.getString().contains("\n")) throw new IllegalStateException("Lines of text should not occupy more than one line");
            largestWidth = Math.max(largestWidth, DefaultFonts.REGISTRY.getWidth(line, 8));
        }

        largestWidth+=2;

        MutableComponent out = Component.empty();

        int i = 0;
        for (Component text : lines) {
            boolean top = i == 0;
            boolean bottom = i == lines.length-1;

            int space = 0;
            MutableComponent text1 = Component.empty();
            text1.append(GuiUtils.getSpace(-3));
            text1.append(top ? PonderGuiTextures.TEXT_BACKGROUND_TOP_LEFT_CORNER.text() : bottom ? PonderGuiTextures.TEXT_BACKGROUND_BOTTOM_LEFT_CORNER.text() : PonderGuiTextures.TEXT_BACKGROUND_LEFT.text());

            int width = DefaultFonts.REGISTRY.getWidth(text, 8);

            if(bottom || top) {
                for (int j = 0; j <= largestWidth; j++) {
                    text1.append(top ? PonderGuiTextures.TEXT_BACKGROUND_TOP.text() : PonderGuiTextures.TEXT_BACKGROUND_BOTTOM.text());
                    space -= 1;
                }
            }

            if(!(bottom || top)) {
                text1.append(GuiUtils.getSpace(largestWidth+1));
                space -= largestWidth+1;
            }

            text1.append(top ? PonderGuiTextures.TEXT_BACKGROUND_TOP_RIGHT_CORNER.text() : bottom ? PonderGuiTextures.TEXT_BACKGROUND_BOTTOM_RIGHT_CORNER.text() : PonderGuiTextures.TEXT_BACKGROUND_RIGHT.text());

            text1.append(GuiUtils.getSpace(space));
            text1.append(text);

            //extra padding
            if(width == largestWidth-2) text1.append(GuiUtils.getSpace(1));
            if(!bottom) text1.append("\n");

            out.append(text1);

            top = false;
            i++;
        }
        super.setText(out);
    }
}
