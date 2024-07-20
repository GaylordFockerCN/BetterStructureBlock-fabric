package com.p1nero.bsb.mixin;

import com.p1nero.bsb.BetterStructureBlock;
import com.p1nero.bsb.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetWorkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    /**
     * 绕开玩家权限认证，立即加载。并根据配置项选择是否输出成功构造的信息
     */
    @Inject(method = "onUpdateStructureBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    private void injected(UpdateStructureBlockC2SPacket packet, CallbackInfo ci) {
        if (this.player.isCreativeLevelTwoOp() || Config.LOAD_IMMEDIATELY) {
            BlockPos blockPos = packet.getPos();
            BlockState blockState = this.player.getWorld().getBlockState(blockPos);
            BlockEntity blockEntity = this.player.getWorld().getBlockEntity(blockPos);
            if (blockEntity instanceof StructureBlockBlockEntity structureBlockBlockEntity) {
                structureBlockBlockEntity.setMode(packet.getMode());
                structureBlockBlockEntity.setTemplateName(packet.getTemplateName());
                structureBlockBlockEntity.setOffset(packet.getOffset());
                structureBlockBlockEntity.setSize(packet.getSize());
                structureBlockBlockEntity.setMirror(packet.getMirror());
                structureBlockBlockEntity.setRotation(packet.getRotation());
                structureBlockBlockEntity.setMetadata(packet.getMetadata());
                structureBlockBlockEntity.setIgnoreEntities(packet.shouldIgnoreEntities());
                structureBlockBlockEntity.setShowAir(packet.shouldShowAir());
                structureBlockBlockEntity.setShowBoundingBox(packet.shouldShowBoundingBox());
                structureBlockBlockEntity.setIntegrity(packet.getIntegrity());
                structureBlockBlockEntity.setSeed(packet.getSeed());
                if (structureBlockBlockEntity.hasStructureName()) {
                    String string = structureBlockBlockEntity.getTemplateName();
                    if (packet.getAction() == net.minecraft.block.entity.StructureBlockBlockEntity.Action.SAVE_AREA) {
                        if (structureBlockBlockEntity.saveStructure()) {
                            this.player.sendMessage(Text.translatable("structure_block.save_success", new Object[]{string}), false);
                        } else {
                            this.player.sendMessage(Text.translatable("structure_block.save_failure", new Object[]{string}), false);
                        }
                    } else if (packet.getAction() == net.minecraft.block.entity.StructureBlockBlockEntity.Action.LOAD_AREA) {
                        if (!structureBlockBlockEntity.isStructureAvailable()) {
                            this.player.sendMessage(Text.translatable("structure_block.load_not_found", new Object[]{string}), false);
                        } else if (structureBlockBlockEntity.loadStructure(this.player.getServerWorld())) {
                            if (!Config.DISABLE_LOAD_MESSAGE) {
                                this.player.sendMessage(Text.translatable("structure_block.load_success", new Object[]{string}), false);
                            }
                        } else {
                            if (Config.LOAD_IMMEDIATELY) {
                                structureBlockBlockEntity.loadStructure(this.player.getServerWorld());
                                BetterStructureBlock.LOGGER.info("try to load structure block AGAIN on server: {}", ((StructureBlockBlockEntity) blockEntity).getTemplateName());
                            } else {
                                this.player.sendMessage(Text.translatable("structure_block.load_prepare", new Object[]{string}), false);
                            }
                        }
                    } else if (packet.getAction() == net.minecraft.block.entity.StructureBlockBlockEntity.Action.SCAN_AREA) {
                        if (structureBlockBlockEntity.detectStructureSize()) {
                            this.player.sendMessage(Text.translatable("structure_block.size_success", new Object[]{string}), false);
                        } else {
                            this.player.sendMessage(Text.translatable("structure_block.size_failure"), false);
                        }
                    }
                } else {
                    this.player.sendMessage(Text.translatable("structure_block.invalid_structure_name", new Object[]{packet.getTemplateName()}), false);
                }

                structureBlockBlockEntity.markDirty();
                this.player.getWorld().updateListeners(blockPos, blockState, blockState, 3);
            }

        }
        ci.cancel();
    }
}
