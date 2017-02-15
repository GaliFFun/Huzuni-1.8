package net.halalaboos.huzuni.mc;

import com.google.common.collect.Lists;
import net.halalaboos.huzuni.Huzuni;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;

public class GuiHuzuniChat extends GuiNewChat
{
	private static final Logger logger = LogManager.getLogger();
	private final Minecraft mc;
	private final Huzuni huzuni = Huzuni.INSTANCE;
	private final List<String> sentMessages = Lists.<String>newArrayList();
	private final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
	private final List<ChatLine> field_146253_i = Lists.<ChatLine>newArrayList();
	private int scrollPos;
	private boolean isScrolled;

	public GuiHuzuniChat(Minecraft mcIn)
	{
		super(mcIn);
		this.mc = mcIn;
	}

	public void drawChat(int p_146230_1_)
	{
		if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
		{
			int i = this.getLineCount();
			boolean flag = false;
			int j = 0;
			int k = this.field_146253_i.size();
			float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

			if (k > 0)
			{
				if (this.getChatOpen())
				{
					flag = true;
				}

				float f1 = this.getChatScale();
				int l = MathHelper.ceiling_float_int((float)this.getChatWidth() / f1);
				GlStateManager.pushMatrix();
				GlStateManager.translate(2.0F, 20.0F, 0.0F);
				GlStateManager.scale(f1, f1, 1.0F);

				for (int i1 = 0; i1 + this.scrollPos < this.field_146253_i.size() && i1 < i; ++i1)
				{
					ChatLine chatline = (ChatLine)this.field_146253_i.get(i1 + this.scrollPos);

					if (chatline != null)
					{
						int j1 = p_146230_1_ - chatline.getUpdatedCounter();

						if (j1 < 200 || flag)
						{
							double d0 = (double)j1 / 200.0D;
							d0 = 1.0D - d0;
							d0 = d0 * 10.0D;
							d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
							d0 = d0 * d0;
							int l1 = (int)(255.0D * d0);

							if (flag)
							{
								l1 = 255;
							}

							l1 = (int)((float)l1 * f);
							++j;

							if (l1 > 3)
							{
								int i2 = 0;
								int j2 = -i1 * 9;
								drawRect(i2, j2 - 9, i2 + l + 4, j2, l1 / 2 << 24);
								String s = chatline.getChatComponent().getFormattedText();
								GlStateManager.enableBlend();
								drawStringWithShadow(s, (float)i2, (float)(j2 - 8), 16777215 + (l1 << 24));
								GlStateManager.disableAlpha();
								GlStateManager.disableBlend();
							}
						}
					}
				}

				if (flag)
				{
					int k2 = getStringHeight();
					GlStateManager.translate(-3.0F, 0.0F, 0.0F);
					int l2 = k * k2 + k;
					int i3 = j * k2 + j;
					int j3 = this.scrollPos * i3 / k;
					int k1 = i3 * i3 / l2;

					if (l2 != i3)
					{
						int k3 = j3 > 0 ? 170 : 96;
						int l3 = this.isScrolled ? 13382451 : 3355562;
						drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
						drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
					}
				}

				GlStateManager.popMatrix();
			}
		}
	}

	private void drawStringWithShadow(String text, float x, float y, int color) {
		if (huzuni.settings.customChat.isEnabled())
			huzuni.chatFontRenderer.drawStringWithShadow(text, (int) x, (int) y - 3, color);
		else
			mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
	}

	private int getStringHeight() {
		return mc.fontRendererObj.FONT_HEIGHT;
		// return huzuni.settings.customChat.isEnabled() ? huzuni.chatFontRenderer.getHeight() : mc.fontRenderer.FONT_HEIGHT;
	}

	private int getStringWidth(String text) {
		return mc.fontRendererObj.getStringWidth(text);
		// return huzuni.settings.customChat.isEnabled() ? huzuni.chatFontRenderer.getStringWidth(text) : mc.fontRenderer.getStringWidth(text);
	}
	/**
	 * Clears the chat.
	 */
	public void clearChatMessages()
	{
		this.field_146253_i.clear();
		this.chatLines.clear();
		this.sentMessages.clear();
	}

	public void printChatMessage(IChatComponent p_146227_1_)
	{
		this.printChatMessageWithOptionalDeletion(p_146227_1_, 0);
	}

