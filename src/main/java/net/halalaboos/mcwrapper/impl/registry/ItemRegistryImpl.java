package net.halalaboos.mcwrapper.impl.registry;

import net.halalaboos.mcwrapper.api.item.Item;
import net.halalaboos.mcwrapper.api.item.ItemTypes;
import net.halalaboos.mcwrapper.api.registry.ItemRegistry;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;

public class ItemRegistryImpl implements ItemRegistry {

	private Collection<Item> registered;

	@Override
	public Item getItem(String name) {
		return (Item) net.minecraft.item.Item.itemRegistry.getObject(new ResourceLocation(name));
	}

	@Override
	public Item getItem(int id) {
		try {
			return (Item) net.minecraft.item.Item.itemRegistry.getObjectById(id);
		} catch (Exception e) {
			return ItemTypes.GOLD_INGOT;
		}
	}

	@Override
	public Collection<Item> getRegisteredItems() {
		if (registered == null) {
			Collection<Item> out = new ArrayList<>();
			for (ResourceLocation resourceLocation : net.minecraft.item.Item.itemRegistry.getKeys()) {
				net.minecraft.item.Item item = net.minecraft.item.Item.itemRegistry.getObject(resourceLocation);
				if (!(item instanceof ItemBlock)) {
					out.add((Item)item);
				}
			}
			registered = out;
		}
		return registered;
	}
}
