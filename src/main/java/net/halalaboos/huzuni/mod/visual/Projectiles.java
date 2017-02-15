package net.halalaboos.huzuni.mod.visual;

import net.halalaboos.huzuni.RenderManager.Renderer;
import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.halalaboos.huzuni.api.settings.Toggleable;
import net.halalaboos.huzuni.api.settings.Value;
import net.halalaboos.huzuni.api.util.render.GLManager;
import net.halalaboos.huzuni.api.util.render.RenderUtils;
import net.halalaboos.huzuni.mc.Reflection;
import net.halalaboos.mcwrapper.api.util.MathUtils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;

/**
 * Renders the trajectory of any throwable item held by the player along with the projectiles within the air.
 * */
public class Projectiles extends BasicMod implements Renderer {

	private final Tessellator tessellator = Tessellator.getInstance();
	
	private final Cylinder cylinder = new Cylinder();

	public final Toggleable lines = new Toggleable("Lines", "Render lines showing the projectile of "),
			landing = new Toggleable("Landing", "Render a landing position of each projectile"),
			arrows = new Toggleable("Arrows", "Show other arrows/items trajectories from other players");
	
	public final Value landingSize = new Value("Landing size", "", 0.5F, 0.5F, 2F, "Adjust the size of the landing pad");
	
	public Projectiles() {
		super("Projectiles", "Render a trajectory showing the path of a projectile");
		setAuthor("Halalaboos");
		setCategory(Category.VISUAL);
		addChildren(lines, landing, arrows, landingSize);
		lines.setEnabled(true);
		landing.setEnabled(true);
		arrows.setEnabled(true);
	}

	@Override
	public void onEnable() {
		huzuni.renderManager.addWorldRenderer(this);
	}
	
	@Override
	public void onDisable() {
		huzuni.renderManager.removeWorldRenderer(this);
	}
	
	@Override
	public void render(float partialTicks) {
		ItemStack item = getApplicableItem();
		if (item != null) {
			int mode = 0;
			float velocity;

			if (item.getItem() instanceof ItemBow)
                mode = 1;
            else if (isSplash(item.getItem()))
            	mode = 2;

			double posX = mc.getRenderManager().viewerPosX - (double) (MathUtils.cos(mc.thePlayer.rotationYaw / 180.0F * (float) Math.PI) * 0.16F),
	                posY = (mc.getRenderManager().viewerPosY + (double) mc.thePlayer.getEyeHeight()) - 0.10000000149011612D,
	                posZ = mc.getRenderManager().viewerPosZ - (double) (MathUtils.sin(mc.thePlayer.rotationYaw / 180.0F * (float) Math.PI) * 0.16F),
	                motionX = (double) (-MathUtils.sin(mc.thePlayer.rotationYaw / 180.0F * (float) Math.PI) * MathUtils.cos(mc.thePlayer.rotationPitch / 180.0F * (float) Math.PI)) * (mode == 1 ? 1.0 : 0.4),
	                motionY = (double) (-MathUtils.sin(mc.thePlayer.rotationPitch / 180.0F * (float) Math.PI)) * (mode == 1 ? 1.0 : 0.4),
	                motionZ = (double) (MathUtils.cos(mc.thePlayer.rotationYaw / 180.0F * (float) Math.PI) * MathUtils.cos(mc.thePlayer.rotationPitch / 180.0F * (float) Math.PI)) * (mode == 1 ? 1.0 : 0.4);
			if (mc.thePlayer.getItemInUseCount() <= 0 && mode == 1)
				velocity = 1F;
			else {
				int var6 = 72000 - mc.thePlayer.getItemInUseCount();
				float power = (float) var6 / 20.0F;
				power = (power * power + power * 2.0F) / 3.0F;
				if ((double) power < 0.1D)
					return;
				if (power > 1.0F)
					power = 1.0F;
				velocity = power;
			}
			renderProjectile(mode, velocity, posX, posY, posZ, motionX, motionY, motionZ, null);
		}
		if (arrows.isEnabled()) {
			for (Object o : mc.theWorld.loadedEntityList) {
				if (o instanceof EntityArrow) {
					EntityArrow entity = (EntityArrow) o;
					if (entity.isDead || Reflection.getInGround(entity))
						continue;
					renderProjectile(1, -1, entity.posX, entity.posY, entity.posZ, entity.motionX, entity.motionY, entity.motionZ, entity.shootingEntity != null ? entity.shootingEntity.getName() : null);
				}
			}
		}
	}

