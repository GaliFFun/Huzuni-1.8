package net.halalaboos.huzuni.api.util;

import com.google.common.collect.Multimap;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.halalaboos.huzuni.Huzuni;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;

import java.net.Proxy;
import java.util.Collection;

/**
 * Easy to use functions that calculate blah blah blah relating to Minecraft.
 * */
public final class MinecraftUtils {

	private static final Minecraft mc = Minecraft.getMinecraft();

	private static final Huzuni huzuni = Huzuni.INSTANCE;

	private MinecraftUtils() {

	}

	/**
	 * @return The address the player is currently connected to.
	 * */
	public static String getCurrentServer() {
		return mc.getCurrentServerData() == null ? "localhost" : mc.getCurrentServerData().serverIP;
	}

	/**
	 * Attempts to log into a Minecraft account using the username and password provided.
	 * @return a {@link Session} class with the account's new session.
	 * */
	public static Session loginToMinecraft(String username, String password) throws AuthenticationException {
		YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService( Proxy.NO_PROXY, "");
		YggdrasilUserAuthentication userAuthentication = (YggdrasilUserAuthentication) authenticationService .createUserAuthentication(Agent.MINECRAFT);
		userAuthentication.setUsername(username);
		userAuthentication.setPassword(password);
		userAuthentication.logIn();
		return new Session(userAuthentication.getSelectedProfile().getName(), userAuthentication.getSelectedProfile().getId().toString(), userAuthentication.getAuthenticatedToken(), "MOJANG" /* we mojang now ;)))*/);
	}

	/**
	 * @return True if the entity type is 
	 * */
	public static boolean checkType(Entity entity, boolean invisible, boolean mob, boolean animal, boolean player) {
		if (!entity.isEntityAlive())
			return false;
		if (entity.isInvisible() && !invisible)
			return false;
		if (mob && entity instanceof IMob)
			return true;
		if (animal && entity instanceof IAnimals && !(entity instanceof IMob))
			return true;
		if (player && entity instanceof EntityPlayer)
			return true;
		return false;
	}

	/**
	 * @return True if the entity age is greater than or equal to 20
	 * */
	public static boolean checkAge(EntityLivingBase entity) {
		return checkAge(entity, 20);
	}

	/**
	 * @return True if the entity age is greater than or equal to the age specified
	 * */
	public static boolean checkAge(EntityLivingBase entity, float age) {
		return entity.ticksExisted >= age;
	}

	/**
	 * @return The closest entity to the player
	 * */
	public static EntityLivingBase getClosestEntityToPlayer(Entity toPlayer, float distance, float extender, boolean invisible, boolean mob, boolean animal, boolean player, boolean ageCheck) {
		EntityLivingBase currentEntity = null;
		for (int i = 0; i < mc.theWorld.loadedEntityList.size(); i++) {
			if (mc.theWorld.loadedEntityList.get(i) instanceof EntityLivingBase) {
				EntityLivingBase entity = (EntityLivingBase) mc.theWorld.loadedEntityList.get(i);
				if (isAliveNotUs(entity) && checkType(entity, invisible, mob, animal, player) && checkTeam(entity) && checkProperties(entity) && (ageCheck ? checkAge(entity) : true)) {
					if (isFriend(entity))
						continue;
					if (currentEntity != null) {
						if (toPlayer.getDistanceToEntity(entity) < toPlayer.getDistanceToEntity(currentEntity))
							currentEntity = entity;
					} else {
						if (toPlayer.getDistanceToEntity(entity) < distance + extender)
							currentEntity = entity;
					}
				}
			}
		}
		return currentEntity;
	}

