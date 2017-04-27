package net.halalaboos.mcwrapper.impl.mixin.potion;

import net.halalaboos.mcwrapper.api.potion.Potion;
import net.halalaboos.mcwrapper.api.potion.PotionEffect;
import org.spongepowered.asm.mixin.*;

@Mixin(net.minecraft.potion.PotionEffect.class)
@Implements(@Interface(iface = PotionEffect.class, prefix = "api$"))
public abstract class MixinPotionEffect implements PotionEffect {

	@Shadow public abstract int shadow$getAmplifier();
	@Shadow public abstract int shadow$getDuration();
	@Shadow public abstract boolean getIsShowParticles();

	@Shadow
	public abstract void setPotionDurationMax(boolean maxDuration);

	@Shadow
	private boolean showParticles;

	@Shadow private int potionID;

	@Override
	public Potion getEffect() {
		return ((Potion) net.minecraft.potion.Potion.potionTypes[potionID]);
	}

	@Intrinsic
	public int api$getDuration() {
		return shadow$getDuration();
	}

	@Intrinsic
	public int api$getAmplifier() {
		return shadow$getAmplifier();
	}

	@Override
	public boolean showParticles() {
		return getIsShowParticles();
	}

	@Override
	public void setInfinite(boolean infinite) {
		setPotionDurationMax(infinite);
	}

	@Override
	public void setParticles(boolean particles) {
		showParticles = particles;
	}
}
