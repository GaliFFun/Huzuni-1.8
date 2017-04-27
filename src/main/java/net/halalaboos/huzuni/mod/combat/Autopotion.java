package net.halalaboos.huzuni.mod.combat;

import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.halalaboos.huzuni.api.node.impl.Toggleable;
import net.halalaboos.huzuni.api.node.impl.Value;
import net.halalaboos.huzuni.api.task.ClickTask;
import net.halalaboos.huzuni.api.task.HotbarTask;
import net.halalaboos.huzuni.api.task.LookTask;
import net.halalaboos.huzuni.api.util.MinecraftUtils;
import net.halalaboos.huzuni.api.util.Timer;
import net.halalaboos.huzuni.gui.Notification.NotificationType;
import net.halalaboos.mcwrapper.api.MCWrapper;
import net.halalaboos.mcwrapper.api.entity.living.player.Hand;
import net.halalaboos.mcwrapper.api.event.player.PostMotionUpdateEvent;
import net.halalaboos.mcwrapper.api.event.player.PreMotionUpdateEvent;
import net.halalaboos.mcwrapper.api.item.ItemStack;
import net.halalaboos.mcwrapper.api.item.types.GlassBottle;
import net.halalaboos.mcwrapper.api.item.types.PotionItem;
import net.halalaboos.mcwrapper.api.potion.PotionEffect;
import net.halalaboos.mcwrapper.api.util.enums.ActionResult;

import java.util.Optional;

import static net.halalaboos.mcwrapper.api.MCWrapper.*;

/**
 * Automatically uses potions when the health reaches below a threshold. <br/>
 * Moves potions from the inventory to the hotbar.
 * */
public class Autopotion extends BasicMod {

	public final Value swapDelay = new Value("Swap Delay", " ms", 0F, 200F, 2000F, 10F, "Delay in MS between each inventory move attempt");

	public final Value useDelay = new Value("Use Delay", " ms", 0F, 400F, 2000F, 10F, "Delay in MS between each item use attempt");

	public final Value healthAmount = new Value("Health", "", 1F, 12F, 19F, 1F, "Minimum health required before attempting to use potions");

	public final Toggleable usePotions = new Toggleable("Use Potions", "Automatically attempt to use potions");

	private final Timer timer = new Timer(), useTimer = new Timer();

	private final LookTask lookTask = new LookTask(this);

	private final HotbarTask hotbarTask = new HotbarTask(this) {

		@Override
		protected boolean isValid(ItemStack itemStack) {
			return !itemStack.empty() && itemStack.getItemType() instanceof PotionItem;
		}

	};

	private final ClickTask clickTask = new ClickTask(this);

	private  net.halalaboos.mcwrapper.api.potion.Potion health;

	public Autopotion() {
		super("Auto potion", "Automagically uses health potions.");
		setCategory(Category.COMBAT);
		setAuthor("Halalaboos");
		addChildren(swapDelay, useDelay, healthAmount, usePotions);
		huzuni.lookManager.registerTaskHolder(this);
		huzuni.hotbarManager.registerTaskHolder(this);
		huzuni.clickManager.registerTaskHolder(this);
		subscribe(PreMotionUpdateEvent.class, this::onPreUpdate);
		subscribe(PostMotionUpdateEvent.class, this::onPostUpdate);
	}

	@Override
	public void onDisable() {
		huzuni.lookManager.withdrawTask(lookTask);
		huzuni.hotbarManager.withdrawTask(hotbarTask);
		huzuni.clickManager.withdrawTask(clickTask);
	}

	private void onPreUpdate(PreMotionUpdateEvent event) {
		if (mc.isScreenOpen()) return;
		if (health == null) {
			health = getAdapter().getPotionRegistry().getPotion("instant_health");
		}
		if (huzuni.hotbarManager.hasPriority(this) && huzuni.lookManager.hasPriority(this) && huzuni.clickManager.hasPriority(this) && usePotions.isEnabled() && needUsePotion()) {
			int hotbarPotion = findHotbarPotion();
			if (hotbarPotion != -1 && useTimer.hasReach((int) useDelay.getValue())) {
				lookTask.setRotations(getPlayer().getYaw(), 90);
				huzuni.lookManager.requestTask(this, lookTask);
				huzuni.hotbarManager.requestTask(this, hotbarTask);
				useTimer.reset();
			} else {
				huzuni.lookManager.withdrawTask(lookTask);
				huzuni.hotbarManager.withdrawTask(hotbarTask);
				movePotions();
			}
		} else {
			huzuni.lookManager.withdrawTask(lookTask);
			huzuni.hotbarManager.withdrawTask(hotbarTask);
			movePotions();
		}
		if (clickTask.hasClicks())
			huzuni.clickManager.requestTask(this, clickTask);
		else
			huzuni.clickManager.withdrawTask(clickTask);
	}

