package com.daniel99j.lib99j.ponder.api;

import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.impl.PonderGuiTextures;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.joml.Vector3f;
import xyz.nucleoid.server.translations.api.Localization;

import java.util.ArrayList;

public class PonderTextDisplay extends TextDisplayElement {
    public PonderScene scene;
    private Vector2i pos;
    private int life;

    public Vec2 textUncentering = Vec2.ZERO;

    public PonderTextDisplay(int life, Vector2i pos, Vec2 scale, Component component, PonderScene scene) {
        this.pos = pos;
        this.life = life;
        this.scene = scene;
        this.setText(component);
        this.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.setBrightness(Brightness.FULL_BRIGHT);
        this.setTextAlignment(Display.TextDisplay.Align.LEFT);
        this.setViewRange(0.01f);
        this.setBackground(0xff000000);
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
        Vec2 coords = PonderCoordUtil.pixelsToWorld(this.pos);

        Vec2 offset = new Vec2(textUncentering.x/40/2*this.getScale().x(), textUncentering.y/40*this.getScale().y());

        this.setTranslation(new Vector3f(coords.x, coords.y, -0.2f).add(new Vector3f(0.5f, -0.5f, 0).mul(scale)).add(offset.x, offset.y, 0));
    }

    public Vector2i getPos() {
        return pos;
    }

    public void setSize(Vec2 size) {
        Vector3f scale = new Vector3f(0.024f * size.x, 0.024f * size.y, 0);
        this.setScale(scale);
    }

    @Override
    public void setText(Component component) {
        ArrayList<Component> lines = new ArrayList<>();

        String baseText = component.getContents() instanceof TranslatableContents translatableContents ? Localization.raw(translatableContents.getKey(), this.scene.player) : component.getString();

        //baseText+=("\ntest"+"!".repeat(NumberUtils.getRandomInt(0, 80))).repeat(NumberUtils.getRandomInt(0, 10));

        while(true) {
            int index = baseText.indexOf("\n");;
            if(index == -1) break;
            lines.add(Component.literal(baseText.substring(0, index)));
            baseText = MiscUtils.replaceTextBetween(baseText, "", "\n", "");
        }

        lines.add(Component.literal(baseText));

        setTextRaw(lines);
    }

    public void setTextRaw(ArrayList<Component> lines) {
        int largestWidth = 0;

        for (Component line : lines) {
            if (Lib99j.isDevelopmentEnvironment && line.getString().contains("\n")) throw new IllegalStateException("Lines of text should not occupy more than one line");
            largestWidth = Math.max(largestWidth, DefaultFonts.REGISTRY.getWidth(line, 8));
        }

        largestWidth+=2;

        MutableComponent out = Component.empty();

        textUncentering = new Vec2(largestWidth-2, -1);
        int i = 0;
        for (Component text : lines) {
            boolean top = i == 0;
            boolean bottom = i == lines.size()-1;
            boolean topAndBottom = top && bottom;

            int space = 0;
            MutableComponent text1 = Component.empty();
            text1.append(GuiUtils.getSpace(-3));
            text1.append(topAndBottom ? PonderGuiTextures.TEXT_BACKGROUND_SINGLE_LEFT.text() : (top ? PonderGuiTextures.TEXT_BACKGROUND_TOP_LEFT_CORNER.text() : bottom ? PonderGuiTextures.TEXT_BACKGROUND_BOTTOM_LEFT_CORNER.text() : PonderGuiTextures.TEXT_BACKGROUND_LEFT.text()));

            int width = DefaultFonts.REGISTRY.getWidth(text, 8);

            if(bottom || top) {
                for (int j = 0; j <= largestWidth; j++) {
                    text1.append(topAndBottom ? PonderGuiTextures.TEXT_BACKGROUND_SINGLE.text() : (top ? PonderGuiTextures.TEXT_BACKGROUND_TOP.text() : PonderGuiTextures.TEXT_BACKGROUND_BOTTOM.text()));
                    space -= 1;
                }
            }

            if(!(bottom || top)) {
                text1.append(GuiUtils.getSpace(largestWidth+1));
                space -= largestWidth+1;
            }

            text1.append(topAndBottom ? PonderGuiTextures.TEXT_BACKGROUND_SINGLE_RIGHT.text() : (top ? PonderGuiTextures.TEXT_BACKGROUND_TOP_RIGHT_CORNER.text() : bottom ? PonderGuiTextures.TEXT_BACKGROUND_BOTTOM_RIGHT_CORNER.text() : PonderGuiTextures.TEXT_BACKGROUND_RIGHT.text()));

            text1.append(GuiUtils.getSpace(space));
            text1.append(text);

            //extra padding
            if(width == largestWidth-2) text1.append(GuiUtils.getSpace(1));
            if(!bottom) text1.append("\n");

            out.append(text1);

            i++;

            textUncentering = textUncentering.add(new Vec2(0, -10));
        }

        super.setText(out);
    }
}
