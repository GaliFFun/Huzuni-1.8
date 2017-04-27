package net.halalaboos.mcwrapper.impl.mixin.network.packet.client;

import net.halalaboos.mcwrapper.api.network.packet.client.DiggingPacket;
import net.halalaboos.mcwrapper.api.util.enums.DigAction;
import net.halalaboos.mcwrapper.api.util.enums.Face;
import net.halalaboos.mcwrapper.api.util.math.Vector3i;
import net.halalaboos.mcwrapper.impl.Convert;
import net.halalaboos.mcwrapper.impl.mixin.network.packet.MixinPacket;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(C07PacketPlayerDigging.class)
@Implements(@Interface(iface = DiggingPacket.class, prefix = "api$"))
public abstract class MixinPacketDigging implements MixinPacket, DiggingPacket {
	@Shadow public abstract BlockPos getPosition();

	@Shadow public abstract C07PacketPlayerDigging.Action getStatus();

	@Shadow public abstract EnumFacing getFacing();

	@Override
	public Vector3i getLocation() {
		return Convert.from(getPosition());
	}

	@Override
	public DigAction getDigAction() {
		return Convert.from(getStatus());
	}

	@Override
	public Face getFace() {
		return Convert.from(getFacing());
	}
}
