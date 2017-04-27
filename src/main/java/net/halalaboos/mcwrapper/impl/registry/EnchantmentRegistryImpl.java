package net.halalaboos.mcwrapper.impl.registry;

import net.halalaboos.mcwrapper.api.item.enchant.Enchantment;
import net.halalaboos.mcwrapper.api.registry.EnchantmentRegistry;
import net.halalaboos.mcwrapper.impl.Convert;

import java.util.ArrayList;
import java.util.Collection;

public class EnchantmentRegistryImpl implements EnchantmentRegistry {

	private Collection<Enchantment> registered;

	@Override
	public Collection<Enchantment> getEnchants() {
		if (registered == null) {
			Collection<Enchantment> out = new ArrayList<>();
			for (net.minecraft.enchantment.Enchantment enchantment : net.minecraft.enchantment.Enchantment.enchantmentsBookList) {
				out.add((Enchantment)enchantment);
			}
			registered = out;
		}
		return registered;
	}

	@Override
	public Enchantment getEnchant(int id) {
		return Convert.from(net.minecraft.enchantment.Enchantment.getEnchantmentById(id));
	}

	@Override
	public Enchantment getEnchant(String name) {
		return Convert.from(net.minecraft.enchantment.Enchantment.getEnchantmentByLocation(name));
	}

	@Override
	public int getId(Enchantment enchantment) {
		return (Convert.to(enchantment)).effectId;
	}
}
