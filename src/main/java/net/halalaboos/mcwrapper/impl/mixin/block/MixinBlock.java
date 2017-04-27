package net.halalaboos.mcwrapper.impl.mixin.block;

import net.halalaboos.huzuni.mod.visual.Xray;
import net.halalaboos.mcwrapper.api.block.Block;
import net.halalaboos.mcwrapper.api.entity.living.player.Player;
import net.halalaboos.mcwrapper.api.util.math.Vector3i;
import net.halalaboos.mcwrapper.api.world.World;
import net.halalaboos.mcwrapper.impl.Convert;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.RegistryNamespacedDefaultedByKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.block.Block.class)
public abstract class MixinBlock implements Block {

	@Shadow public float slipperiness;
	@Shadow private IBlockState defaultBlockState;

	@Shadow
	public abstract String getLocalizedName();

	@Shadow
	@Final
	public static RegistryNamespacedDefaultedByKey<ResourceLocation, net.minecraft.block.Block> blockRegistry;

	@Shadow public abstract float getBlockHardness(net.minecraft.world.World worldIn, BlockPos pos);

	@Override
	public float getSlipperiness() {
		return slipperiness;
	}

	@Override
	public void setSlipperiness(float slipperiness) {
		this.slipperiness = slipperiness;
	}

	@Override
	public float blockStrength(Player player, World world, Vector3i pos) {
		return getBlockHardness(Convert.world(), Convert.to(pos));
	}

	@Override
	public String name() {
		return getLocalizedName();
	}

	@Override
	public int getId() {
		return blockRegistry.getIDForObject((net.minecraft.block.Block) (Object) this);
	}

	@Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
	public void shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> ci) {
		if (Xray.INSTANCE.isEnabled()) {
			ci.setReturnValue(Xray.INSTANCE.isEnabled(this));
		}
	}

	@Inject(method = "getAmbientOcclusionLightValue", at = @At("HEAD"), cancellable = true)
	public void getAmbientOcclusionLightValue(CallbackInfoReturnable<Float> ci) {
		if (Xray.INSTANCE.isEnabled()) {
			ci.setReturnValue(10000F);
		}
	}
}
