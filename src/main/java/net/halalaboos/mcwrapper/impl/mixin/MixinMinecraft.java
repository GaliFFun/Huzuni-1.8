package net.halalaboos.mcwrapper.impl.mixin;

import net.halalaboos.huzuni.Huzuni;
import net.halalaboos.mcwrapper.api.MCWrapper;
import net.halalaboos.mcwrapper.api.MCWrapperHooks;
import net.halalaboos.mcwrapper.api.MinecraftClient;
import net.halalaboos.mcwrapper.api.client.ClientPlayer;
import net.halalaboos.mcwrapper.api.client.Controller;
import net.halalaboos.mcwrapper.api.client.Session;
import net.halalaboos.mcwrapper.api.client.gui.TextRenderer;
import net.halalaboos.mcwrapper.api.client.gui.screen.Screen;
import net.halalaboos.mcwrapper.api.entity.Entity;
import net.halalaboos.mcwrapper.api.network.NetworkHandler;
import net.halalaboos.mcwrapper.api.network.ServerInfo;
import net.halalaboos.mcwrapper.api.util.Resolution;
import net.halalaboos.mcwrapper.api.util.ResourcePath;
import net.halalaboos.mcwrapper.api.util.enums.Face;
import net.halalaboos.mcwrapper.api.util.math.Result;
import net.halalaboos.mcwrapper.api.util.math.Vector3d;
import net.halalaboos.mcwrapper.api.util.math.Vector3i;
import net.halalaboos.mcwrapper.api.world.World;
import net.halalaboos.mcwrapper.impl.Convert;
import net.halalaboos.mcwrapper.impl.MinecraftAdapterImpl;
import net.halalaboos.mcwrapper.impl.guiscreen.GuiScreenWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.Optional;

@Mixin(net.minecraft.client.Minecraft.class)
@Implements(@Interface(iface = MinecraftClient.class, prefix = "api$"))
public abstract class MixinMinecraft implements MinecraftClient {

	@Shadow private int rightClickDelayTimer;
	@Shadow private Timer timer;
	@Shadow public EntityPlayerSP thePlayer;
	@Shadow public WorldClient theWorld;
	@Shadow public int displayWidth;
	@Shadow public int displayHeight;
	@Shadow public GameSettings gameSettings;
	@Shadow public abstract boolean isSingleplayer();
	@Shadow @Nullable public abstract ServerData getCurrentServerData();
	@Shadow public GuiIngame ingameGUI;
	@Shadow private static int debugFPS;
	@Shadow public PlayerControllerMP playerController;
	@Shadow private RenderManager renderManager;
	@Shadow public FontRenderer fontRendererObj;
	@Shadow public abstract boolean isUnicode();
	@Shadow @Nullable public abstract NetHandlerPlayClient getNetHandler();
	@Shadow public abstract IResourceManager getResourceManager();
	@Shadow public abstract void displayGuiScreen(@Nullable GuiScreen guiScreenIn);
	@Shadow @Final public File mcDataDir;
	@Shadow private net.minecraft.entity.Entity renderViewEntity;
	@Shadow public RenderGlobal renderGlobal;

	@Shadow public MovingObjectPosition objectMouseOver;

	@Shadow @Final private Proxy proxy;

	@Shadow public GuiScreen currentScreen;

	@Shadow public abstract TextureManager getTextureManager();

	@Shadow @Final private net.minecraft.util.Session session;

	@Shadow public boolean inGameHasFocus;

