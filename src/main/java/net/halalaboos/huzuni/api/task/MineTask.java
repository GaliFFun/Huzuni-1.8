package net.halalaboos.huzuni.api.task;

import net.halalaboos.huzuni.api.mod.Mod;
import net.halalaboos.huzuni.api.util.MathUtils;
import net.halalaboos.huzuni.api.util.Timer;
import net.halalaboos.huzuni.mod.movement.Freecam;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;

/**
 * Look task which can take block data and simulate server-sided mining.
 * */
public class MineTask extends LookTask {
	
	protected final Timer timer = new Timer();
	
	protected EnumFacing face;
	
	protected BlockPos position;
		
	protected float curBlockDamage = 0F;
	
	protected boolean digging = false;
	
	protected int mineDelay = 100;
		
	public MineTask(Mod mod) {
		super(mod);
	}
	
	public MineTask(Mod mod, BlockPos position, EnumFacing face) {
		super(mod, position.getX(), position.getY(), position.getZ());
		this.position = position;
		this.face = face;
	}
	
	@Override
	public void onPreUpdate() {
		if (timer.hasReach(mineDelay) && hasBlock() && !Freecam.INSTANCE.isEnabled()) {
			this.setRotations(position, face);
			super.onPreUpdate();
		}
	}
	
	@Override
	public void onPostUpdate() {
		if (timer.hasReach(mineDelay) && hasBlock() && !Freecam.INSTANCE.isEnabled()) {
			super.onPostUpdate();
			if (!blockExists() || !isWithinDistance()) {
				if (digging) {
					sendPacket(1);
					onTaskFinishPremature(position, face);
				}
				reset();
				return;
			}
			Block blockState = getBlockState();
			mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
			if (curBlockDamage <= 0F) {
				digging = true;
				sendPacket(0);
				if (mc.playerController.isInCreativeMode() || blockState.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, position) >= 1F) {
					mc.theWorld.setBlockToAir(position);
					onTaskFinish(position, face);
					reset();
					return;
				}
			}
			curBlockDamage += blockState.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, this.position);
			mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), position, (int) (curBlockDamage * 10.0F) - 1);
			if (curBlockDamage >= 1F) {
				mc.theWorld.setBlockToAir(position);
				sendPacket(2);
				onTaskFinish(position, face);
				reset();
			}
		}
	}
	
	@Override
	public void onTaskCancelled() {
		if (isMining()) {
			sendPacket(1);
			onTaskFinishPremature(position, face);
			reset();
		}
	}
	
	@Override
	public void setRunning(boolean running) {
		super.setRunning(running);
		if (!running && isMining()) {
			sendPacket(1);
			onTaskFinishPremature(position, face);
			reset();
		}
	}
	
	public int getMineDelay() {
		return mineDelay;
	}

	public void setMineDelay(int mineDelay) {
		this.mineDelay = mineDelay;
	}

	/**
	 * @return True if the block is within player reach distance.
	 * */
	public boolean isWithinDistance() {
		return MathUtils.getDistance(position) < mc.playerController.getBlockReachDistance();
	}

	/**
	 * @return True if the block is not air.
	 * */
	public boolean blockExists() {
		return getBlockState().getMaterial() != Material.air;
	}
	
	public boolean isMining() {
		return digging;
	}

	/**
	 * Sends a mining packet based on the mode given.
	 * */
	private void sendPacket(int mode) {
		C07PacketPlayerDigging.Action action = mode == 0 ? C07PacketPlayerDigging.Action.START_DESTROY_BLOCK : (mode == 1 ? C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK : (mode == 2 ? C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK : null));
		mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(action, position, face));
	}
	
	private void onTaskFinishPremature(BlockPos position, EnumFacing face) {
		timer.reset();
	}

	private void onTaskFinish(BlockPos position, EnumFacing face) {
		timer.reset();
	}

	/**
	 * Cancels the mining simulation if the block is not air.
	 * */
	public void cancelMining() {
		if (hasBlock()) {
			if (isMining()) {
				sendPacket(1);
				onTaskFinishPremature(position, face);
			}
			reset();
		}
	}

	/**
	 * Sets the block position and face value.
	 * */
	public void setBlock(BlockPos position, EnumFacing face) {
		this.position = position;
		this.face = face;
		if (position != null && face != null)
			this.setRotations(position, face);
	}

	/**
     * @return True if the position and face values are present for this task.
     * */
	public boolean hasBlock() {
		return position != null && face != null;
	}

	/**
     * Resets the mining information.
     * */
	protected void reset() {
		setBlock(null, null);
		curBlockDamage = 0F;
		digging = false;
		timer.reset();
	}
	
	protected Block getBlockState() {
		return mc.theWorld.getBlockState(position).getBlock();
	}
	
	protected boolean shouldRotate() {
		return isWithinDistance();
	}
	
}
