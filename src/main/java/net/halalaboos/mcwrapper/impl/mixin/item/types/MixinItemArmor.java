package net.halalaboos.mcwrapper.impl.mixin.item.types;

import net.halalaboos.mcwrapper.api.item.types.Armor;
import net.halalaboos.mcwrapper.impl.mixin.item.MixinItem;
import net.minecraft.item.ItemArmor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemArmor.class)
public abstract class MixinItemArmor extends MixinItem implements Armor {

	@Shadow @Final public int damageReduceAmount;

	@Shadow @Final public int armorType;

	@Override
	public int getDamageReduceAmount() {
		return damageReduceAmount;
	}

	@Override
	public int getType() {
		return armorType;
	}
}
