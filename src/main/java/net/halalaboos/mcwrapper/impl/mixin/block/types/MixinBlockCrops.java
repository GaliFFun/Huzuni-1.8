package net.halalaboos.mcwrapper.impl.mixin.block.types;

import net.halalaboos.mcwrapper.api.block.types.Crops;
import net.halalaboos.mcwrapper.api.util.math.Vector3i;
import net.halalaboos.mcwrapper.impl.Convert;
import net.halalaboos.mcwrapper.impl.mixin.block.MixinBlock;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockCrops.class)
public abstract class MixinBlockCrops extends MixinBlock implements Crops {
	@Shadow public abstract int getMetaFromState(IBlockState state);

	@Shadow @Final public static PropertyInteger AGE;

	@Override
	public int getAge(Vector3i pos) {
		IBlockState state = Convert.world().getBlockState(Convert.to(pos));
		return getMetaFromState(state);
	}

	@Override
	public boolean isGrown(Vector3i pos) {
		return getAge(pos) >= getMaxAge();
	}

	@Override
	public int getMaxAge() {
		return 7;
	}
}
