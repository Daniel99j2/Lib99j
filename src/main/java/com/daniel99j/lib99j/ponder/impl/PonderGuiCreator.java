package com.daniel99j.lib99j.ponder.impl;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.RunCodeClickEvent;
import com.daniel99j.lib99j.api.RunCodeClickEventHolder;
import com.daniel99j.lib99j.api.gui.GuiUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.api.PonderBuilder;
import com.daniel99j.lib99j.ponder.api.PonderCoordUtil;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PonderGuiCreator extends PonderScene {
    public static final PonderBuilder PONDER_GUI_CREATOR_BUILDER = PonderManager.registerBuilder(
            PonderBuilder.create(Identifier.fromNamespaceAndPath("lib99j", "ponder_gui_creator"), Items.BARRIER.getDefaultInstance(), Component.literal("Ponder GUI Creator"), Component.literal("Ponder GUI Creator")).size(5, 5, 5).hideFromCommands()
                    .waitFor(1000000)
                    .finishStep()
                    .build()
    );

    private final ItemDisplayElement screenSizeDisplay;

    private ItemDisplayElement positioningElement;

    private boolean positioningX = true;

    private Vector2i testPos = new Vector2i(0, 0);

    private final RunCodeClickEventHolder eventStorage = new RunCodeClickEventHolder();

    protected PonderGuiCreator(ServerPlayer player, PonderBuilder builder, PonderScene oldAfterStep, int goTo) {
        super(player, builder, oldAfterStep, goTo);
        this.screenSizeDisplay = new ItemDisplayElement(Items.LIME_STAINED_GLASS) {
            @Override
            public Vec3 getCurrentPos() {
                BlockDisplayElement element = ((Lib99jPlayerUtilController) player).lib99j$getCamera();
                return element != null ? element.getCurrentPos() : Vec3.ZERO;
            }
        };
        //The 'standard' monitor is 16:9
        //Size done at 70 fov
        float normalSize = 0.31f/10;
        this.screenSizeDisplay.setScale(new Vector3f(normalSize*16, normalSize*9, 0));
        this.screenSizeDisplay.setTranslation(new Vector3f(0, 0, -0.2001f));
        this.screenSizeDisplay.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.screenSizeDisplay.setViewRange(0.01f);

        this.getElementHolder().addElement(this.screenSizeDisplay);


        this.positioningElement = new ItemDisplayElement(Items.WHITE_CONCRETE.getDefaultInstance()) {
            @Override
            public Vec3 getCurrentPos() {
                BlockDisplayElement element = ((Lib99jPlayerUtilController) player).lib99j$getCamera();
                return element != null ? element.getCurrentPos() : Vec3.ZERO;
            }
        };
        Vector3f scale = new Vector3f(0.01f, 0.01f, 0);
        this.positioningElement.setScale(scale);
        this.positioningElement.setBillboardMode(Display.BillboardConstraints.CENTER);
        Vec2 coords = PonderCoordUtil.pixelsToRelative(new Vector2i(0, 0));
        this.positioningElement.setTranslation(new Vector3f(coords.x, coords.y, -0.2f).add(new Vector3f(0.5f, -0.5f, 0).mul(scale)));
        this.positioningElement.setViewRange(0.01f);
        this.getElementHolder().addElement(this.positioningElement);
    }


    @Override
    public void openMenu() {
        this.setMode(PonderSceneMode.IN_MENU);

        MutableComponent title = builder.title.copy();
        if(title.getStyle().getHoverEvent() == null) GuiUtils.styleText(title, title.getStyle().withHoverEvent(new HoverEvent.ShowText(builder.description)), false);

        List<DialogBody> body = new ArrayList<>();

        body.add(action("Hide safe area", () -> {
            this.screenSizeDisplay.setItem(ItemStack.EMPTY);
        }));

        body.add(action("Show safe area", () -> {
            this.screenSizeDisplay.setItem(Items.LIME_STAINED_GLASS.getDefaultInstance());
        }));

        body.add(action("Show positioning element", () -> {
            this.positioningElement.setItem(Items.WHITE_CONCRETE.getDefaultInstance());
        }));

        body.add(action("Hide positioning element", () -> {
            this.positioningElement.setItem(ItemStack.EMPTY);
        }));

        body.add(action("Set position mode to X (scroll to change X position)", () -> {
            positioningX = true;
        }));

        body.add(action("Set position mode to Y (scroll to change Y position)", () -> {
            positioningX = false;
        }));

        body.add(action("Get position element pos", () -> {
            player.sendSystemMessage(Component.literal("Current pos: "+this.testPos.x+", "+this.testPos.y));
        }));

        body.add(action("Set position element pos to center", () -> {
            setTestPos(PonderCoordUtil.relativeToPixels(new Vec2(0.5f, 0.5f)));
        }));


        player.openDialog(Holder.direct(new NoticeDialog(
                new CommonDialogData(Component.literal("Ponder GUI Editor"), Optional.empty(), true, false, DialogAction.WAIT_FOR_RESPONSE, body, List.of()), new ActionButton(new CommonButtonData(Component.translatable("gui.done"), 150), Optional.of(new StaticAction(new ClickEvent.Custom(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "close_ponder_menu"), Optional.empty()))))
        )));
    }

    private void setTestPos(Vector2i vec2) {
        testPos = vec2;
        Vector3f scale = new Vector3f(0.01f, 0.01f, 0);
        Vec2 coords = PonderCoordUtil.pixelsToWorld(testPos);
        this.positioningElement.setTranslation(new Vector3f(coords.x, coords.y, -0.2f).add(new Vector3f(0.5f, -0.5f, 0).mul(scale)));
    }

    private ItemBody action(String text, Runnable code) {

        MutableComponent title = Component.literal(text);
        //eventStorage
        //Because this is dev-only, I dont care about security of the user running things whilst not in the UI
        GuiUtils.styleText(title, title.getStyle().withClickEvent(new RunCodeClickEvent((payload) -> {
            code.run();
            this.closeMenu();
        }, () -> true, true, this.player, eventStorage).clickEvent()), false);

        return new ItemBody(Items.CRAFTING_TABLE.getDefaultInstance(), Optional.of(new PlainMessage(title, 200)), false, false, 16, 16);
    }

    @Override
    public void addToSelectedStep(int selectedStep) {
        if(this.positioningElement != null) {
            var mul = new Vector2i(10, 10);

            setTestPos(testPos.add(new Vector2i(positioningX ? selectedStep/mul.x : 0, positioningX ? 0 : selectedStep/mul.y)));

        }
    }

    //if its frozen then the elements dont update


    @Override
    protected void updateTickStatus() {
        super.updateTickStatus();
    }

    @Override
    public void closeMenu() {
        super.closeMenu();
        setMode(PonderSceneMode.PAUSED);
        this.eventStorage.close();
    }
}
