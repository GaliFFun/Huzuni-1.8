package net.halalaboos.huzuni.gui.widgets;

import net.halalaboos.huzuni.api.gui.WidgetManager;
import net.halalaboos.mcwrapper.api.util.MathUtils;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class BiomeWidget extends BackgroundWidget {

	public BiomeWidget(WidgetManager menuManager) {
		super("Biome", "Renders the biome you are currently inside", menuManager);
	}

	@Override
	public void renderMenu(int x, int y, int width, int height) {
		super.renderMenu(x, y, width, height);
        Chunk currentChunk = mc.theWorld.getChunkFromBlockCoords(new BlockPos(MathUtils.floor(mc.thePlayer.posX), ((int) mc.thePlayer.posY), MathUtils.floor(mc.thePlayer.posZ)));
        String biome = "Biome: " + currentChunk.getBiome(new BlockPos(MathUtils.floor(mc.thePlayer.posX) & 15, mc.thePlayer.posY, MathUtils.floor(mc.thePlayer.posZ) & 15), mc.theWorld.getWorldChunkManager()).biomeName;
		theme.drawStringWithShadow(biome, x, y, 0xFFFFFF);
		this.setWidth(theme.getStringWidth(biome) + 2);
		this.setHeight(theme.getStringHeight(biome));
	}
}
