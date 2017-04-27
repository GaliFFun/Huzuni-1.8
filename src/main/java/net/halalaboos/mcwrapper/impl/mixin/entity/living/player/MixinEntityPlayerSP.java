package net.halalaboos.mcwrapper.impl.mixin.entity.living.player;

import com.google.common.collect.Multimap;
import net.halalaboos.huzuni.Huzuni;
import net.halalaboos.mcwrapper.api.MCWrapper;
import net.halalaboos.mcwrapper.api.client.ClientPlayer;
import net.halalaboos.mcwrapper.api.client.Input;
import net.halalaboos.mcwrapper.api.entity.living.Living;
import net.halalaboos.mcwrapper.api.entity.living.player.GameType;
import net.halalaboos.mcwrapper.api.entity.living.player.Hand;
import net.halalaboos.mcwrapper.api.entity.living.player.Player;
import net.halalaboos.mcwrapper.api.event.player.MoveEvent;
import net.halalaboos.mcwrapper.api.event.player.PostMotionUpdateEvent;
import net.halalaboos.mcwrapper.api.event.player.PreMotionUpdateEvent;
import net.halalaboos.mcwrapper.api.item.ItemStack;
import net.halalaboos.mcwrapper.api.network.PlayerInfo;
import net.halalaboos.mcwrapper.api.util.math.Vector3i;
import net.halalaboos.mcwrapper.impl.Convert;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovementInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(net.minecraft.client.entity.EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer implements ClientPlayer {

	@Shadow public abstract void swingItem();
	@Shadow public abstract void closeScreen();
	@Shadow public MovementInput movementInput;
	@Shadow @Final public NetHandlerPlayClient sendQueue;
	@Shadow public abstract void onUpdateWalkingPlayer();

	@Shadow private String clientBrand;

	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	public void sendChatMessage(String message, CallbackInfo ci) {
		if (message.startsWith(huzuni.commandManager.getCommandPrefix())) {
			huzuni.commandManager.processCommand(message.substring(huzuni.commandManager.getCommandPrefix().length()));
			ci.cancel();
		}
		if (message.startsWith("~") && message.length() > 1) {
			this.sendQueue.addToSendQueue(new C01PacketChatMessage(message.substring(1, message.length())));
			ci.cancel();
		}
	}

	@Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V", shift = At.Shift.AFTER))
	public void noSlow(CallbackInfo ci) {
		if (this.isUsingItem() && !this.isRiding() && !getItemUseSlowdown()) {
			this.movementInput.moveStrafe *= 5F;
			this.movementInput.moveForward *= 5F;
		}
	}

	private PreMotionUpdateEvent preMotion = new PreMotionUpdateEvent();
	private PostMotionUpdateEvent postMotion = new PostMotionUpdateEvent();
	private Huzuni huzuni = Huzuni.INSTANCE;

	@Inject(method = "onUpdate", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V",
			shift = At.Shift.BEFORE), cancellable = true)
	private void dispatchUpdateEvents(CallbackInfo ci) {
		preMotion.setCancelled(false);
		MCWrapper.getEventManager().publish(preMotion);
		if (preMotion.isCancelled()) {
			huzuni.lookManager.cancelTask();
			ci.cancel();
		}
		huzuni.clickManager.onPreUpdate(preMotion);
		huzuni.hotbarManager.onPreUpdate(preMotion);
		huzuni.lookManager.onPreUpdate(preMotion);
			onUpdateWalkingPlayer();
		huzuni.lookManager.onPostUpdate();
		huzuni.hotbarManager.onPostUpdate();
		huzuni.clickManager.onPostUpdate();
		MCWrapper.getEventManager().publish(postMotion);
		ci.cancel();
	}

	private final MoveEvent event = new MoveEvent(0, 0, 0);

	@Override
	public void moveEntity(double x, double y, double z) {
		event.setMotionX(x);
		event.setMotionY(y);
		event.setMotionZ(z);
		MCWrapper.getEventManager().publish(event);
		super.moveEntity(event.getMotionX(), event.getMotionY(), event.getMotionZ());
	}

	@Override
	public boolean isUnderStairs() {
		EnumFacing face = getHorizontalFacing();
		IBlockState blockState = worldObj.getBlockState(new BlockPos(posX, posY - 1, posZ));
		IBlockState nextBlockState = worldObj.getBlockState(new BlockPos(posX + face.getDirectionVec().getX(), posY, posZ + face.getDirectionVec().getZ()));

		if (BlockStairs.isBlockStairs(blockState.getBlock()) && BlockStairs.isBlockStairs(nextBlockState.getBlock())) {
			EnumFacing blockFace = blockState.getValue(BlockStairs.FACING);
			EnumFacing nextBlockFace = blockState.getValue(BlockStairs.FACING);
			return face.equals(blockFace) && face.equals(nextBlockFace);
		} else
			return false;
	}

	@Override
	public void swingItem(Hand hand) {
		swingItem();
	}

	@Override
	public void closeWindow() {
		closeScreen();
	}

	@Override
	public boolean isFlying() {
		return capabilities.isFlying;
	}

	@Override
	public void setFlying(boolean flying) {
		capabilities.isFlying = flying;
	}

	@Override
	public String getBrand() {
		return clientBrand;
	}

	@Override
	public void sendMessage(String message) {
		sendQueue.addToSendQueue(new C01PacketChatMessage(message));
	}

	@Override
	public PlayerInfo getInfo(Player player) {
		return ((PlayerInfo) sendQueue.getPlayerInfo(player.getUUID()));
	}

	@Override
	public boolean isNPC() {
		return false;
	}

	@Override
	public void setItemUseSlowdown(boolean slowdown) {
		this.itemSlowdown = slowdown;
	}

	@Override
	public boolean getItemUseSlowdown() {
		return itemSlowdown;
	}

	private boolean itemSlowdown = true;

	@Override
	public boolean canBePushed() {
		return pushable && super.canBePushed();
	}

	@Override
	public void setPushable(boolean pushed) {
		this.pushable = pushed;
	}

	private boolean pushable = true;

	@Override
	public float calculateDamage(Living target, ItemStack weapon, float cooldown) {
		EntityLivingBase entity = (EntityLivingBase) target;
		net.minecraft.item.ItemStack item = Convert.to(weapon);
		float attackAttribute = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getBaseValue();
		if (item != null) {
			Multimap<String, AttributeModifier> attributes = item.getAttributeModifiers();
			Collection<AttributeModifier> attackModifier = attributes.get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
			for (AttributeModifier modifier : attackModifier) {
				attackAttribute += modifier.getAmount();
			}

			float enchantModifier = EnchantmentHelper.getModifierForCreature(item, entity.getCreatureAttribute());
			attackAttribute *= (0.2F + cooldown * cooldown * 0.8F);
			enchantModifier *= cooldown;
			if (attackAttribute > 0.0F || enchantModifier > 0.0F) {
				boolean hasKnockback = false;
				boolean hasCritical = false;
				hasCritical = hasKnockback && fallDistance > 0.0F && !onGround && !isOnLadder() && !isInWater() && !isPotionActive(Potion.blindness) && !isRiding();
				hasCritical = hasCritical && !isSprinting();
				if (hasCritical) {
					attackAttribute *= 1.5F;
				}
				attackAttribute += enchantModifier;

				attackAttribute = getDamageAfterPotion(attackAttribute, (float) entity.getTotalArmorValue());
				attackAttribute = Math.max(attackAttribute - entity.getAbsorptionAmount(), 0.0F);
				return attackAttribute;
			} else
				return 0F;
		}
		return attackAttribute;
	}

	@Override
	public float calculateDamageWithAttackSpeed(Living target, ItemStack item) {
		return calculateDamage(target, item, 1.0F);
	}

	private float getDamageAfterPotion(float damage, float enchantLevel) {
		float f = net.halalaboos.mcwrapper.api.util.math.MathUtils.clamp(enchantLevel, 0.0F, 20.0F);
		return damage * (1.0F - f / 25.0F);
	}

	@Override
	public boolean isGameType(GameType type) {
		switch (type) {
			case CREATIVE: return isCreative();
			case SPECTATOR: return isSpectator();
			default:
				return type == MCWrapper.getController().getGameType();
		}
	}

	@Override
	public float getDigStrength(Vector3i position, ItemStack stack) {
		net.minecraft.item.ItemStack item = Convert.to(stack);
		if (item == null) return 1F;

		IBlockState state = worldObj.getBlockState(Convert.to(position));
		float strength = item.getStrVsBlock(state.getBlock());

		if (strength > 1.0F) {
			int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, item);

			if (efficiency > 0) {
				strength += (float) (efficiency * efficiency + 1);
			}
		}

		if (isInsideOfMaterial(Material.water) &&
				!EnchantmentHelper.getAquaAffinityModifier((EntityPlayerSP)(Object)this)) {
			strength /= 5.0F;
		}

		if (!onGround) {
			strength /= 5.0F;
		}

		return strength;
	}

	@Override
	public boolean canHarvestBlock(Vector3i position, ItemStack item) {
		net.minecraft.item.ItemStack stack = Convert.to(item);
		IBlockState state = worldObj.getBlockState(Convert.to(position));
		return state.getBlock().getMaterial().isToolNotRequired() || stack != null && stack.canHarvestBlock(state.getBlock());
	}

	@Override
	public float getRelativeHardness(Vector3i position, ItemStack item) {
		IBlockState blockState = worldObj.getBlockState(Convert.to(position));
		float blockHardness = blockState.getBlock().getBlockHardness(worldObj, Convert.to(position));
		return blockHardness < 0.0F ? 0.0F : (!canHarvestBlock(position, item) ? getDigStrength(position, item) / blockHardness / 100.0F : getDigStrength(position, item) / blockHardness / 30.0F);
	}

	@Override
	public Input getInput() {
		return (Input)movementInput;
	}

	@Override
	public boolean isSlowedByBlocks() {
		return this.slowedByBlocks;
	}

	@Override
	public void setSlowedByBlocks(boolean slowedByBlocks) {
		this.slowedByBlocks = slowedByBlocks;
	}

	private boolean slowedByBlocks = true;
}