	private void onPostUpdate(PostMotionUpdateEvent event) {
		if (mc.isScreenOpen()) return;
		if (lookTask.isRunning() && hotbarTask.isRunning()) {
			usePotion();
		}
	}
	/**
	 * Moves items from the inventory to the hot bar.
	 * */
	private void movePotions() {
		int emptySlot = findEmptyPotion();
		if (emptySlot != -1) {
			int newPotion = getUseablePotion();
			if (newPotion != -1) {
				if (getPlayer().getPlayerInventory().getStack(newPotion).isPresent()) {
					ItemStack itemStack = getPlayer().getPlayerInventory().getStack(newPotion).get();
					if (MinecraftUtils.isShiftable(itemStack)) {
						if (clickTask.containsClick(newPotion, 0, 1))
							timer.reset();
						else if (timer.hasReach((int) swapDelay.getValue())) {
							clickTask.add(newPotion, 0, 1);
							huzuni.addNotification(NotificationType.INFO, this, 5000, "Shift-clicking potion!");
						}
					}
				} else {
					if (clickTask.containsClick(newPotion, 0, 0))
						timer.reset();
					else if (timer.hasReach((int) swapDelay.getValue())) {
						huzuni.addNotification(NotificationType.INFO, this, 5000, "Moving potion!");
						clickTask.add(newPotion, 0, 0);
						clickTask.add(emptySlot, 0, 0);
						clickTask.add(newPotion, 0, 0);
					}
				}
			}
		}
	}

	/**
	 * @return The index for the first usable potion found within the inventory.
	 */
	private int getUseablePotion() {
		for (int o = 9; o < 36; o++) {
			if (getPlayer().getInventoryContainer().getSlotAt(o).getItem().isPresent()) {
				ItemStack item = getPlayer().getInventoryContainer().getSlotAt(o).getItem().get();
				if (isPotion(item))
					return o;
			}
		}
		return -1;
	}

	/**
	 * @return The first index within the hotbar which either contains no item or an empty glass bottle.
	 * */
	private int findEmptyPotion() {
		for (int o = 36; o < 45; o++) {
			Optional<ItemStack> item = getPlayer().getInventoryContainer().getSlotAt(o).getItem();
			if (!item.isPresent())
				return o;
			else if (item.get().getItemType() instanceof GlassBottle)
				return o;
		}
		return -1;
	}

	/**
	 * @return The index of the first potion within the hotbar.
	 * */
	private int findHotbarPotion() {
		for (int o = 0; o < 9; o++) {
			Optional<ItemStack> item = getPlayer().getPlayerInventory().getStack(o);
			if (item.isPresent() && isPotion(item.get()))
				return o;
		}
		return -1;
	}

	/**
	 * @return True if the item stack is a health potion.
	 * */
	private boolean isPotion(ItemStack itemStack) {
		if (MCWrapper.getAdapter().getPotionRegistry().isSplash(itemStack)) {
			for (PotionEffect effect : getAdapter().getPotionRegistry().getEffects(itemStack)) {
				if (effect.getEffect() == health) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return True if the player's health reaches below the threshold.
	 * */
	private boolean needUsePotion()
	{
		return getPlayer().getHealthData().getCurrentHealth() <= healthAmount.getValue();
	}

	/**
	 * Attempts to use a potion.
	 * */
	private void usePotion() {
		if (getController().rightClick(Hand.MAIN) == ActionResult.SUCCESS) {
			huzuni.addNotification(NotificationType.CONFIRM, this, 5000, "Using potion!");
		}
	}

}