	/**
	 * @return The closest entity to the player that requires the least change in yaw and pitch
	 * */
	public static EntityLivingBase getClosestEntity(float distance, float extender, boolean invisible, boolean mob, boolean animal, boolean player, boolean ageCheck) {
		EntityLivingBase currentEntity = null;
		for (int i = 0; i < mc.theWorld.loadedEntityList.size(); i++) {
			if (mc.theWorld.loadedEntityList.get(i) instanceof EntityLivingBase) {
				EntityLivingBase entity = (EntityLivingBase) mc.theWorld.loadedEntityList.get(i);
				if (isAliveNotUs(entity) && checkType(entity, invisible, mob, animal, player) && checkTeam(entity) && checkProperties(entity) && (ageCheck ? checkAge(entity) : true)) {
					if (isFriend(entity))
						continue;
					if (currentEntity != null) {
						if (isWithinDistance(entity, distance + extender) && isClosestToMouse(currentEntity, entity, distance, extender))
							currentEntity = entity;
					} else {
						if (isWithinDistance(entity, distance + extender))
							currentEntity = entity;
					}
				}
			}
		}
		return currentEntity;
	}

	/**
	 * @return The closest entity to our mouse that is within our FOV
	 * */
	public static EntityLivingBase getEntityWithinFOV(float fov, boolean invisible, boolean mob, boolean animal, boolean player, boolean ageCheck) {
		EntityLivingBase currentEntity = null;
		for (int i = 0; i < mc.theWorld.loadedEntityList.size(); i++) {
			if (mc.theWorld.loadedEntityList.get(i) instanceof EntityLivingBase) {
				EntityLivingBase entity = (EntityLivingBase) mc.theWorld.loadedEntityList.get(i);
				if (isAliveNotUs(entity) && checkType(entity, invisible, mob, animal, player) && checkTeam(entity) && checkProperties(entity) && (ageCheck ? checkAge(entity) : true)) {
					if (isFriend(entity))
						continue;
					if (currentEntity != null) {
						if (isWithinFOV(entity, fov) && isClosestToMouse(currentEntity, entity, -1, 0))
							currentEntity = entity;
					} else {
						if (isWithinFOV(entity, fov))
							currentEntity = entity;
					}
				}
			}
		}
		return currentEntity;
	}

	/**
	 * @return True if the entity is another player and does not contain any properties
	 * */
	public static boolean checkProperties(EntityLivingBase entity) {
		return !(entity instanceof EntityPlayer) || ((EntityPlayer) entity).getGameProfile().getProperties().size() > 0;
	}

	/**
	 * @return True if the entity is within the player's friends list
	 * */
	public static boolean isFriend(EntityLivingBase entity) {
		return entity instanceof EntityPlayer && huzuni.friendManager.isFriend(entity.getName());
	}

	/**
	 * @return True if the entity is another player and is on the player's team
	 * */
	public static boolean checkTeam(EntityLivingBase entity) {
		if (!huzuni.settings.team.isEnabled() || !huzuni.settings.team.hasSelected())
			return true;
		if (entity instanceof EntityPlayer)
			return !huzuni.settings.team.hasTeamColor(entity.getDisplayName().getFormattedText());
		else
			return true;
	}

	/**
	 * @return The closest entity to your mouse
	 */
	public static boolean isClosestToMouse(EntityLivingBase currentEntity, EntityLivingBase otherEntity, float distance, float extender) {

		// If we can't reach our current entity without the extender, but we CAN with our OTHER entity, return true.
		if (!isWithinDistance(currentEntity, distance) && isWithinDistance(otherEntity, distance))
			return true;

		float otherDist = getDistanceFromMouse(otherEntity),
				currentDist = getDistanceFromMouse(currentEntity);
		return (otherDist < currentDist || currentDist == -1) && otherDist != -1;
	}

	/**
	 * @return True if the entity is within the distance specified to the player
	 * */
	public static boolean isWithinDistance(EntityLivingBase entity, float distance) {
		return distance == -1 || mc.thePlayer.getDistanceToEntity(entity) < distance;
	}

	/**
	 * @return True if the entity is alive and not the player
	 * */
	public static boolean isAliveNotUs(EntityLivingBase entity) {
		if (entity.getName().equalsIgnoreCase(mc.thePlayer.getName()))
			return false;
		return (entity != null) && entity.isEntityAlive() && entity != mc.thePlayer;
	}

