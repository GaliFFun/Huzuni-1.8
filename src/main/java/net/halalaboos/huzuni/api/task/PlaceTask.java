package net.halalaboos.huzuni.api.task;

import net.halalaboos.huzuni.api.mod.Mod;
import net.halalaboos.huzuni.api.util.MathUtils;
import net.halalaboos.huzuni.api.util.Timer;
import net.halalaboos.huzuni.mod.movement.Freecam;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

/**
 * Look task which simulates block placement server-sided.
 * */
public class PlaceTask extends LookTask {
	
	protected final Timer timer = new Timer();
	
	protected EnumFacing face;
	
	protected BlockPos position;
			
	protected int placeDelay = 100;
	
	protected boolean naturalPlacement = true;
			
	public PlaceTask(Mod mod) {
		super(mod);
	}
	
	public PlaceTask(Mod mod, BlockPos position, EnumFacing face) {
		super(mod, position.getX(), position.getY(), position.getZ());
		this.position = position;
		this.face = face;
	}
	
	@Override
	public void onPreUpdate() {
		if (timer.hasReach(placeDelay) && hasBlock() && shouldRotate() && !Freecam.INSTANCE.isEnabled()) {
			this.setRotations(position, face);
			if (shouldResetBlock()) {
				reset();
				return;
			}
			super.onPreUpdate();
		}
	}
	
	@Override
	public void onPostUpdate() {
		if (timer.hasReach(placeDelay) && hasBlock() && shouldRotate() && !Freecam.INSTANCE.isEnabled()) {
			super.onPostUpdate();
			if (isWithinDistance()) {
				mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
				if (naturalPlacement) {
					if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), position, face, new Vec3((float) face.getDirectionVec().getX() / 2F, (float) face.getDirectionVec().getY() / 2F, (float) face.getDirectionVec().getZ() / 2F))) {
						if (shouldResetBlock()) {
							reset();
						}
					}
				} else {
					mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(position, face.getIndex(), mc.thePlayer.getCurrentEquippedItem(), (float) face.getDirectionVec().getX() / 2F, (float) face.getDirectionVec().getY() / 2F, (float) face.getDirectionVec().getZ() / 2F));
				}
				timer.reset();
			}
		}
	}
	
	@Override
	public void onTaskCancelled() {
		reset();
	}
	
	@Override
	public void setRunning(boolean running) {
		super.setRunning(running);
		if (!running)
			reset();
	}

	/**
	 * @return True if the item held is required to continue block placement.
	 * */
	protected boolean hasRequiredItem(ItemStack item) {
		return item.getItem() instanceof ItemBlock;
	}

	/**
	 * @return True if the player should face the position.
	 * */
	protected boolean shouldRotate() {
		return mc.thePlayer.getHeldItem() != null && hasRequiredItem(mc.thePlayer.getHeldItem()) && isWithinDistance();
	}
	
	protected void reset() {
		setBlock(null, null);
		timer.reset();
	}
	
	protected IBlockState getBlockState() {
		return mc.theWorld.getBlockState(position);
	}
	
	public int getPlaceDelay() {
		return placeDelay;
	}

	public void setPlaceDelay(int placeDelay) {
		this.placeDelay = placeDelay;
	}

	/**
	 * @return True if the position is within the player's reach distance.
	 * */
	public boolean isWithinDistance() {
		return MathUtils.getDistance(position) < mc.playerController.getBlockReachDistance();
	}

	/**
	 * @return True if the block at our placement location is not air.
	 * */
	public boolean shouldResetBlock() {
		return mc.theWorld.getBlockState(position.offset(face)).getBlock().getMaterial() != Material.air;
	}
	
	public void cancelPlacing() {
		if (hasBlock()) {
			reset();
		}
	}
	
	public void setBlock(BlockPos position, EnumFacing face) {
		this.position = position;
		this.face = face;
		if (position != null && face != null)
			this.setRotations(position, face);
	}
	
	public boolean hasBlock() {
		return position != null && face != null;
	}

	public boolean isNaturalPlacement() {
		return naturalPlacement;
	}

	public void setNaturalPlacement(boolean naturalPlacement) {
		this.naturalPlacement = naturalPlacement;
	}
	
}
