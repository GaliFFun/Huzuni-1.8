package net.halalaboos.mcwrapper.impl.mixin.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.halalaboos.mcwrapper.api.MCWrapper;
import net.halalaboos.mcwrapper.api.event.network.PacketReadEvent;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

	@Shadow private Channel channel;
	@Shadow private INetHandler packetListener;

	/**
	 * @author
	 */
	@Overwrite
	protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet p_channelRead0_2_) throws Exception {
		if (channel.isOpen()) {
			try {
				PacketReadEvent event = new PacketReadEvent((net.halalaboos.mcwrapper.api.network.packet.Packet)p_channelRead0_2_);
				MCWrapper.getEventManager().publish(event);
				if (event.isCancelled()) return;
				p_channelRead0_2_.processPacket(this.packetListener);
			} catch (ThreadQuickExitException ignored) {}
		}
	}
}