	/**
	 * @return Distance the entity is from our mouse.
	 */
	public static float getDistanceFromMouse(EntityLivingBase entity) {
		float[] neededRotations = getRotationsNeeded(entity);
		if (neededRotations != null) {
			float neededYaw = getYawDifference(mc.thePlayer.rotationYaw % 360F, neededRotations[0]), neededPitch = (mc.thePlayer.rotationPitch % 360F) - neededRotations[1];
			return net.halalaboos.mcwrapper.api.util.MathUtils.sqrt(neededYaw * neededYaw + neededPitch * neededPitch);
		}
		return -1F;
	}

	/**
	 * @return Rotations needed to face the position.
	 */
	public static float[] getRotationsNeeded(double x, double y, double z) {
		double xSize = x - mc.thePlayer.posX;
		double ySize = y - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
		double zSize = z - mc.thePlayer.posZ;

		double theta = (double) net.halalaboos.mcwrapper.api.util.MathUtils.sqrt(xSize * xSize + zSize * zSize);
		float yaw = (float) (Math.atan2(zSize, xSize) * 180.0D / Math.PI) - 90.0F;
		float pitch = (float) (-(Math.atan2(ySize, theta) * 180.0D / Math.PI));
		return new float[] {
				(mc.thePlayer.rotationYaw + net.halalaboos.mcwrapper.api.util.MathUtils.wrapDegrees(yaw - mc.thePlayer.rotationYaw)) % 360F,
				(mc.thePlayer.rotationPitch + net.halalaboos.mcwrapper.api.util.MathUtils.wrapDegrees(pitch - mc.thePlayer.rotationPitch)) % 360F,
		};
	}

	/**
	 * @return Rotations needed to face the entity.
	 */
	public static float[] getRotationsNeeded(EntityLivingBase entity) {
		if (entity == null)
			return null;
		return getRotationsNeeded(entity.posX, entity.posY + ((double) entity.getEyeHeight() / 2F), entity.posZ);
	}

	/**
	 * @return Rotations needed to face the entity without constantly snapping the player's head.
	 */
	public static float[] getRotationsNeededLenient(EntityLivingBase entity) {
		if (entity == null)
			return null;
		float yaw = mc.thePlayer.rotationYaw % 360F, pitch = mc.thePlayer.rotationPitch % 360F; /* Player's current yaw/pitch */
		float[] rotations = getRotationsNeeded(entity.posX, entity.posY + ((double) entity.getEyeHeight() / 2F), entity.posZ); /* Required yaw/pitch to be looking at center of the entity */
		float[] rotationCaps = getEntityCaps(entity);
		float yawDifference = getYawDifference(yaw, rotations[0]), pitchDifference = rotations[1] - pitch; /* Calculates the difference between player's yaw/pitch and the required yaw/pitch */

		if (yawDifference > rotationCaps[0] || yawDifference < -rotationCaps[0]) { /* If our yaw difference is outside of the maximum/minimum yaw required to be looking at an entity, let's change things up and move the yaw to the edge of the entity */
			if (yawDifference < 0) {
				yaw += yawDifference + rotationCaps[0];
			} else if (yawDifference > 0) {
				yaw += yawDifference - rotationCaps[0];
			}
		}
		if (pitchDifference > rotationCaps[1] || pitchDifference < -rotationCaps[1]) {
			if (pitchDifference < 0) {
				pitch += pitchDifference + rotationCaps[1];
			} else if (pitchDifference > 0) {
				pitch += pitchDifference - rotationCaps[1];
			}
		}
		return new float[] { yaw, pitch };
	}

	/**
	 * @return the yaw and pitch constraints needed to keep the player's head within an entity
	 * */
	public static float[] getEntityCaps(EntityLivingBase entity) {
		return getEntityCaps(entity, 6.5F);
	}

	/**
	 * @return Maximum/minimum rotation leniency allowed to still be considered 'inside' of a given entity.
	 * */
	public static float[] getEntityCaps(EntityLivingBase entity, float distance) {
		float distanceRatio = distance / mc.thePlayer.getDistanceToEntity(entity); /* I honestly do not remember my logic behind this and I don't want to bring out a notebook and figure out why this works, but it seems to work */
		float entitySize = 5F; /* magic number */
		return new float[] { distanceRatio * entity.width * entitySize, distanceRatio * entity.height * entitySize };
	}

