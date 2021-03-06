package net.halalaboos.huzuni.gui.widgets;

import net.halalaboos.huzuni.WaypointManager.Waypoint;
import net.halalaboos.huzuni.api.gui.WidgetManager;
import net.halalaboos.huzuni.api.gui.widget.Widget;
import net.halalaboos.huzuni.api.node.impl.Toggleable;
import net.halalaboos.huzuni.api.node.impl.Value;
import net.halalaboos.huzuni.api.util.gl.GLUtils;
import net.halalaboos.huzuni.api.util.gl.Texture;
import net.halalaboos.mcwrapper.api.MCWrapper;
import net.halalaboos.mcwrapper.api.client.ClientPlayer;
import net.halalaboos.mcwrapper.api.util.math.MathUtils;
import org.lwjgl.opengl.GL11;

/**
 * Widget designed to appear like an old-school compass.
 * */
public class CompassWidget extends Widget {

	private final Texture location = new Texture("location.png");
	
	private final Toggleable intercardinal = new Toggleable("Intercardinal", "Render the intercardinal points onto the compass");
	
	private final Toggleable waypoints = new Toggleable("Waypoints", "Render the waypoints");

	private final Value width = new Value("Width", "", 40, 90, 100, 1F, "Size of the compass");
	
	public CompassWidget(WidgetManager menuManager) {
		super("Compass", "Old-school compass", menuManager);
		this.addChildren(intercardinal, width); // waypoints
		this.setWidth((int) width.getValue() * 2);
		this.setHeight(11);
	}

	@Override
	public void renderMenu(int x, int y, int width, int height) {
		ClientPlayer player = MCWrapper.getPlayer();
		float yaw = player.getRotation().yaw;
		this.setWidth((int) this.width.getValue() * 2);
		this.setHeight(11);
		theme.drawBorder(x, y, width, height, false);
		theme.drawBackgroundRect(x, y, width, height, false);
		int rotation = (int) MathUtils.wrapDegrees(yaw);
		
		int offsetX = (int) this.width.getValue();
		GLUtils.glScissor(x + 1, y + 1, x + width - 1, y + height - 1);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		theme.drawString("N", (x - rotation + offsetX) + 180 - theme.getStringWidth("N") / 2, y, 0xFFFFFF);
		theme.drawString("E", (x - rotation + offsetX) - 90 - theme.getStringWidth("E") / 2, y, 0xFFFFFF);
		theme.drawString("S", (x - rotation + offsetX) - theme.getStringWidth("S") / 2, y, 0xFFFFFF);
		theme.drawString("W", (x - rotation + offsetX) + 90 - theme.getStringWidth("W") / 2, y, 0xFFFFFF);
		theme.drawString("N", (x - rotation + offsetX) - 180 - theme.getStringWidth("N") / 2, y, 0xFFFFFF);
		theme.drawLine(x + offsetX, y, x + offsetX, y + height, true);
		if (intercardinal.isEnabled()) {
			theme.drawLine((x - rotation + offsetX) + 45, y + 2, (x - rotation + offsetX) + 45, y + height - 2, true);
			theme.drawLine((x - rotation + offsetX) - 45, y + 2, (x - rotation + offsetX) - 45, y + height - 2, true);
			theme.drawLine((x - rotation + offsetX) + 135, y + 2, (x - rotation + offsetX) + 135, y + height - 2, true);
			theme.drawLine((x - rotation + offsetX) - 135, y + 2, (x - rotation + offsetX) - 135, y + height - 2, true);
			theme.drawLine((x - rotation + offsetX) - 225, y + 2, (x - rotation + offsetX) - 225, y + height - 2, true);
			theme.drawLine((x - rotation + offsetX) + 225, y + 2, (x - rotation + offsetX) + 225, y + height - 2, true);
		}
		if (waypoints.isEnabled()) {
			for (Waypoint waypoint : huzuni.waypointManager.getLocalWaypoints()) {
				float waypointYaw = MathUtils.wrapDegrees((float) (Math.atan2(waypoint.getPosition().getZ() - player.getZ(), waypoint.getPosition().getX() - player.getX()) * 180.0D / Math.PI) - 90.0F);
				GLUtils.glColor(waypoint.getColor());
				location.render(x - rotation + offsetX + waypointYaw - 5, y, 10, 10);
			}
		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

}
