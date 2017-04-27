package net.halalaboos.mcwrapper.impl;

import net.halalaboos.mcwrapper.api.MinecraftAdapter;
import net.halalaboos.mcwrapper.api.MinecraftClient;
import net.halalaboos.mcwrapper.api.item.ItemStack;
import net.halalaboos.mcwrapper.api.opengl.GLState;
import net.halalaboos.mcwrapper.api.potion.PotionEffect;
import net.halalaboos.mcwrapper.api.registry.*;
import net.halalaboos.mcwrapper.api.util.Builder;
import net.halalaboos.mcwrapper.impl.builder.ItemStackBuilder;
import net.halalaboos.mcwrapper.impl.builder.PotionEffectBuilder;
import net.halalaboos.mcwrapper.impl.registry.*;
import net.minecraft.client.Minecraft;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MinecraftAdapterImpl implements MinecraftAdapter {

	private MinecraftClient mc;

	private ItemRegistry itemRegistry = new ItemRegistryImpl();
	private BlockRegistry blockRegistry = new BlockRegistryImpl();
	private PotionRegistry potionRegistry = new PotionRegistryImpl();
	private EnchantmentRegistry enchantmentRegistry = new EnchantmentRegistryImpl();
	private EntityRegistry entityRegistry = new EntityRegistryImpl();

	private final Map<Class<?>, Supplier<?>> builderMap = new IdentityHashMap<>();
	private GLStateImpl glState = new GLStateImpl();

	public MinecraftAdapterImpl(Minecraft mc) {
		this.mc = ((MinecraftClient) mc);
		registerBuilder(ItemStack.Builder.class, ItemStackBuilder::new);
		registerBuilder(PotionEffect.Builder.class, PotionEffectBuilder::new);
	}

	@Override
	public MinecraftClient getMinecraft() {
		return ((MinecraftClient) Minecraft.getMinecraft());
	}

	@Override
	public ItemRegistry getItemRegistry() {
		return this.itemRegistry;
	}

	@Override
	public BlockRegistry getBlockRegistry() {
		return this.blockRegistry;
	}

	@Override
	public PotionRegistry getPotionRegistry() {
		return this.potionRegistry;
	}

	@Override
	public EnchantmentRegistry getEnchantmentRegistry() {
		return this.enchantmentRegistry;
	}

	@Override
	public EntityRegistry getEntityRegistry() {
		return entityRegistry;
	}

	@Override
	public GLState getGLStateManager() {
		return glState;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Builder> T getBuilder(Class<? extends Builder> builder) {
		Supplier<?> supplier = builderMap.get(builder);
		return (T)supplier.get();
	}

	@Override
	public <T> void registerBuilder(Class<T> builder, Supplier<? extends T> instance) {
		this.builderMap.put(builder, instance);
	}

	@Override
	public String getMinecraftVersion() {
		return "1.8.8";
	}
}
