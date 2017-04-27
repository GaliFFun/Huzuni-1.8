package net.halalaboos.mcwrapper.impl.mixin.network.packet.server;

import net.halalaboos.mcwrapper.api.network.packet.server.SpawnObjectPacket;
import net.halalaboos.mcwrapper.impl.mixin.network.packet.MixinPacket;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(S0EPacketSpawnObject.class)
public abstract class MixinPacketSpawnObject implements MixinPacket, SpawnObjectPacket {

	@Shadow public abstract int getEntityID();

	@Shadow private int field_149020_k; //todo check

	@Override
	public int getSpawnedId() {
		return getEntityID();
	}

	@Override
	public int getSourceId() {
		return field_149020_k;
	}
}
