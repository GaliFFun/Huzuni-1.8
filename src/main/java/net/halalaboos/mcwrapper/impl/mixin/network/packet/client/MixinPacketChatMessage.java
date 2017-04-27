package net.halalaboos.mcwrapper.impl.mixin.network.packet.client;

import net.halalaboos.mcwrapper.api.network.packet.client.ChatMessagePacket;
import net.halalaboos.mcwrapper.impl.mixin.network.packet.MixinPacket;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(C01PacketChatMessage.class)
public abstract class MixinPacketChatMessage implements MixinPacket, ChatMessagePacket {

	@Shadow private String message;

	@Override
	public String getText() {
		return message;
	}

	@Override
	public void setText(String text) {
		this.message = text;
	}
}
