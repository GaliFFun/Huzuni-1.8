package net.halalaboos.huzuni.mod.combat;

import net.halalaboos.huzuni.api.event.EventManager.EventMethod;
import net.halalaboos.huzuni.api.event.PacketEvent;
import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.minecraft.network.play.client.C02PacketUseEntity;

/**
 * Attempts to force criticals by jumping.
 * */
public class Criticals extends BasicMod {
	
	public Criticals() {
		super("Criticals", "Automagically critical with each hit");
		this.setCategory(Category.COMBAT);
		setAuthor("brudin");
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
		if (event.type == PacketEvent.Type.SENT) {
			if (event.getPacket() instanceof C02PacketUseEntity) {
				C02PacketUseEntity packetUseEntity = (C02PacketUseEntity)event.getPacket();
				if (packetUseEntity.getAction() == C02PacketUseEntity.Action.ATTACK) {
					if (shouldCritical()) {
						doCrit();
					}
				}
			}
		}
	}
	
	private void doCrit() {
		boolean preGround = mc.thePlayer.onGround;
		mc.thePlayer.onGround = false;
		mc.thePlayer.jump();
		mc.thePlayer.onGround = preGround;
	}
	
	private boolean shouldCritical() {
		return !mc.thePlayer.isInWater() && mc.thePlayer.onGround && !mc.thePlayer.isOnLadder();
	}
	
}