	@Inject(method = "startGame", at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/Minecraft;ingameGUI:Lnet/minecraft/client/gui/GuiIngame;",
			shift = At.Shift.AFTER))
	public void initWrapper(CallbackInfo ci) {
		MCWrapper.setAdapter(new MinecraftAdapterImpl((Minecraft)(Object)this));
		Huzuni.INSTANCE.start();
	}

	@Inject(method = "runTick()V", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/lwjgl/input/Keyboard;next()Z", shift = At.Shift.AFTER))
	public void onKeyPress(CallbackInfo ci) {
		if (Keyboard.getEventKeyState()) {
			int keyCode = Keyboard.getEventKey();
			MCWrapperHooks.keyTyped(keyCode);
		}
	}

	@Inject(method = "runTick()V", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/lwjgl/input/Mouse;getEventButton()I"))
	public void onMouseClick(CallbackInfo ci) {
		if (Mouse.getEventButtonState()) {
			MCWrapperHooks.mouseClicked(Mouse.getEventButton());
		}
	}

	@Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
	public void onLoadWorld(@Nullable WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
		if (worldClientIn != null) {
			MCWrapperHooks.joinWorld((World)worldClientIn);
		}
	}

	@Override
	public int getRightClickDelayTimer() {
		return rightClickDelayTimer;
	}

	@Override
	public void setRightClickDelayTimer(int rightClickDelayTimer) {
		this.rightClickDelayTimer = rightClickDelayTimer;
	}

	@Override
	public float getDelta() {
		return timer.renderPartialTicks;
	}

	@Override
	public float getTimerSpeed() {
		return timer.timerSpeed;
	}

	@Override
	public void setTimerSpeed(float timerSpeed) {
		timer.timerSpeed = timerSpeed;
	}

	@Override
	public ClientPlayer getPlayer() {
		return ((ClientPlayer) thePlayer);
	}

	@Override
	public World getWorld() {
		return ((World) theWorld);
	}

	@Override
	public boolean isRemote() {
		return !isSingleplayer();
	}

	@Override
	public Resolution getScreenResolution() {
		return new Resolution(displayWidth, displayHeight, gameSettings.guiScale);
	}

	@Override
	public Optional<ServerInfo> getServerInfo() {
		if (getCurrentServerData() == null) {
			return Optional.empty();
		}
		return Optional.of((ServerInfo)getCurrentServerData());
	}

	@Override
	public void clearMessages(boolean sentMessages) {
		ingameGUI.getChatGUI().clearChatMessages();
	}

	@Override
	public int getFPS() {
		return debugFPS;
	}

	@Override
	public Controller getController() {
		return ((Controller) playerController);
	}

	@Override
	public Vector3d getCamera() {
		return new Vector3d(renderManager.viewerPosX, renderManager.viewerPosY, renderManager.viewerPosZ);
	}

	@Override
	public TextRenderer getTextRenderer() {
		return ((TextRenderer) fontRendererObj);
	}

	@Override
	public boolean useUnicode() {
		return isUnicode();
	}

	@Override
	public NetworkHandler getNetworkHandler() {
		return (NetworkHandler) getNetHandler();
	}

	@Override
	public InputStream getInputStream(ResourcePath path) throws IOException {
		return getResourceManager().getResource(new ResourceLocation(path.toString())).getInputStream();
	}

	@Override
	public void showScreen(Screen screen) {
		displayGuiScreen(new GuiScreenWrapper(screen, currentScreen));
	}

	@Override
	public File getSaveDirectory() {
		return mcDataDir;
	}

	@Override
	public boolean shouldShowGui() {
		return Minecraft.isGuiEnabled();
	}

	public Entity getViewEntity() {
		return (Entity)renderViewEntity;
	}

	@Override
	public void printMessage(String message) {
		ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(message));
	}

	@Override
	public void loadRenderers() {
		renderGlobal.loadRenderers();
	}

	@Override
	public net.halalaboos.mcwrapper.api.client.GameSettings getSettings() {
		return (net.halalaboos.mcwrapper.api.client.GameSettings)gameSettings;
	}

	@Override
	public Vector3i getMouseVector() {
		return Convert.from(objectMouseOver.getBlockPos());
	}

	@Override
	public Face getMouseFace() {
		return Convert.from(objectMouseOver.sideHit);
	}

	@Override
	public Optional<Result> getMouseResult() {
		if (objectMouseOver == null) {
			return Optional.empty();
		}
		return Optional.of(Convert.from(objectMouseOver));
	}

	public Optional<Entity> getMousedEntity() {
		if (objectMouseOver.entityHit == null) return Optional.empty();
		return Optional.of((Entity)objectMouseOver.entityHit);
	}

	@Intrinsic
	public Proxy api$getProxy() {
		return proxy;
	}

	@Override
	public Vector3d getPlayerView() {
		return new Vector3d(renderManager.playerViewX, renderManager.playerViewY, 0);
	}

	@Override
	public boolean isScreenOpen() {
		return currentScreen != null;
	}

	@Override
	public Object getScreen() {
		if (currentScreen instanceof GuiScreenWrapper) {
			return ((GuiScreenWrapper) currentScreen).getScreen();
		}
		return currentScreen;
	}

	@Override
	public void bindTexture(ResourcePath path) {
		getTextureManager().bindTexture(Convert.to(path));
	}

	@Override
	public Session session() {
		return (Session)this.session;
	}
}
