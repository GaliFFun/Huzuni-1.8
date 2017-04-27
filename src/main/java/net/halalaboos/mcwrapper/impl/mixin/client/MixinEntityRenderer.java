package net.halalaboos.mcwrapper.impl.mixin.client;

import net.halalaboos.huzuni.Huzuni;
import net.halalaboos.mcwrapper.api.MCWrapper;
import net.halalaboos.mcwrapper.api.client.ClientEffects;
import net.halalaboos.mcwrapper.api.event.render.HUDRenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

	@Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
	public void dispatchHUDEvent(float partialTicks, long nano, CallbackInfo ci) {
		MCWrapper.getEventManager().publish(new HUDRenderEvent(Minecraft.getMinecraft().gameSettings.showDebugInfo, partialTicks));
	}

	@Shadow
	private Minecraft mc;

	@Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
	public boolean modifyBlindness(EntityLivingBase entityLivingBase, Potion potionIn) {
		if (potionIn == Potion.blindness) {
			return ClientEffects.BLINDNESS.isEnabled() && entityLivingBase.isPotionActive(Potion.blindness);
		}
		return entityLivingBase.isPotionActive(potionIn);
	}

	@Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderOverlays(F)V"))
	public void modifyOverlays(ItemRenderer itemRenderer, float partialTicks) {
		if (ClientEffects.OVERLAY.isEnabled()) {
			itemRenderer.renderOverlays(partialTicks);
		}
	}

	@Inject(method = "renderWorldPass", at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z",
			shift = At.Shift.BEFORE))
	private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
		Huzuni.INSTANCE.renderManager.enableGlConstants();
		Huzuni.INSTANCE.renderManager.renderWorld(partialTicks);
		dispatchLineRenderer(partialTicks, pass);
		Huzuni.INSTANCE.renderManager.disableGlConstants();
	}

	@Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
	private void modifyHurtcam(float partialTicks, CallbackInfo ci) {
		if (!ClientEffects.HURTCAM.isEnabled()) {
			ci.cancel();
		}
	}

	@Shadow
	private void setupCameraTransform(float partialTicks, int pass) {}

	private void dispatchLineRenderer(float partialTicks, int pass) {
		boolean oldBobbing = mc.gameSettings.viewBobbing;
		mc.gameSettings.viewBobbing = false;
		glPushMatrix();
		setupCameraTransform(partialTicks, pass);
		Huzuni.INSTANCE.renderManager.renderLines();
		mc.gameSettings.viewBobbing = oldBobbing;
		glPopMatrix();
	}

	@Shadow private int[] lightmapColors;
	@Shadow private boolean lightmapUpdateNeeded;

	@Shadow protected abstract void renderRainSnow(float partialTicks);

	@Inject(method = "updateLightmap",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;updateDynamicTexture()V"))
	public void updateLightmap(float partialTicks, CallbackInfo ci) {
		if (Huzuni.INSTANCE.settings.monochromeLighting.isEnabled()) {
			for (int i = 0; i < lightmapColors.length; i++) {
				int color = lightmapColors[i];
				int avg = ((color >> 16 & 0xFF) + (color >> 8 & 0xFF) + (color & 0xFF)) / 3;
				color = 0xFF000000 | (avg | avg << 8 | avg << 16 | avg << 24);
				lightmapColors[i] = color;
			}
		}
	}
}
