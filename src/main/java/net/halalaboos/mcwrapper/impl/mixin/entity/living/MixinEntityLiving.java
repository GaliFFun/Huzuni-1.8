package net.halalaboos.mcwrapper.impl.mixin.entity.living;

import net.halalaboos.mcwrapper.api.entity.living.Living;
import net.halalaboos.mcwrapper.api.entity.living.data.HealthData;
import net.halalaboos.mcwrapper.api.entity.living.player.Hand;
import net.halalaboos.mcwrapper.api.item.ItemStack;
import net.halalaboos.mcwrapper.api.potion.PotionEffect;
import net.halalaboos.mcwrapper.impl.Convert;
import net.halalaboos.mcwrapper.impl.mixin.entity.MixinEntity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Mixin(net.minecraft.entity.EntityLivingBase.class)
@Implements(@Interface(iface = Living.class, prefix = "api$"))
public abstract class MixinEntityLiving extends MixinEntity implements Living {

	@Shadow public abstract IAttributeInstance getEntityAttribute(IAttribute attribute);

	@Shadow public abstract boolean isPotionActive(Potion potionIn);

	@Shadow public abstract float getHealth();
	@Shadow public abstract float getMaxHealth();
	@Shadow public abstract boolean isOnLadder();
	@Shadow protected abstract void shadow$jump();
	@Shadow public int maxHurtResistantTime;
	@Shadow public float jumpMovementFactor;
	@Shadow @Final private Map<Potion, net.minecraft.potion.PotionEffect> activePotionsMap;
	@Shadow public abstract float getAbsorptionAmount();

	@Shadow
	public abstract int getTotalArmorValue();

	@Shadow
	public abstract void addPotionEffect(net.minecraft.potion.PotionEffect potioneffectIn);

	@Shadow
	public abstract void removePotionEffect(int id);

	@Override
	public HealthData getHealthData() {
		return new HealthData(getHealth(), getMaxHealth(), getAbsorptionAmount());
	}

	@Intrinsic
	public void api$jump() {
		shadow$jump();
	}

	@Override
	public boolean isClimbing() {
		return isOnLadder();
	}

	@Override
	public Optional<ItemStack> getHeldItem(Hand hand) {
		return Convert.getOptional(getHeldItem());
	}

	@Override
	public int getMaxHurtResistantTime() {
		return maxHurtResistantTime;
	}

	@Override
	public float getJumpMovementFactor() {
		return jumpMovementFactor;
	}

	@Override
	public void setJumpMovementFactor(float movementFactor) {
		this.jumpMovementFactor = movementFactor;
	}

	@Override //TODO
	public int getItemUseTicks() {
		return 1;
	}

	@Override
	public Collection<PotionEffect> getEffects() {
		return ((Collection<PotionEffect>)(Object) activePotionsMap.values());
	}

	@Override
	public int getTotalArmor() {
		return getTotalArmorValue();
	}

	@Override
	public void addEffect(PotionEffect effect) {
		addPotionEffect(Convert.to(effect));
	}

	@Override
	public void removeEffect(net.halalaboos.mcwrapper.api.potion.Potion potion) {
		removePotionEffect(potion.id());
	}

	@Shadow
	public boolean canBePushed() {return true;}

	@Shadow protected boolean dead;

	@Shadow public boolean isSwingInProgress;

	@Shadow public abstract net.minecraft.item.ItemStack getHeldItem();

	@Override
	public boolean isDead() {
		return dead;
	}

	@Override
	public Hand getCurrentHand() {
		return Hand.MAIN;
	}
}
