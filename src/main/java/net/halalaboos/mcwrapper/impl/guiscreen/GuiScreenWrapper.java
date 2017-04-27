package net.halalaboos.mcwrapper.impl.guiscreen;

import net.halalaboos.mcwrapper.api.client.gui.screen.Button;
import net.halalaboos.mcwrapper.api.client.gui.screen.Screen;
import net.halalaboos.mcwrapper.api.util.enums.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GuiScreenWrapper extends GuiScreen {

	private Screen screen;

	private GuiScreen parent;

	private Map<GuiButton, Button> buttonMap = new HashMap<>();

	private PanoramaRenderer panoramaRenderer;

	public GuiScreenWrapper(Screen screen, GuiScreen parent) {
		this.screen = screen;
		this.parent = parent;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (screen.isPanorama()) {
			panoramaRenderer = new PanoramaRenderer(width, height);

			panoramaRenderer.init();
		}
		for (Button button : screen.getButtons()) {
			this.buttonMap.put(new GuiButton(button.getId(), button.getX(), button.getY(), button.getWidth(), button.getHeight(),
					button.getText()), button);
		}
		buttonList.addAll(buttonMap.keySet());
		screen.width = width;
		screen.height = height;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for (GuiButton button : buttonMap.keySet()) {
			if (button.isMouseOver()) {
				buttonMap.get(button).getOnClick().apply(buttonMap.get(button), MouseButton.getMouseButton(mouseButton));
			}
		}
		screen.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onResize(Minecraft mcIn, int w, int h) {
		super.onResize(mcIn, w, h);
		screen.width = w;
		screen.height = h;
	}

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		screen.width = width;
		screen.height = height;
		screen.initGui();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(parent);
		}
		screen.keyTyped(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		screen.handleMouseInput();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (screen.shouldDrawBackground() && !screen.isPanorama()) {
			drawDefaultBackground();
		}
		if (panoramaRenderer != null && screen.isPanorama()) {
			panoramaRenderer.renderSkybox(partialTicks);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		screen.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		screen.onGuiClosed();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (panoramaRenderer != null && screen.isPanorama()) {
			panoramaRenderer.panoramaTick();
		}
		screen.updateScreen();
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		screen.mouseReleased(mouseX, mouseY, state);
	}

	public Screen getScreen() {
		return screen;
	}
}
