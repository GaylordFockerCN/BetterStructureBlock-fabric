package com.p1nero.bsb.mixin;

import com.p1nero.bsb.BetterStructureBlock;
import com.p1nero.bsb.BetterStructureConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * 不知道为什么Server的level为null，所以拆开
 */
@Mixin(StructureBlockBlockEntity.class)
public abstract class ClientStructureBlockBlockEntityMixin extends BlockEntity {
    public ClientStructureBlockBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique
    private boolean loaded = false;
    @Unique
    private boolean firstLoad = true;
    @Shadow public abstract void setOffset(BlockPos offset);

    @Shadow public abstract void setSize(Vec3i size);

    @Shadow public abstract String getTemplateName();

    @Shadow public abstract StructureBlockMode getMode();

    @Shadow public abstract BlockPos getOffset();

    @Shadow public abstract Vec3i getSize();

    @Shadow public abstract BlockMirror getMirror();

    @Shadow public abstract BlockRotation getRotation();

    @Shadow public abstract String getMetadata();

    @Shadow public abstract boolean shouldIgnoreEntities();

    @Shadow public abstract boolean shouldShowAir();

    @Shadow public abstract boolean shouldShowBoundingBox();

    @Shadow public abstract long getSeed();

    @Shadow public abstract float getIntegrity();

    /**
     * 核心就在这里，load的时候被限制了大小{@link StructureBlockBlockEntity#readNbt(NbtCompound)}
     * 不知为何在服务端调用会没反应
     */
    @Inject(at = @At("TAIL"), method = "readNbt")
    private void betterStructureBlock$readNbt(NbtCompound nbt, CallbackInfo ci) {
        int i = nbt.getInt("posX");
        int j = nbt.getInt("posY");
        int k = nbt.getInt("posZ");
        setOffset(new BlockPos(i,j,k));
        int l = nbt.getInt("sizeX");
        int m = nbt.getInt("sizeY");
        int n = nbt.getInt("sizeZ");
        setSize(new Vec3i(l,m,n));
        if(firstLoad){
            loaded = nbt.getBoolean("loaded");
            firstLoad = false;
        }

        if(world != null && !loaded && BetterStructureConfig.LOAD_IMMEDIATELY){
            Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).sendPacket(new UpdateStructureBlockC2SPacket(getPos(), StructureBlockBlockEntity.Action.LOAD_AREA, getMode(), getTemplateName(), getOffset(), getSize(), getMirror(), getRotation(), getMetadata(), shouldIgnoreEntities(), shouldShowAir(), shouldShowBoundingBox(), getIntegrity(), getSeed()));
            BetterStructureBlock.LOGGER.info("post load request : {} ", getTemplateName());
            loaded = true;
        }
    }


    @Inject(at = @At("TAIL"), method = "writeNbt")
    private void betterStructureBlock$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("loaded", loaded);
    }
}