	/**
	 * @return the difference between two yaw values
	 * */
	public static float getYawDifference(float currentYaw, float neededYaw) {
		float yawDifference = neededYaw - currentYaw;
		if (yawDifference > 180)
			yawDifference = -((360F - neededYaw) + currentYaw);
		else if (yawDifference < -180)
			yawDifference = ((360F - currentYaw) + neededYaw);

		return yawDifference;
	}

	/**
	 * Compares the needed yaw to the player's yaw and returns whether or not it is less than the fov.
	 * @return True if the entity is within the FOV specified.
	 * */
	public static boolean isWithinFOV(EntityLivingBase entity, float fov) {
		float[] rotations = getRotationsNeeded(entity);
		float yawDifference = getYawDifference(mc.thePlayer.rotationYaw % 360F, rotations[0]);
		return yawDifference < fov && yawDifference > -fov;
	}

	/**
	 * Raytraces to find a face on the block that can be seen by the player.
	 * */
	public static EnumFacing findFace(BlockPos position) {
		if (mc.theWorld.getBlockState(position).getBlock() != Blocks.air) {
			for (EnumFacing face : EnumFacing.values()) {
				Vec3 playerVec = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
				Vec3 blockVec = new Vec3(position.getX() + 0.5F + (float) (face.getDirectionVec().getX()) / 2F, position.getY() + 0.5F + (float) (face.getDirectionVec().getY()) / 2F, position.getZ() + 0.5F + (float) (face.getDirectionVec().getZ()) / 2F);
				MovingObjectPosition raytraceResult = mc.theWorld.rayTraceBlocks(playerVec, blockVec);
				if (raytraceResult == null || raytraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
					return face;
				}
			}
		}
		return null;
	}

	/**
	 * Finds the face of the first adjacent block that can be seen by the player.
	 * */
	public static EnumFacing getAdjacent(BlockPos position) {
		for (EnumFacing face : EnumFacing.values()) {
			BlockPos otherPosition = position.offset(face);
			if (mc.theWorld.getBlockState(otherPosition).getBlock() != Blocks.air) {
				EnumFacing otherFace = face.getOpposite();
				Vec3 playerVec = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
				Vec3 blockVec = new Vec3(otherPosition.getX() + 0.5F + (float) (otherFace.getDirectionVec().getX()) / 2F, otherPosition.getY() + 0.5F + (float) (otherFace.getDirectionVec().getY()) / 2F, otherPosition.getZ() + 0.5F + (float) (otherFace.getDirectionVec().getZ()) / 2F);
				MovingObjectPosition raytraceResult = mc.theWorld.rayTraceBlocks(playerVec, blockVec);
				if (raytraceResult == null || raytraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
					return face;
				}
			}
		}
		return null;
	}

	public static float getDamgage(EntityLivingBase entity, ItemStack item) {
		float attackAttribute = (float) mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.attackDamage).getBaseValue();
		if (item != null) {
			Multimap<String, AttributeModifier> attributes = item.getAttributeModifiers();
			Collection<AttributeModifier> attackModifier = attributes.get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
			for (AttributeModifier modifier : attackModifier) {
				attackAttribute += modifier.getAmount();
			}
			if (attackAttribute > 0.0F) {
				boolean hasKnockback = false;
				boolean hasCritical;
				hasCritical = hasKnockback && mc.thePlayer.fallDistance > 0.0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isPotionActive(Potion.blindness) && !mc.thePlayer.isRiding();
				hasCritical = hasCritical && !mc.thePlayer.isSprinting();
				if (hasCritical) {
					attackAttribute *= 1.5F;
				}
				attackAttribute = getDamageAfterMagicAbsorb(attackAttribute, (float) entity.getTotalArmorValue());
				attackAttribute = Math.max(attackAttribute - entity.getAbsorptionAmount(), 0.0F);
				return attackAttribute;
			} else
				return 0F;
		}
		return attackAttribute;
	}


	private static float getDamageAfterMagicAbsorb(float p_188401_0_, float p_188401_1_) {
		float f = MathUtils.clamp(p_188401_1_, 0.0F, 20.0F);
		return p_188401_0_ * (1.0F - f / 25.0F);
	}
}
