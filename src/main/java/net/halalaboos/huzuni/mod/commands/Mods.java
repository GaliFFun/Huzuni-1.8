package net.halalaboos.huzuni.mod.commands;

import net.halalaboos.huzuni.api.mod.Mod;
import net.halalaboos.huzuni.api.mod.command.impl.BasicCommand;
import net.halalaboos.mcwrapper.api.util.enums.TextColor;

/**
 * @author brudin
 * @version 1.0
 * @since 4/10/14
 */
public final class Mods extends BasicCommand {

	public Mods() {
		super(new String[] { "mods", "hacks" }, "Lists all of the mods.");
	}

	@Override
	protected void runCommand(String input, String[] args) {
		String allMods = "Mods (" + huzuni.modManager.getMods().size() + "): ";
		for (Mod mod : huzuni.modManager.getMods()) {
			allMods += (mod.isEnabled() ? TextColor.GREEN : TextColor.RED) + mod.getName() + TextColor.RESET + ", ";
		}
		huzuni.addChatMessage(allMods.substring(0, allMods.length() - 2));
	}
	
}