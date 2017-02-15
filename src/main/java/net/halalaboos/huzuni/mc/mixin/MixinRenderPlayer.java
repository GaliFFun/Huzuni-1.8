package net.halalaboos.huzuni.mc.mixin;

import net.halalaboos.huzuni.mod.visual.Nametags;
import net.minecraft.client.entity.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.entity.RenderPlayer.class) public class MixinRenderPlayer {

	@Inject(method = "renderOffsetLivingLabel", at = @At("HEAD"), cancellable = true)
	protected void renderEntityName(AbstractClientPlayer entityIn, double x, double y, double z, String str,
									float p_177069_9_, double p_177069_10_,
									CallbackInfo ci) {
		if (Nametags.INSTANCE.isEnabled()) {
			ci.cancel();
		}
	}
}
