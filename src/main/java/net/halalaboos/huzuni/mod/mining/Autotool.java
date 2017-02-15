package net.halalaboos.huzuni.mod.mining;

import net.halalaboos.huzuni.api.event.EventManager.EventMethod;
import net.halalaboos.huzuni.api.event.PacketEvent;
import net.halalaboos.huzuni.api.event.UpdateEvent;
import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.halalaboos.huzuni.api.settings.Toggleable;
import net.halalaboos.huzuni.api.settings.Value;
import net.halalaboos.huzuni.api.task.HotbarTask;
import net.halalaboos.huzuni.api.util.MinecraftUtils;
import net.halalaboos.huzuni.api.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

/**
 * Automatically switches to the best tool needed for the player's given situation.
 * */
public class Autotool extends BasicMod {
	
	public final Toggleable weapon = new Toggleable("Swap weapon", "Swaps to the best weapon when in combat.");

	public final Value combatTime = new Value("Combat time", " ms", 100F, 1000F, 5000F, 50F, "Time required to pass until no longer considered in combat.");
	
	private final Timer timer = new Timer();
	
	private final HotbarTask hotbarTask = new HotbarTask(this) {

		@Override
		protected boolean isValid(ItemStack itemStack) {
			if (itemStack != null) {
				if (digging && hasBlock()) {
					return getRelativeBlockHardness(position, blockState, blockState.getBlock(), itemStack) > 0.055555556F;
				} else if (weapon.isEnabled() && !digging && hasEntity()) {
					return MinecraftUtils.getDamgage(entity, itemStack) > 1F;
				} else
					return false;
			} else
				return false;
		}
		
		@Override
		protected boolean compare(ItemStack currentItem, ItemStack newItem) {
			if (newItem != null) {
				if (digging) {
					float currentHardness = getRelativeBlockHardness(position, blockState, blockState.getBlock(), currentItem);
					float newHardness = getRelativeBlockHardness(position, blockState, blockState.getBlock(), newItem);	
					return currentHardness < newHardness;
				} else {
					float currentDamage = MinecraftUtils.getDamgage(entity, currentItem);
					float newDamage = MinecraftUtils.getDamgage(entity, newItem);
					return currentDamage < newDamage;
				}
			} else
				return false;
		}
	};
	
	private final Potion digSpeed, digSlowdown;
	
	private boolean digging = false;
	
	private EntityLivingBase entity = null;
	
	private IBlockState blockState = null;
	
	private BlockPos position = null;
	
	public Autotool() {
		super("Auto tool", "Switches to the best tool in your hotbar when mining", Keyboard.KEY_J);
		this.setCategory(Category.MINING);
		setAuthor("Halalaboos");
		this.addChildren(weapon, combatTime);
		weapon.setEnabled(true);
		digSpeed = Potion.getPotionFromResourceLocation("haste");
		digSlowdown = Potion.getPotionFromResourceLocation("mining_fatigue");
		huzuni.hotbarManager.registerTaskHolder(this);
	}
	
	@Override
	public void onEnable() {
		huzuni.eventManager.addListener(this);
	}
	
	@Override
	public void onDisable() {
		huzuni.eventManager.removeListener(this);
		huzuni.hotbarManager.withdrawTask(hotbarTask);
	}
	
	@EventMethod
	public void onPacket(PacketEvent event) {
		if (event.getPacket() instanceof C07PacketPlayerDigging) {
			C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.getPacket();
			if (!mc.playerController.isInCreativeMode() && packet.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
				this.digging = true;
				this.entity = null;
				this.position = packet.getPosition();
				this.blockState = mc.theWorld.getBlockState(this.position);
				huzuni.hotbarManager.requestTask(this, hotbarTask);
			}
		} else if (event.getPacket() instanceof C02PacketUseEntity) {
			C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
			if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
				Entity entity = packet.getEntityFromWorld(mc.theWorld);
				if (entity instanceof EntityLivingBase) {
					this.digging = false;
					this.position = null;
					this.blockState = null;
					this.entity = (EntityLivingBase) entity;
					timer.reset();
					huzuni.hotbarManager.requestTask(this, hotbarTask);
				}
			}
		}
	}
	
	@EventMethod
	public void onUpdate(UpdateEvent event) {
		if (!mc.gameSettings.keyBindAttack.isKeyDown() && digging) {
			this.blockState = null;
			this.position = null;
			this.digging = false;
			huzuni.hotbarManager.withdrawTask(hotbarTask);
		}
		if (timer.hasReach((int) combatTime.getValue()) && weapon.isEnabled() && !digging) {
			this.entity = null;
			huzuni.hotbarManager.withdrawTask(hotbarTask);
		}
	}

	/**
     * @return True if the auto tool has a block.
     * */
	private boolean hasBlock() {
		return blockState != null && position != null;
	}

	/**
     * @return True if the auto tool has an entity.
     * */
	private boolean hasEntity() {
		return entity != null;
	}

	/**
     * @return The block hardness relative to the item stack.
     * */
	public float getRelativeBlockHardness(BlockPos position, IBlockState blockState, Block block, ItemStack item) {
        float blockHardness = blockState.getBlock().getBlockHardness(mc.theWorld, position);
        return blockHardness < 0.0F ? 0.0F : (!canHarvestBlock(block, item) ? getStrength(block, item) / blockHardness / 100.0F : getStrength(block, item) / blockHardness / 30.0F);
    }

    /**
     * @return True if the item stack can harvest the block.
     * */
	public boolean canHarvestBlock(Block block, ItemStack item) {
		return block.getMaterial().isToolNotRequired() || item != null && item.canHarvestBlock(block);
	}

	/**
     * @return The strength of the item stack vs the block.
     * */
	public float getStrength(Block block, ItemStack item) {
		float strength = item.getStrVsBlock(block);
		if (strength > 1.0F) {
			int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, item);

			if (efficiency > 0) {
				strength += (float) (efficiency * efficiency + 1);
			}
		}
		
		if (mc.thePlayer.isPotionActive(digSpeed)) {
			strength *= 1.0F + (float) (mc.thePlayer.getActivePotionEffect(digSpeed).getAmplifier() + 1) * 0.2F;
		}
		
		if (mc.thePlayer.isPotionActive(digSlowdown)) {
			float poitionModifier = 1.0F;

			switch (mc.thePlayer.getActivePotionEffect(digSlowdown).getAmplifier()) {
			case 0:
				poitionModifier = 0.3F;
				break;

			case 1:
				poitionModifier = 0.09F;
				break;

			case 2:
				poitionModifier = 0.0027F;
				break;

			case 3:
			default:
				poitionModifier = 8.1E-4F;
			}

			strength *= poitionModifier;
		}

		if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
			strength /= 5.0F;
		}


		if (!mc.thePlayer.onGround) {
			strength /= 5.0F;
		}
		return strength;
	}

}
