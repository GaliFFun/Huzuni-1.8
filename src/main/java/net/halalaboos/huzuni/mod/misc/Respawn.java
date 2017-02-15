package net.halalaboos.huzuni.mod.misc;

import net.halalaboos.huzuni.api.event.EventManager.EventMethod;
import net.halalaboos.huzuni.api.event.PacketEvent;
import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S06PacketUpdateHealth;

/**
 * Respawns the player once their health has reached below 0.
 * */
public class Respawn extends BasicMod {
	
	public Respawn() {
		super("Respawn", "Automagically respawns once you're sent to the respawn screen");
		this.setCategory(Category.MISC);
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
		if (event.type == PacketEvent.Type.READ) {
			if (event.getPacket() instanceof S06PacketUpdateHealth) {
				S06PacketUpdateHealth packet = (S06PacketUpdateHealth)event.getPacket();
				if (packet.getHealth() > 0.0F)
					return;
				mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
			}
		}
	}

}
