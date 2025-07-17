package com.p1nero.bsb.mixin;

import com.p1nero.bsb.BetterStructureConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.BlockRotStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 不知道为什么Server的level为null，所以拆开
 */
@Mixin(StructureBlockBlockEntity.class)
public abstract class ServerStructureBlockBlockEntityMixin extends BlockEntity {
	@Unique
	private static boolean better_structure_block$IS_LOADING;//防止一次加载太多
	@Shadow public abstract boolean loadStructure(ServerWorld world);

	@Unique
	private boolean loaded = false;
	@Shadow public abstract void setOffset(BlockPos offset);

	@Shadow public abstract void setSize(Vec3i size);

	@Shadow private String author;

	@Shadow private Vec3i size;

	@Shadow private float integrity;

	@Shadow private BlockMirror mirror;

	@Shadow private BlockRotation rotation;

	@Shadow private boolean ignoreEntities;

    @Shadow
    public static Random createRandom(long seed) {
        return null;
    }

    @Shadow private long seed;

	@Shadow private BlockPos offset;

	public ServerStructureBlockBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * 核心就在这里，load的时候被限制了大小{@link StructureBlockBlockEntity#readNbt(NbtCompound)}
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
		loaded = nbt.getBoolean("loaded");

		if(!loaded && BetterStructureConfig.LOAD_IMMEDIATELY){
			loaded = loadStructure(((ServerWorld) world));
		}
	}

	@Inject(method = "place", at = @At("HEAD"), cancellable = true)
	private void better_structure_block$place(ServerWorld world, boolean interactive, StructureTemplate template, CallbackInfoReturnable<Boolean> cir) {
		if(BetterStructureConfig.LOAD_IMMEDIATELY){
			if(!better_structure_block$IS_LOADING){
				better_structure_block$IS_LOADING = true;
				if(betterStructureBlock$originalPlace(world, interactive, template)){
					if(BetterStructureConfig.DESTROY_AFTER_LOAD) {
						world.breakBlock(this.getPos(), false);
					}
					better_structure_block$IS_LOADING = false;
					cir.setReturnValue(true);
					return;
				}
				better_structure_block$IS_LOADING = false;
			}
			cir.setReturnValue(false);
		}
	}

	@Unique
	public boolean betterStructureBlock$originalPlace(ServerWorld world, boolean interactive, StructureTemplate template) {
		BlockPos blockPos = this.getPos();
		if (!StringHelper.isEmpty(template.getAuthor())) {
			this.author = template.getAuthor();
		}

		Vec3i vec3i = template.getSize();
		boolean bl = this.size.equals(vec3i);
		if (!bl) {
			this.size = vec3i;
			this.markDirty();
			BlockState blockState = world.getBlockState(blockPos);
			world.updateListeners(blockPos, blockState, blockState, 3);
		}

		if (interactive && !bl) {
			return false;
		} else {
			StructurePlacementData structurePlacementData = (new StructurePlacementData()).setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities);
			if (this.integrity < 1.0F) {
				structurePlacementData.clearProcessors().addProcessor(new BlockRotStructureProcessor(MathHelper.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
			}

			BlockPos blockPos2 = blockPos.add(this.offset);
			template.place(world, blockPos2, blockPos2, structurePlacementData, createRandom(this.seed), 2);
			return true;
		}
	}

	@Inject(at = @At("TAIL"), method = "writeNbt")
	private void betterStructureBlock$writeNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putBoolean("loaded", loaded);
	}

}