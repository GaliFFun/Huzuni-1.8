package net.halalaboos.mcwrapper.impl.mixin.item;

import net.halalaboos.mcwrapper.api.item.Item;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.item.Item.class)
public abstract class MixinItem implements Item {

	@Shadow
	@Final
	public static RegistryNamespaced<ResourceLocation, net.minecraft.item.Item> itemRegistry;

	@Shadow
	public abstract String getUnlocalizedName();

	@Override
	public int getId() {
		return itemRegistry.getIDForObject((net.minecraft.item.Item)(Object)this);
	}

	@Override
	public String name() {
		return getUnlocalizedName();
	}
}