	private boolean isSplash(Item item) {
		return item instanceof ItemPotion && mc.thePlayer.getCurrentEquippedItem().getItemDamage() != 0;
	}
	
	private void renderProjectile(int mode, float velocity, double x, double y, double z, double motionX, double motionY, double motionZ, String text) {
		if (velocity != -1) {
			float theta = MathUtils.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
	        motionX /= (double) theta;
	        motionY /= (double) theta;
	        motionZ /= (double) theta;
	        motionX *= (mode == 1 ? (velocity * 2) : 1) * getMult(mode);
	        motionY *= (mode == 1 ? (velocity * 2) : 1) * getMult(mode);
	        motionZ *= (mode == 1 ? (velocity * 2) : 1) * getMult(mode);
		}
        boolean hasLanded = false, isEntity = false;
        MovingObjectPosition collision = null;
        float size = mode == 1 ? 0.3F : 0.25F;
        float gravity = getGravity(mode);
        
        if (text != null) {
			GlStateManager.pushMatrix();
			RenderUtils.prepareBillboarding((float) (x - mc.getRenderManager().viewerPosX), (float) (y - mc.getRenderManager().viewerPosY), (float) (z - mc.getRenderManager().viewerPosZ), true);
			GlStateManager.enableTexture2D();
			mc.fontRendererObj.drawStringWithShadow(text, -mc.fontRendererObj.getStringWidth(text) / 2, 1, 0xFFFFFFF);
			GlStateManager.disableTexture2D();
			GlStateManager.popMatrix();
        }
        GLManager.glColor(0F, 1F, 0F, 1F);
    	WorldRenderer renderer = tessellator.getWorldRenderer();
        if (lines.isEnabled())
    		renderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);
    	for (; !hasLanded && y > 0;) {
    		Vec3 present = new Vec3(x, y, z);
            Vec3 future = new Vec3(x + motionX, y + motionY, z + motionZ);
            MovingObjectPosition possibleCollision = mc.theWorld.rayTraceBlocks(present, future, false, true, false);
            if (possibleCollision != null) {
                hasLanded = true;
                collision = possibleCollision;
            }
            AxisAlignedBB boundingBox = new AxisAlignedBB(x - size, y - size, z - size, x + size, y + size, z + size);

            List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
            for (int index = 0; index < entities.size(); ++index) {
                Entity entity = entities.get(index);

                if (entity.canBeCollidedWith() && entity != mc.thePlayer) {
                    AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox().expand(0.3D, 0.3D, 0.3D);
                    MovingObjectPosition entityCollision = entityBoundingBox.calculateIntercept(present, future);
                    if (entityCollision != null) {
                        hasLanded = true;
                        isEntity = true;
                        collision = entityCollision;
                    }
                }
            }
            
	    	x += motionX;
            y += motionY;
            z += motionZ;
            float motionAdjustment = 0.99F;
            if (isInMaterial(boundingBox, Material.water))
                 motionAdjustment = 0.8F;

            motionX *= motionAdjustment;
            motionY *= motionAdjustment;
            motionZ *= motionAdjustment;
            motionY -= gravity;
            if (lines.isEnabled())
            	renderer.pos(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ).endVertex();
    	}
    	 if (lines.isEnabled())
    		 tessellator.draw();
    	if (landing.isEnabled()) {
	        GlStateManager.pushMatrix();
	        GlStateManager.translate(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ);
	        if (collision != null) {
	            switch (collision.sideHit.getIndex()) {
	                case 2:
	                	GlStateManager.rotate(90, 1, 0, 0);
	                    break;
	                case 3:
	                	GlStateManager.rotate(90, 1, 0, 0);
	                    break;
	                case 4:
	                	GlStateManager.rotate(90, 0, 0, 1);
	                    break;
	                case 5:
	                	GlStateManager.rotate(90, 0, 0, 1);
	                    break;
	                default:
	                    break;
	            }
	             if (isEntity)
	             	GLManager.glColor(1, 0, 0, 1F);
	        }
	        renderPoint();
	        GlStateManager.popMatrix();
    	}
	}

	private ItemStack getApplicableItem() {
		return mc.thePlayer.getHeldItem();
	}
	
	private boolean isThrowable(Item item) {
		return item instanceof ItemBow || item instanceof ItemSnowball || item instanceof ItemEnderPearl || item instanceof ItemEgg || isSplash(item);
	}
	
	private float getMult(int mode) {
		return mode == 2 ? 0.5F : 1.5F;
    }

    private float getGravity(int mode) {
    	return mode >= 1 ? 0.05F : 0.03F;
    }

	private void renderPoint() {
		Tessellator tessellator = Tessellator.getInstance();
    	WorldRenderer renderer = tessellator.getWorldRenderer();
    	renderer.begin(GL_LINES, DefaultVertexFormats.POSITION);
    	renderer.pos(-landingSize.getValue(), 0, 0).endVertex();
    	renderer.pos(0, 0, 0).endVertex();
    	renderer.pos(0, 0, -landingSize.getValue()).endVertex();
    	renderer.pos(0, 0, 0).endVertex();
    	
    	renderer.pos(landingSize.getValue(), 0, 0).endVertex();
    	renderer.pos(0, 0, 0).endVertex();
    	renderer.pos(0, 0, landingSize.getValue()).endVertex();
    	renderer.pos(0, 0, 0).endVertex();
    	tessellator.draw();

    	GlStateManager.rotate(-90, 1, 0, 0);
        cylinder.setDrawStyle(GLU.GLU_LINE);
        cylinder.draw(landingSize.getValue(), landingSize.getValue(), 0.1f, 24, 1);
    }
	
	private boolean isInMaterial(AxisAlignedBB axisalignedBB, Material material) {
        int chunkMinX = MathUtils.floor(axisalignedBB.minX);
        int chunkMaxX = MathUtils.floor(axisalignedBB.maxX + 1.0D);
        int chunkMinY = MathUtils.floor(axisalignedBB.minY);
        int chunkMaxY = MathUtils.floor(axisalignedBB.maxY + 1.0D);
        int chunkMinZ = MathUtils.floor(axisalignedBB.minZ);
        int chunkMaxZ = MathUtils.floor(axisalignedBB.maxZ + 1.0D);

		StructureBoundingBox structureBoundingBox = new StructureBoundingBox(chunkMinX, chunkMinY, chunkMinZ, chunkMaxX, chunkMaxY, chunkMaxZ);
        if (!mc.theWorld.isAreaLoaded(structureBoundingBox)) {
            return false;
        } else {
            boolean isWithin = false;
            for (int x = chunkMinX; x < chunkMaxX; ++x) {
                for (int y = chunkMinY; y < chunkMaxY; ++y) {
                    for (int z = chunkMinZ; z < chunkMaxZ; ++z) {
						IBlockState blockState = mc.theWorld.getBlockState(new BlockPos(x, y, z));
                        if (blockState.getBlock().getMaterial() == material) {
                            double liquidHeight = (double) ((float) (y + 1) - BlockLiquid.getLiquidHeightPercent((Integer)blockState.getValue(BlockLiquid.LEVEL)));
                            if ((double) chunkMaxY >= liquidHeight)
                                isWithin = true;
                        }
                    }
                }
            }
            return isWithin;
        }
    }
}