	private List<IChatComponent> splitText(IChatComponent textComponent, int maxTextLenght, boolean p_178908_3_, boolean forceTextColor)
	{
		int i = 0;
		IChatComponent itextcomponent = new ChatComponentText("");
		List<IChatComponent> list = Lists.newArrayList();
		List<IChatComponent> list1 = Lists.newArrayList(textComponent);

		for (int j = 0; j < list1.size(); ++j)
		{
			IChatComponent itextcomponent1 = list1.get(j);
			String s = itextcomponent1.getUnformattedText();
			boolean flag = false;

			if (s.contains("\n"))
			{
				int k = s.indexOf(10);
				String s1 = s.substring(k + 1);
				s = s.substring(0, k + 1);
				ChatComponentText ChatComponentText = new ChatComponentText(s1);
				ChatComponentText.setChatStyle(itextcomponent1.getChatStyle().createShallowCopy());
				list1.add(j + 1, ChatComponentText);
				flag = true;
			}

			String s4 = GuiUtilRenderComponents.func_178909_a(itextcomponent1.getChatStyle().getFormattingCode() + s, forceTextColor);
			String s5 = s4.endsWith("\n") ? s4.substring(0, s4.length() - 1) : s4;
			int i1 = getStringWidth(s5);
			ChatComponentText ChatComponentText1 = new ChatComponentText(s5);
			ChatComponentText1.setChatStyle(itextcomponent1.getChatStyle().createShallowCopy());

			if (i + i1 > maxTextLenght)
			{
				String s2 = trimStringToWidth(s4, maxTextLenght - i, false);
				String s3 = s2.length() < s4.length() ? s4.substring(s2.length()) : null;

				if (s3 != null && !s3.isEmpty())
				{
					int l = s2.lastIndexOf(" ");

					if (l >= 0 && getStringWidth(s4.substring(0, l)) > 0)
					{
						s2 = s4.substring(0, l);

						if (p_178908_3_)
						{
							++l;
						}

						s3 = s4.substring(l);
					}
					else if (i > 0 && !s4.contains(" "))
					{
						s2 = "";
						s3 = s4;
					}

					ChatComponentText ChatComponentText2 = new ChatComponentText(s3);
					ChatComponentText2.setChatStyle(itextcomponent1.getChatStyle().createShallowCopy());
					list1.add(j + 1, ChatComponentText2);
				}

				i1 = getStringWidth(s2);
				ChatComponentText1 = new ChatComponentText(s2);
				ChatComponentText1.setChatStyle(itextcomponent1.getChatStyle().createShallowCopy());
				flag = true;
			}

			if (i + i1 <= maxTextLenght)
			{
				i += i1;
				itextcomponent.appendSibling(ChatComponentText1);
			}
			else
			{
				flag = true;
			}

			if (flag)
			{
				list.add(itextcomponent);
				i = 0;
				itextcomponent = new ChatComponentText("");
			}
		}

		list.add(itextcomponent);
		return list;
	}

