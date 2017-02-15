package net.halalaboos.huzuni.mod.movement;

import com.mojang.authlib.GameProfile;
import net.halalaboos.huzuni.api.event.EventManager.EventMethod;
import net.halalaboos.huzuni.api.event.PacketEvent;
import net.halalaboos.huzuni.api.event.PlayerMoveEvent;
import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.halalaboos.huzuni.api.settings.Value;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import org.lwjgl.input.Keyboard;

/**
 * Allows the player to fly freely from their body and explore the world.
 * */
public class Freecam extends BasicMod {
	
	public static final Freecam INSTANCE = new Freecam();
	
	public final Value speed = new Value("Speed", "", 0.1F, 1F, 10F, "movement speed");

	private boolean oldFlying = false;
    private EntityOtherPlayerMP fakePlayer;
	
	private Freecam() {
		super("Freecam", "Allows an individual to fly FROM THEIR BODY?", Keyboard.KEY_U);
		this.setCategory(Category.MOVEMENT);
		setAuthor("Halalaboos");
		addChildren(speed);
	}
	
	@Override
	public void toggle() {
		super.toggle();
		if (mc.thePlayer != null && mc.theWorld != null) {
	        if (isEnabled()) {
	        	oldFlying = Flight.INSTANCE.isEnabled();
	            fakePlayer = new EntityOtherPlayerMP(mc.theWorld, new GameProfile(mc.thePlayer.getUniqueID(), mc.thePlayer.getName()));
	            fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer);
				fakePlayer.inventory = mc.thePlayer.inventory;
	            fakePlayer.setPositionAndRotation(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
	            fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead;
				mc.theWorld.addEntityToWorld(-69, fakePlayer);
				Flight.INSTANCE.setEnabled(true);
			 } else {
	        	if (fakePlayer != null && mc.thePlayer != null) {
	        		mc.thePlayer.setPositionAndRotation(fakePlayer.posX, fakePlayer.posY, fakePlayer.posZ, fakePlayer.rotationYaw, fakePlayer.rotationPitch);
	            	mc.theWorld.removeEntityFromWorld(-69);
					Flight.INSTANCE.setEnabled(oldFlying);
	        	}
	        	 if (mc.thePlayer != null)
					 Flight.INSTANCE.setEnabled(oldFlying);
	        }
		}
    }
	
	@Override
	public void onEnable() {
		huzuni.eventManager.addListener(this);
	}
	
	@Override
	public void onDisable() {
		huzuni.eventManager.removeListener(this);
	}

	@EventMethod
	public void onPlayerMove(PlayerMoveEvent event) {
		mc.thePlayer.setSprinting(false);
		Flight.INSTANCE.setEnabled(true);
		if (fakePlayer != null)
			fakePlayer.setHealth(mc.thePlayer.getHealth());
		event.setMotionX(event.getMotionX() * speed.getValue());
		event.setMotionY(event.getMotionY() * speed.getValue());
		event.setMotionZ(event.getMotionZ() * speed.getValue());
	}
}
