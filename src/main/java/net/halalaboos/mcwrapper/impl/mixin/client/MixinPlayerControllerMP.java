package net.halalaboos.mcwrapper.impl.mixin.client;

import net.halalaboos.mcwrapper.api.MCWrapper;
import net.halalaboos.mcwrapper.api.client.Controller;
import net.halalaboos.mcwrapper.api.entity.Entity;
import net.halalaboos.mcwrapper.api.entity.living.player.GameType;
import net.halalaboos.mcwrapper.api.entity.living.player.Hand;
import net.halalaboos.mcwrapper.api.event.player.BlockDigEvent;
import net.halalaboos.mcwrapper.api.item.ItemStack;
import net.halalaboos.mcwrapper.api.util.enums.ActionResult;
import net.halalaboos.mcwrapper.api.util.enums.ClickType;
import net.halalaboos.mcwrapper.api.util.enums.Face;
import net.halalaboos.mcwrapper.api.util.math.Vector3d;
import net.halalaboos.mcwrapper.api.util.math.Vector3i;
import net.halalaboos.mcwrapper.impl.Convert;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP implements Controller {

	@Shadow private float curBlockDamageMP;
	@Shadow private int blockHitDelay;
	@Shadow private boolean isHittingBlock;
	@Shadow public abstract float getBlockReachDistance();
	@Shadow public abstract void resetBlockRemoving();
	@Shadow public abstract void updateController();
	@Shadow public abstract WorldSettings.GameType getCurrentGameType();

	@Shadow public abstract boolean onPlayerRightClick(EntityPlayerSP player, WorldClient worldIn, net.minecraft.item.ItemStack heldStack, BlockPos hitPos, EnumFacing side, Vec3 hitVec);

	@Shadow public abstract net.minecraft.item.ItemStack windowClick(int windowId, int slotId, int mouseButton, int type, EntityPlayer player);

	@Shadow public abstract void attackEntity(EntityPlayer playerIn, net.minecraft.entity.Entity targetEntity);

	@Shadow public abstract boolean sendUseItem(EntityPlayer playerIn, World worldIn, net.minecraft.item.ItemStack itemStackIn);

	@Shadow public abstract boolean onPlayerDestroyBlock(BlockPos pos, EnumFacing side);

	@Shadow public abstract boolean interactWithEntitySendPacket(EntityPlayer playerIn, net.minecraft.entity.Entity targetEntity);

	private BlockDigEvent blockDigEvent;

	@Inject(method = "onPlayerDamageBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;curBlockDamageMP:F", ordinal = 0, shift = At.Shift.BEFORE))
	public void addBlockDigEvent(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Boolean> ci) {
		this.blockDigEvent = new BlockDigEvent(Convert.from(pos), this.curBlockDamageMP, facing.ordinal());
		MCWrapper.getEventManager().publish(this.blockDigEvent);
		this.curBlockDamageMP = this.blockDigEvent.progress;
	}

	@Redirect(method = "onPlayerDamageBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getPlayerRelativeBlockHardness(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)F"))
	public float getModifiedHardness(Block block, EntityPlayer playerIn, World worldIn, BlockPos pos) {
		return block.getPlayerRelativeBlockHardness(playerIn, worldIn, pos) * blockDigEvent.multiplier;
	}

	@Override
	public float getBlockDamage() {
		return curBlockDamageMP;
	}

	@Override
	public void setBlockDamage(float damage) {
		this.curBlockDamageMP = damage;
	}

	@Override
	public int getHitDelay() {
		return blockHitDelay;
	}

	@Override
	public void setHitDelay(int hitDelay) {
		this.blockHitDelay = hitDelay;
	}

	@Override
	public boolean isHittingBlock() {
		return isHittingBlock;
	}

	@Override
	public float getBlockReach() {
		return getBlockReachDistance();
	}

	@Override
	public void resetDigging() {
		resetBlockRemoving();
	}

	@Override
	public void update() {
		updateController();
	}

	@Override
	public GameType getGameType() {
		return GameType.values()[getCurrentGameType().ordinal()];
	}

	@Override
	public boolean onBlockDestroy(Vector3i blockPosition) {
		//side param isn't used, so we can use whatever we want here
		return onPlayerDestroyBlock(Convert.to(blockPosition), EnumFacing.UP);
	}

	@Override
	public ActionResult rightClickBlock(Vector3i pos, Face direction, Vector3d vec, Hand hand) {
		boolean result = onPlayerRightClick(Convert.player(), Convert.world(), Convert.player().getHeldItem(), Convert.to(pos),
				Convert.to(direction), Convert.to(vec));
		return result ? ActionResult.SUCCESS : ActionResult.FAIL;
	}

	@Override
	public ItemStack clickSlot(int windowId, int slot, int mouseButton, ClickType type) {
		//todo test
		return Convert.from(windowClick(windowId, slot, mouseButton, type.ordinal(), Convert.player()));
	}

	@Override
	public ActionResult interactWith(Entity target, Hand hand) {
		return interactWithEntitySendPacket(Convert.player(), (net.minecraft.entity.Entity)target) ? ActionResult.SUCCESS : ActionResult.FAIL;
	}

	@Override
	public void attack(Entity target) {
		attackEntity(Convert.player(), (net.minecraft.entity.Entity)target);
	}

	@Override
	public ActionResult rightClick(Hand hand) {
		return sendUseItem(Convert.player(), Convert.world(), Convert.player().getHeldItem()) ? ActionResult.SUCCESS : ActionResult.FAIL;
	}
}
