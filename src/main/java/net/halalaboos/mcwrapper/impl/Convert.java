package net.halalaboos.mcwrapper.impl;

import net.halalaboos.mcwrapper.api.inventory.Inventory;
import net.halalaboos.mcwrapper.api.item.ItemStack;
import net.halalaboos.mcwrapper.api.item.enchant.Enchantment;
import net.halalaboos.mcwrapper.api.util.ResourcePath;
import net.halalaboos.mcwrapper.api.util.enums.DigAction;
import net.halalaboos.mcwrapper.api.util.enums.Face;
import net.halalaboos.mcwrapper.api.util.math.AABB;
import net.halalaboos.mcwrapper.api.util.math.Result;
import net.halalaboos.mcwrapper.api.util.math.Vector3d;
import net.halalaboos.mcwrapper.api.util.math.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;

import java.util.Optional;

/**
 * Utility for quickly converting MCWrapper data classes to the Minecraft ones, or vice-versa.
 * <p>This is only used for the Mixins.</p>
 */
public class Convert {

	/**
	 * Converts the Minecraft Bounding Box class to the MCWrapper one ({@link AABB}).
	 */
	public static AABB from(AxisAlignedBB bb) {
		return new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
	}

	public static AxisAlignedBB to(AABB bb) {
		return new AxisAlignedBB(bb.min.getX(), bb.min.getY(), bb.min.getZ(), bb.max.getX(), bb.max.getY(), bb.max.getZ());
	}

	public static Vector3i from(BlockPos pos) {
		return new Vector3i(pos.getX(), pos.getY(), pos.getZ());
	}

	public static BlockPos to(Vector3i pos) {
		return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
	}

	public static PotionEffect to(net.halalaboos.mcwrapper.api.potion.PotionEffect effect) {
		return (PotionEffect) effect;
	}

	public static Potion to(net.halalaboos.mcwrapper.api.potion.Potion potion) {
		return Potion.potionTypes[potion.id()];
	}

	public static C07PacketPlayerDigging.Action to(DigAction action) {
		return C07PacketPlayerDigging.Action.values()[action.ordinal()];
	}

	public static DigAction from(C07PacketPlayerDigging.Action action) {
		return DigAction.values()[action.ordinal()];
	}

	public static Face from(EnumFacing facing) {
		return Face.values()[facing.ordinal()];
	}

	public static EnumFacing to(Face face) {
		return EnumFacing.values()[face.ordinal()];
	}

	public static Vec3 to(Vector3d vec) {
		return new Vec3(vec.getX(), vec.getY(), vec.getZ());
	}

	public static Result from(MovingObjectPosition result) {
		Result out = Result.values()[result.typeOfHit.ordinal()];
		if (result.sideHit != null) {
			Face face = Convert.from(result.sideHit);
			out.setFace(face);
		}
		return out;
	}

	public static ItemStack from(net.minecraft.item.ItemStack stack) {
		return (ItemStack)(Object)stack;
	}

	//Only used for some Mixins, don't use this for anything outside of the impl package!!
	public static EntityPlayerSP player() {
		return mc().thePlayer;
	}

	//Only used for some Mixins, don't use this for anything outside of the impl package!!
	public static WorldClient world() {
		return mc().theWorld;
	}

	public static MovingObjectPosition mouseOver() {
		return mc().objectMouseOver;
	}

	private static Minecraft mc() {
		return Minecraft.getMinecraft();
	}

	public static net.minecraft.item.ItemStack to(ItemStack stack) {
		return (net.minecraft.item.ItemStack)(Object)stack;
	}

	public static Enchantment from(net.minecraft.enchantment.Enchantment enchantment) {
		return (Enchantment) enchantment;
	}

	public static net.minecraft.enchantment.Enchantment to(Enchantment enchantment) {
		return (net.minecraft.enchantment.Enchantment)enchantment;
	}

	public static Inventory from(IInventory inventory) {
		return (Inventory)inventory;
	}

	/**
	 * Various methods throughout our API use Optionals in the instance where {@link ItemStack ItemStacks} are returned.
	 * The reason for this is because in recent versions, ItemStacks are never actually null in most instances.
	 *
	 * So for our Mixins, we could either write this code below over and over again for each {@code getItem()} method,
	 * and then change the {@code stack.isEmpty()} bit to {@code stack == null} for pre 1.11 versions, or instead
	 * have all of the methods point to this method, which means we'd only have to update one method.
	 */
	public static Optional<ItemStack> getOptional(net.minecraft.item.ItemStack stack) {
		if (stack == null) {
			return Optional.empty();
		}
		return Optional.of(Convert.from(stack));
	}

	public static net.halalaboos.mcwrapper.api.block.Block from(Block block) {
		return (net.halalaboos.mcwrapper.api.block.Block)block;
	}

	public static ResourceLocation to(ResourcePath path) {
		return new ResourceLocation(path.getPath());
	}
}
