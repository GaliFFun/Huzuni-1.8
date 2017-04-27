package net.halalaboos.mcwrapper.impl.mixin.network;

import net.halalaboos.mcwrapper.api.MCWrapper;
import net.halalaboos.mcwrapper.api.entity.living.player.Hand;
import net.halalaboos.mcwrapper.api.event.network.PacketSendEvent;
import net.halalaboos.mcwrapper.api.network.NetworkHandler;
import net.halalaboos.mcwrapper.api.network.PlayerInfo;
import net.halalaboos.mcwrapper.api.util.enums.DigAction;
import net.halalaboos.mcwrapper.api.util.enums.Face;
import net.halalaboos.mcwrapper.api.util.math.Vector3i;
import net.halalaboos.mcwrapper.impl.Convert;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements NetworkHandler {

	@Final
	@Shadow private NetworkManager netManager;

	/**
	 * @reason Publishes the {@link PacketSendEvent}.
	 *
	 * @author b
	 */
	@Overwrite
	public void addToSendQueue(Packet<?> packet) {
		if (packet == null) return;
		PacketSendEvent event = new PacketSendEvent(((net.halalaboos.mcwrapper.api.network.packet.Packet) packet));
		MCWrapper.getEventManager().publish(event);
		if (event.isCancelled()) return;
		netManager.sendPacket(packet);
	}

	@Shadow public abstract Collection<NetworkPlayerInfo> getPlayerInfoMap();

	@Shadow
	public abstract NetworkPlayerInfo getPlayerInfo(UUID uniqueId);

	@SuppressWarnings("unchecked")
	@Override
	public Collection<PlayerInfo> getPlayers() {
		return (Collection<PlayerInfo>)(Object) getPlayerInfoMap();
	}

	@Override
	public void sendRespawn() {
		addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
	}

	@Override
	public void sendUseSwing() {
		C02PacketUseEntity packetTryUse = new C02PacketUseEntity(); //todo test
		sendSwing(Hand.MAIN);
		addToSendQueue(packetTryUse);
	}

	@Override
	public Optional<PlayerInfo> getInfo(UUID uuid) {
		if (getPlayerInfo(uuid) == null) return Optional.empty();
		return Optional.of((PlayerInfo) getPlayerInfo(uuid));
	}

	@Override
	public void sendDigging(DigAction action, Vector3i pos, int face) {
		addToSendQueue(new C07PacketPlayerDigging(Convert.to(action), Convert.to(pos), EnumFacing.values()[face]));
	}

	@Override
	public void sendSwing(Hand hand) {
		addToSendQueue(new C0APacketAnimation());
	}

	@Override
	public void sendTryUseItemOnBlock(Vector3i pos, Face face, Hand hand, float faceX, float faceY, float faceZ) {
		addToSendQueue(new C08PacketPlayerBlockPlacement(Convert.to(pos), Convert.to(face).ordinal(), Convert.player().getHeldItem(), faceX, faceY, faceZ));
	}
}
