package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.api.PonderManager;
import com.daniel99j.lib99j.ponder.api.PonderScene;
import com.daniel99j.lib99j.ponder.impl.PonderDevEdits;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disablePlayerMove(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListener) this, this.server.packetProcessor());
        if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) {
            VFXUtils.handleMovePacket(packet, this.player);
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItemOn", at = @At("HEAD"), cancellable = true, order = 10000)
    private void addToBlockEdits(ServerboundUseItemOnPacket serverboundUseItemOnPacket, CallbackInfo ci) {
        if (this.player.getItemInHand(serverboundUseItemOnPacket.getHand()).getItem() instanceof BlockItem blockItem) {
            blockEdit(serverboundUseItemOnPacket.getHitResult().getBlockPos().relative(serverboundUseItemOnPacket.getHitResult().getDirection()), blockItem.getBlock(), ci);
        }
    }

    @Inject(method = "handleUseItem", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disableInteract(ServerboundUseItemPacket serverboundUseItemPacket, CallbackInfo ci) {
        if (PonderManager.isPondering(this.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true, order = 10000)
    private void removeFromBlockEdits(ServerboundPlayerActionPacket serverboundPlayerActionPacket, CallbackInfo ci) {
        if (serverboundPlayerActionPacket.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            blockEdit(serverboundPlayerActionPacket.getPos(), Blocks.AIR, ci);
        } else if (PonderManager.isPondering(this.player)) {
            ci.cancel();
        }

        if ((serverboundPlayerActionPacket.getAction() == ServerboundPlayerActionPacket.Action.DROP_ITEM || serverboundPlayerActionPacket.getAction() == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) && PonderManager.isPondering(this.player)) {
            PonderManager.activeScenes.get(this.player).stopPonderingSafely();
            ci.cancel();
        }
    }

    @Unique
    private void blockEdit(BlockPos pos, Block newBlock, CallbackInfo ci) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && PonderManager.isPondering(this.player)) {
            PonderScene currentScene = PonderManager.activeScenes.get(this.player);
            if (currentScene.ponderDevEdits.blockEdits.isEmpty())
                this.player.sendSystemMessage(Component.literal("Use '/ponder dev get_edits' to see all block edits"));

            Block actualBlock = currentScene.getLevel().getBlockState(pos).getBlock();
            currentScene.ponderDevEdits.blockEdits.removeIf((blockEdit -> blockEdit.pos().equals(pos)));

            if (actualBlock.defaultBlockState().isAir()) {
                //dont add +Air
                if (!newBlock.defaultBlockState().isAir())
                    currentScene.ponderDevEdits.blockEdits.add(new PonderDevEdits.BlockEdit(pos, newBlock, PonderDevEdits.EditType.ADD));
            } else if (newBlock.defaultBlockState().isAir()) {
                if (!newBlock.equals(actualBlock
                ))
                    currentScene.ponderDevEdits.blockEdits.add(new PonderDevEdits.BlockEdit(pos, actualBlock, PonderDevEdits.EditType.REMOVE));
            } else {
                currentScene.ponderDevEdits.blockEdits.add(new PonderDevEdits.BlockEdit(pos, newBlock, PonderDevEdits.EditType.CHANGE));
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disablePlayerInteract(ServerboundInteractPacket serverboundInteractPacket, CallbackInfo ci) {
        if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleAcceptTeleportPacket", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disableTeleport(ServerboundAcceptTeleportationPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListener) this, this.server.packetProcessor());
        if (PonderManager.isPondering(this.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "updateSignText", at = @At("HEAD"), cancellable = true)
    private void onSignUpdate(ServerboundSignUpdatePacket packet, List<FilteredText> signText, CallbackInfo ci) {
        if (packet.getPos().getY() == 0) {
            if ((Objects.equals(packet.getLines()[3], "lib99j$checker") || Objects.equals(packet.getLines()[3], "lib99j$final"))) {
                for (int i = 0; i < 3; i++) {
                    if (((Lib99jPlayerUtilController) player).lib99j$isModCheckerRunning())
                        ((Lib99jPlayerUtilController) player).lib99j$getActiveTranslationChecker().results().add(packet.getLines()[i]);
                }
                ci.cancel();

                //For the sneaky players doing random stuff...
                //I can remove it if it actually causes problems
                //But it's a good deterrent for silly players
                if (player.level().getBlockEntity(packet.getPos()) instanceof SignBlockEntity && !((Lib99jPlayerUtilController) player).lib99j$isModCheckerRunning()) {
                    disconnect(Component.translatable("multiplayer.disconnect.spoof_mod_checker"));
                    Lib99j.LOGGER.warn(player.getName().getString() + " tried to spoof the mod checker!");
                }
            }
            if (Objects.equals(packet.getLines()[3], "lib99j$final")) {
                ((Lib99jPlayerUtilController) player).lib99j$finishCurrentModChecker();
            }
        }
    }
}