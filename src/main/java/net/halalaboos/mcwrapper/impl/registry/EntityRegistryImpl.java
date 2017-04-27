package net.halalaboos.mcwrapper.impl.registry;

import net.halalaboos.mcwrapper.api.registry.EntityRegistry;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.projectile.*;

import java.util.HashMap;
import java.util.Map;

public class EntityRegistryImpl implements EntityRegistry {

	private Map<Class, Integer> entityMap = new HashMap<>();

	@Override
	public int getID(Class clazz) {
		try {
			return entityMap.get(clazz);
		} catch (Exception e) {
			return 666;
		}
	}

	private void addMapping(Class clazz, String name, int id) {
		entityMap.put(clazz, id);
	}

	private void foo() {
		addMapping(EntityItem.class, "Item", 1);
		addMapping(EntityXPOrb.class, "XPOrb", 2);
		addMapping(EntityEgg.class, "ThrownEgg", 7);
		addMapping(EntityLeashKnot.class, "LeashKnot", 8);
		addMapping(EntityPainting.class, "Painting", 9);
		addMapping(EntitySnowball.class, "Snowball", 11);
		addMapping(EntityLargeFireball.class, "Fireball", 12);
		addMapping(EntitySmallFireball.class, "SmallFireball", 13);
		addMapping(EntityEnderPearl.class, "ThrownEnderpearl", 14);
		addMapping(EntityEnderEye.class, "EyeOfEnderSignal", 15);
		addMapping(EntityPotion.class, "ThrownPotion", 16);
		addMapping(EntityExpBottle.class, "ThrownExpBottle", 17);
		addMapping(EntityItemFrame.class, "ItemFrame", 18);
		addMapping(EntityWitherSkull.class, "WitherSkull", 19);
		addMapping(EntityTNTPrimed.class, "PrimedTnt", 20);
		addMapping(EntityFallingBlock.class, "FallingSand", 21);
		addMapping(EntityFireworkRocket.class, "FireworksRocketEntity", 22);
		addMapping(EntityArmorStand.class, "ArmorStand", 30);
		addMapping(EntityBoat.class, "Boat", 41);
		addMapping(EntityMinecartEmpty.class, EntityMinecart.EnumMinecartType.RIDEABLE.getName(), 42);
		addMapping(EntityMinecartChest.class, EntityMinecart.EnumMinecartType.CHEST.getName(), 43);
		addMapping(EntityMinecartFurnace.class, EntityMinecart.EnumMinecartType.FURNACE.getName(), 44);
		addMapping(EntityMinecartTNT.class, EntityMinecart.EnumMinecartType.TNT.getName(), 45);
		addMapping(EntityMinecartHopper.class, EntityMinecart.EnumMinecartType.HOPPER.getName(), 46);
		addMapping(EntityMinecartCommandBlock.class, EntityMinecart.EnumMinecartType.COMMAND_BLOCK.getName(), 40);
		addMapping(EntityLiving.class, "Mob", 48);
		addMapping(EntityMob.class, "Monster", 49);
		addMapping(EntityCreeper.class, "Creeper", 50);
		addMapping(EntitySkeleton.class, "Skeleton", 51);
		addMapping(EntitySpider.class, "Spider", 52);
		addMapping(EntityGiantZombie.class, "Giant", 53);
		addMapping(EntityZombie.class, "Zombie", 54);
		addMapping(EntitySlime.class, "Slime", 55);
		addMapping(EntityGhast.class, "Ghast", 56);
		addMapping(EntityPigZombie.class, "PigZombie", 57);
		addMapping(EntityEnderman.class, "Enderman", 58);
		addMapping(EntityCaveSpider.class, "CaveSpider", 59);
		addMapping(EntitySilverfish.class, "Silverfish", 60);
		addMapping(EntityBlaze.class, "Blaze", 61);
		addMapping(EntityMagmaCube.class, "LavaSlime", 62);
		addMapping(EntityDragon.class, "EnderDragon", 63);
		addMapping(EntityWither.class, "WitherBoss", 64);
		addMapping(EntityBat.class, "Bat", 65);
		addMapping(EntityWitch.class, "Witch", 66);
		addMapping(EntityEndermite.class, "Endermite", 67);
		addMapping(EntityGuardian.class, "Guardian", 68);
		addMapping(EntityPig.class, "Pig", 90);
		addMapping(EntitySheep.class, "Sheep", 91);
		addMapping(EntityCow.class, "Cow", 92);
		addMapping(EntityChicken.class, "Chicken", 93);
		addMapping(EntitySquid.class, "Squid", 94);
		addMapping(EntityWolf.class, "Wolf", 95);
		addMapping(EntityMooshroom.class, "MushroomCow", 96);
		addMapping(EntitySnowman.class, "SnowMan", 97);
		addMapping(EntityOcelot.class, "Ozelot", 98);
		addMapping(EntityIronGolem.class, "VillagerGolem", 99);
		addMapping(EntityHorse.class, "EntityHorse", 100);
		addMapping(EntityRabbit.class, "Rabbit", 101);
		addMapping(EntityVillager.class, "Villager", 120);
		addMapping(EntityEnderCrystal.class, "EnderCrystal", 200);
	}
}
