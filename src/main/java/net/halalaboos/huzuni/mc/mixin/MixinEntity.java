package net.halalaboos.huzuni.mc.mixin;

import net.halalaboos.huzuni.mod.movement.Freecam;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.entity.Entity.class) public abstract class MixinEntity {

	@Shadow
	public boolean noClip;

	@Inject(method = "moveEntity", at = @At("HEAD"), cancellable = true)
	public void move(double x, double y, double z, CallbackInfo ci) {
		if (Minecraft.getMinecraft().thePlayer != null) {
			Minecraft.getMinecraft().thePlayer.noClip = Freecam.INSTANCE.isEnabled() ||
					Minecraft.getMinecraft().thePlayer.isSpectator();
		}
	}
}
