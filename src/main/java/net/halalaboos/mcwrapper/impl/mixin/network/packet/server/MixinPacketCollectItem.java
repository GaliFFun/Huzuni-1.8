package net.halalaboos.mcwrapper.impl.mixin.network.packet.server;

import net.halalaboos.mcwrapper.api.network.packet.server.ItemPickupPacket;
import net.halalaboos.mcwrapper.impl.mixin.network.packet.MixinPacket;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(S0DPacketCollectItem.class)
public abstract class MixinPacketCollectItem implements MixinPacket, ItemPickupPacket {

	@Shadow public abstract int getCollectedItemEntityID();
	@Shadow public abstract int getEntityID();

	@Override
	public int getCollector() {
		return getEntityID();
	}

	//todo fix
	@Override
	public int getQuantity() {
		return 1;
	}

	@Override
	public int getCollected() {
		return getCollectedItemEntityID();
	}
}
