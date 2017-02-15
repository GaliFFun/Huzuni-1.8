package net.halalaboos.huzuni.mc.mixin;

import net.halalaboos.huzuni.mod.visual.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.block.Block.class) public abstract class MixinBlock {

	@Shadow
	public abstract BlockState getBlockState();

	@Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
	public void shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side,
									 CallbackInfoReturnable<Boolean> ci) {
		if (Xray.INSTANCE.isEnabled()) {
			ci.setReturnValue(Xray.INSTANCE.isEnabled((getBlockState().getBlock())));
		}
	}

	@Inject(method = "getAmbientOcclusionLightValue", at = @At("HEAD"), cancellable = true)
	public void getAmbientOcclusionLightValue(CallbackInfoReturnable<Float> ci) {
		if (Xray.INSTANCE.isEnabled()) {
			ci.setReturnValue(10000F);
		}
	}
}
