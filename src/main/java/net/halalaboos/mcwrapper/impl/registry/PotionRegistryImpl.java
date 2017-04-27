package net.halalaboos.mcwrapper.impl.registry;

import net.halalaboos.mcwrapper.api.item.ItemStack;
import net.halalaboos.mcwrapper.api.potion.Potion;
import net.halalaboos.mcwrapper.api.potion.PotionEffect;
import net.halalaboos.mcwrapper.api.registry.PotionRegistry;
import net.halalaboos.mcwrapper.impl.Convert;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;

import java.util.List;

public class PotionRegistryImpl implements PotionRegistry {

	@SuppressWarnings("unchecked")
	@Override
	public List<PotionEffect> getEffects(ItemStack stack) {
		net.minecraft.item.ItemStack mcStack = Convert.to(stack);
		if (mcStack != null) {
			if (mcStack.getItem() == Items.potionitem) {
				List<net.minecraft.potion.PotionEffect> effects = ((ItemPotion) mcStack.getItem()).getEffects(mcStack);
				return ((List<PotionEffect>) (Object) effects);
			}
		}
		return null;
	}

	@Override
	public boolean isSplash(ItemStack stack) {
		if (stack == null) return false;
		net.minecraft.item.ItemStack stack1 = Convert.to(stack);
		if (stack1.getItem() == Items.potionitem) {
			if (ItemPotion.isSplash(stack1.getMetadata())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Potion getPotion(String name) {
		for (int i = 0; i < net.minecraft.potion.Potion.potionTypes.length; i++) {
			net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[i];
			if (potion.getName().equals(name)) return ((Potion) potion);
		}
		return null;
	}
}