	/**
	 * Trims a string to a specified width, and will reverse it if reverse is set.
	 */
	public String trimStringToWidth(String text, int width, boolean reverse) {
		StringBuilder stringbuilder = new StringBuilder();
		int i = 0;
		int j = reverse ? text.length() - 1 : 0;
		int k = reverse ? -1 : 1;
		boolean flag = false;
		boolean flag1 = false;

		for (int l = j; l >= 0 && l < text.length() && i < width; l += k) {
			char c0 = text.charAt(l);
			int i1 = getStringWidth("" + c0);

			if (flag) {
				flag = false;

				if (c0 != 108 && c0 != 76) {
					if (c0 == 114 || c0 == 82) {
						flag1 = false;
					}
				} else {
					flag1 = true;
				}
			} else if (i1 < 0) {
				flag = true;
			} else {
				i += i1;

				if (flag1) {
					++i;
				}
			}

			if (i > width) {
				break;
			}

			if (reverse) {
				stringbuilder.insert(0, c0);
			} else {
				stringbuilder.append(c0);
			}
		}

		return stringbuilder.toString();
	}
	/**
	 * prints the ChatComponent to Chat. If the ID is not 0, deletes an existing Chat Line of that ID from the GUI
	 *
	 * @param chatComponent The chat component to display
	 * @param chatLineId The chat line id
	 */
	public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId)
	{
		this.setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
		logger.info("[CHAT] " + chatComponent.getUnformattedText());
	}

	private void setChatLine(IChatComponent chatComponent, int chatLineId, int p_146237_3_, boolean p_146237_4_)
	{
		if (chatLineId != 0) {
			this.deleteChatLine(chatLineId);
		}

		int i = MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale());
		List<IChatComponent> list = splitText(chatComponent, i, false, false);
		boolean flag = this.getChatOpen();

		for (IChatComponent ichatcomponent : list)
		{
			if (flag && this.scrollPos > 0)
			{
				this.isScrolled = true;
				this.scroll(1);
			}

			this.field_146253_i.add(0, new ChatLine(p_146237_3_, ichatcomponent, chatLineId));
		}

		while (this.field_146253_i.size() > 100)
		{
			this.field_146253_i.remove(this.field_146253_i.size() - 1);
		}

		if (!p_146237_4_)
		{
			this.chatLines.add(0, new ChatLine(p_146237_3_, chatComponent, chatLineId));

			while (this.chatLines.size() > 100)
			{
				this.chatLines.remove(this.chatLines.size() - 1);
			}
		}
	}

	public void refreshChat()
	{
		this.field_146253_i.clear();
		this.resetScroll();

		for (int i = this.chatLines.size() - 1; i >= 0; --i)
		{
			ChatLine chatline = (ChatLine)this.chatLines.get(i);
			this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
		}
	}

	public List<String> getSentMessages()
	{
		return this.sentMessages;
	}

	/**
	 * Adds this string to the list of sent messages, for recall using the up/down arrow keys
	 */
	public void addToSentMessages(String p_146239_1_)
	{
		if (this.sentMessages.isEmpty() || !((String)this.sentMessages.get(this.sentMessages.size() - 1)).equals(p_146239_1_))
		{
			this.sentMessages.add(p_146239_1_);
		}
	}

	/**
	 * Resets the chat scroll (executed when the GUI is closed, among others)
	 */
	public void resetScroll()
	{
		this.scrollPos = 0;
		this.isScrolled = false;
	}

	/**
	 * Scrolls the chat by the given number of lines.
	 */
	public void scroll(int p_146229_1_)
	{
		this.scrollPos += p_146229_1_;
		int i = this.field_146253_i.size();

		if (this.scrollPos > i - this.getLineCount())
		{
			this.scrollPos = i - this.getLineCount();
		}

		if (this.scrollPos <= 0)
		{
			this.scrollPos = 0;
			this.isScrolled = false;
		}
	}

	/**
	 * Gets the chat component under the mouse
	 */
	public IChatComponent getChatComponent(int p_146236_1_, int p_146236_2_)
	{
		if (!this.getChatOpen())
		{
			return null;
		}
		else
		{
			ScaledResolution scaledresolution = new ScaledResolution(this.mc);
			int i = scaledresolution.getScaleFactor();
			float f = this.getChatScale();
			int j = p_146236_1_ / i - 3;
			int k = p_146236_2_ / i - 27;
			j = MathHelper.floor_float((float)j / f);
			k = MathHelper.floor_float((float)k / f);

			if (j >= 0 && k >= 0)
			{
				int l = Math.min(this.getLineCount(), this.field_146253_i.size());

				if (j <= MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l)
				{
					int i1 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;

					if (i1 >= 0 && i1 < this.field_146253_i.size())
					{
						ChatLine chatline = (ChatLine)this.field_146253_i.get(i1);
						int j1 = 0;

						for (IChatComponent ichatcomponent : chatline.getChatComponent())
						{
							if (ichatcomponent instanceof ChatComponentText)
							{
								j1 += getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText)ichatcomponent).getChatComponentText_TextValue(), false));

								if (j1 > j)
								{
									return ichatcomponent;
								}
							}
						}
					}

					return null;
				}
				else
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * Returns true if the chat GUI is open
	 */
	public boolean getChatOpen()
	{
		return this.mc.currentScreen instanceof GuiChat;
	}

	/**
	 * finds and deletes a Chat line by ID
	 */
	public void deleteChatLine(int p_146242_1_)
	{
		Iterator<ChatLine> iterator = this.field_146253_i.iterator();

		while (iterator.hasNext())
		{
			ChatLine chatline = (ChatLine)iterator.next();

			if (chatline.getChatLineID() == p_146242_1_)
			{
				iterator.remove();
			}
		}

		iterator = this.chatLines.iterator();

		while (iterator.hasNext())
		{
			ChatLine chatline1 = (ChatLine)iterator.next();

			if (chatline1.getChatLineID() == p_146242_1_)
			{
				iterator.remove();
				break;
			}
		}
	}

	public int getChatWidth()
	{
		return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
	}

	public int getChatHeight()
	{
		return calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
	}

	/**
	 * Returns the chatscale from mc.gameSettings.chatScale
	 */
	public float getChatScale()
	{
		return this.mc.gameSettings.chatScale;
	}

	public static int calculateChatboxWidth(float p_146233_0_)
	{
		int i = 320;
		int j = 40;
		return MathHelper.floor_float(p_146233_0_ * (float)(i - j) + (float)j);
	}

	public static int calculateChatboxHeight(float p_146243_0_)
	{
		int i = 180;
		int j = 20;
		return MathHelper.floor_float(p_146243_0_ * (float)(i - j) + (float)j);
	}

	public int getLineCount()
	{
		return this.getChatHeight() / 9;
	}
}
