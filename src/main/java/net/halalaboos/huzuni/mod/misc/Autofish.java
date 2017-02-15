package net.halalaboos.huzuni.mod.misc;

import net.halalaboos.huzuni.api.event.EventManager.EventMethod;
import net.halalaboos.huzuni.api.event.PacketEvent;
import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

/**
 * Attempts to recast rods when fishing when a bob from a fish was found.
 * */
public class Autofish extends BasicMod {
	
	private int idFish;
	
	public Autofish() {
		super("Auto fish", "Automagically recasts and pulls fish");
		setAuthor("brudin");
		this.setCategory(Category.MISC);
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
	public void onPacket(PacketEvent event) {
		if (event.type == PacketEvent.Type.READ) {
			if (event.getPacket() instanceof S0EPacketSpawnObject) {
				S0EPacketSpawnObject packet = (S0EPacketSpawnObject)event.getPacket();
				if (packet.getEntityID() == 90 && packet.func_149009_m() == mc.thePlayer.getEntityId()) {
					idFish = packet.getEntityID();
				}
			} else if (event.getPacket() instanceof S12PacketEntityVelocity) {
				S12PacketEntityVelocity packetEntityVelocity = (S12PacketEntityVelocity)event.getPacket();
				Entity packetEntity = mc.theWorld.getEntityByID(packetEntityVelocity.getEntityID());
				if (packetEntity != null && packetEntity instanceof EntityFishHook) {
					EntityFishHook fish = (EntityFishHook)packetEntity;
					if (fish.motionX == 0 && fish.motionY < -0.02 && fish.motionZ == 0) {
						recastRod();
						idFish = -420;
					}
				}
			}
		}
	}

	/**
     * Recasts the rod.
     * */
	private void recastRod() {
		C0APacketAnimation packetAnimation = new C0APacketAnimation();
		C08PacketPlayerBlockPlacement packetTryUse = new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem());

		mc.getNetHandler().addToSendQueue(packetAnimation);
		mc.getNetHandler().addToSendQueue(packetTryUse);

		mc.getNetHandler().addToSendQueue(packetAnimation);
		mc.getNetHandler().addToSendQueue(packetTryUse);
	}


}
