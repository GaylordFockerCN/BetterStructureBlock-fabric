package com.p1nero.bsb.mixin;

import com.p1nero.bsb.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 不知道为什么Server的level为null，所以拆开
 */
@Mixin(StructureBlockBlockEntity.class)
public abstract class ServerStructureBlockBlockEntityMixin extends BlockEntity {
	@Shadow public abstract boolean loadStructure(ServerWorld world);

	@Unique
	private boolean loaded = false;
	@Shadow public abstract void setOffset(BlockPos offset);

	@Shadow public abstract void setSize(Vec3i size);

	public ServerStructureBlockBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * 核心就在这里，load的时候被限制了大小！！{@link StructureBlockBlockEntity#readNbt(NbtCompound)}
	 */
	@Inject(at = @At("TAIL"), method = "readNbt")
	private void injectedRead(NbtCompound nbt, CallbackInfo ci) {
		int i = nbt.getInt("posX");
		int j = nbt.getInt("posY");
		int k = nbt.getInt("posZ");
		setOffset(new BlockPos(i,j,k));
		int l = nbt.getInt("sizeX");
		int m = nbt.getInt("sizeY");
		int n = nbt.getInt("sizeZ");
		setSize(new Vec3i(l,m,n));
		loaded = nbt.getBoolean("loaded");

		if(!loaded && Config.LOAD_IMMEDIATELY){
			loaded = loadStructure(((ServerWorld) world));
		}
	}


	@Inject(at = @At("TAIL"), method = "writeNbt")
	private void injectedWrite(NbtCompound nbt, CallbackInfo ci) {
		nbt.putBoolean("loaded", loaded);
	}

}