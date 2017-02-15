package net.halalaboos.huzuni.mod.visual;

import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.halalaboos.huzuni.api.settings.ItemSelector;
import net.halalaboos.huzuni.api.settings.Value;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

/**
 * Shows blocks hidden behind other blocks.
 * */
public class Xray extends BasicMod {
	
	public static final Xray INSTANCE = new Xray();
	
	public final ItemSelector<Block> blockList = new ItemSelector<>("Xray blocks", "Select blocks that you want enabled.");
	
	public final Value opacity = new Value("Opacity", "%", 0F, 30F, 100F, 1F, "Opacity blocks are rendered with.");

    private final int ignoredBlocks[] = {
			6, 31, 32, 37, 38, 83, 106, 111, 175
    };

    private float brightness = 0;

    private Xray() {
		super("Xray", "See stuff", Keyboard.KEY_X);
		this.setCategory(Category.VISUAL);
		setAuthor("Halalaboos");
		this.addChildren(blockList);
		blockList.addItem(new ItemStack(Items.water_bucket), Blocks.water, Blocks.flowing_water);
		blockList.addItem(new ItemStack(Items.lava_bucket), Blocks.lava, Blocks.flowing_lava);
		blockList.addItem(new ItemStack(Blocks.diamond_ore), Blocks.diamond_ore);
		blockList.addItem(new ItemStack(Blocks.emerald_ore), Blocks.emerald_ore);
		blockList.addItem(new ItemStack(Blocks.lapis_ore), Blocks.lapis_ore);
		blockList.addItem(new ItemStack(Blocks.gold_ore), Blocks.gold_ore);
		
		blockList.addItem(new ItemStack(Blocks.iron_ore), Blocks.iron_ore);
		blockList.addItem(new ItemStack(Blocks.redstone_ore), Blocks.redstone_ore, Blocks.lit_redstone_ore);
		blockList.addItem(new ItemStack(Blocks.coal_ore), Blocks.coal_ore);
		blockList.addItem(new ItemStack(Blocks.quartz_ore), Blocks.quartz_ore);
		blockList.addItem(new ItemStack(Blocks.diamond_block), Blocks.diamond_block);
		blockList.addItem(new ItemStack(Blocks.emerald_block), Blocks.emerald_block);
		
		blockList.addItem(new ItemStack(Blocks.lapis_block), Blocks.lapis_block);
		blockList.addItem(new ItemStack(Blocks.gold_block), Blocks.gold_block);
		blockList.addItem(new ItemStack(Blocks.iron_block), Blocks.iron_block);
		blockList.addItem(new ItemStack(Blocks.redstone_block), Blocks.redstone_block);
		blockList.addItem(new ItemStack(Blocks.coal_block), Blocks.coal_block);
		blockList.addItem(new ItemStack(Blocks.quartz_block), Blocks.quartz_block);

		blockList.addItem(new ItemStack(Blocks.obsidian), Blocks.obsidian);
		blockList.addItem(new ItemStack(Blocks.mossy_cobblestone), Blocks.mossy_cobblestone);
		blockList.addItem(new ItemStack(Blocks.mob_spawner), Blocks.mob_spawner);
		blockList.addItem(new ItemStack(Blocks.end_stone), Blocks.end_stone);
		blockList.addItem(new ItemStack(Blocks.nether_brick), Blocks.nether_brick);
		
		blockList.addItem(new ItemStack(Blocks.netherrack), Blocks.netherrack);
		blockList.addItem(new ItemStack(Blocks.soul_sand), Blocks.soul_sand);
		blockList.addItem(new ItemStack(Blocks.prismarine), Blocks.prismarine);
		blockList.addItem(new ItemStack(Blocks.slime_block), Blocks.slime_block);
		blockList.addItem(new ItemStack(Blocks.sponge), Blocks.sponge);
		
		blockList.addItem(new ItemStack(Blocks.bookshelf), Blocks.bookshelf);
		blockList.addItem(new ItemStack(Blocks.brick_block), Blocks.brick_block);
		blockList.addItem(new ItemStack(Blocks.cobblestone), Blocks.cobblestone);
		blockList.addItem(new ItemStack(Blocks.hay_block), Blocks.hay_block);
		blockList.addItem(new ItemStack(Blocks.pumpkin), Blocks.pumpkin, Blocks.lit_pumpkin);
		blockList.addItem(new ItemStack(Blocks.log), Blocks.log, Blocks.log2);
		
		blockList.addItem(new ItemStack(Blocks.snow), Blocks.snow);
		blockList.addItem(new ItemStack(Blocks.wool), Blocks.wool);
		// blockList.addItem(new ItemStack(Blocks.ICE), Blocks.ICE, Blocks.FROSTED_ICE, Blocks.PACKED_ICE);
		blockList.addItem(new ItemStack(Blocks.bedrock), Blocks.bedrock);
		blockList.addItem(new ItemStack(Blocks.clay), Blocks.clay);
		blockList.addItem(new ItemStack(Blocks.hardened_clay), Blocks.hardened_clay, Blocks.stained_hardened_clay);

		blockList.addItem(new ItemStack(Blocks.sand), Blocks.sand);
		blockList.addItem(new ItemStack(Blocks.gravel), Blocks.gravel);
		blockList.addItem(new ItemStack(Blocks.sandstone), Blocks.sandstone);
		blockList.addItem(new ItemStack(Blocks.red_sandstone), Blocks.red_sandstone);
		// blockList.addItem(new ItemStack(Blocks.LEAVES), Blocks.LEAVES, Blocks.LEAVES2);
		// blockList.addItem(new ItemStack(Blocks.GLASS), Blocks.GLASS);
		this.settings.setDisplayable(false);
	}
	
	@Override
	protected void onEnable() {
		brightness = mc.gameSettings.gammaSetting;
		mc.gameSettings.gammaSetting = 1000F;
	}
	

	@Override
	protected void onDisable() {
		mc.gameSettings.gammaSetting = brightness;
	}

	private void fastReload() {
    	if (mc.theWorld != null) {
    		int x = (int) mc.thePlayer.posX;
    		int z = (int) mc.thePlayer.posZ;
    		int viewDistance = 16 * mc.gameSettings.renderDistanceChunks;
			mc.theWorld.markBlockRangeForRenderUpdate(x - viewDistance, 0, z - viewDistance, x + viewDistance, 256, z + viewDistance);
		}
	}
	
	@Override
	public void toggle() {
		super.toggle();
		fastReload();
	}
	
	public boolean isEnabled(Block block) {
		return blockList.isEnabledObject(block);
	}

	public boolean shouldIgnore(Block block) {
		return Arrays.binarySearch(ignoredBlocks, Block.getIdFromBlock(block)) >= 0;
	}

	public int getOpacity() {
		return (int) ((opacity.getValue() / 100F) * 255F);
	}
	
	public boolean hasOpacity() {
		return getOpacity() > 20;
	}
}
