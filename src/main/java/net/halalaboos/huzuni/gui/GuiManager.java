package net.halalaboos.huzuni.gui;

import net.halalaboos.huzuni.Huzuni;
import net.halalaboos.huzuni.api.gui.Theme;
import net.halalaboos.huzuni.api.gui.WidgetManager;
import net.halalaboos.huzuni.api.gui.widget.ScreenGlue;
import net.halalaboos.huzuni.api.gui.widget.Widget;
import net.halalaboos.huzuni.api.gui.widget.WidgetGlue;
import net.halalaboos.huzuni.api.node.Mode;
import net.halalaboos.huzuni.gui.widgets.*;
import net.halalaboos.huzuni.gui.widgets.enabled.EnabledModsWidget;
import net.halalaboos.huzuni.gui.widgets.tabbed.TabbedMenuWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages important gui information and allows access to the widget manager, settings menu, and notifications.
 * */
public class GuiManager {
	
	public final WidgetManager widgetManager = new WidgetManager(Huzuni.INSTANCE);
	
	public final SettingsMenu settingsMenu = new SettingsMenu();
		
	private final List<Notification> notifications = new ArrayList<>();
	
	private Mode<Theme> themes;
	
	public GuiManager() {
		
	}

	/**
     * Initializes the themes, widgets, and settings menu.
     * */
	public void init() {
		themes = new Mode<Theme>("Theme", "Themes used to render the menus", new BasicTheme()) {
			@Override
			public void setSelectedItem(int selectedItem) {
				super.setSelectedItem(selectedItem);
				widgetManager.setTheme(getSelectedItem());
				settingsMenu.setTheme(getSelectedItem());
			}
		};
		widgetManager.setTheme(getTheme());

		Widget titleMenu = new TitleWidget(widgetManager).setGlue(ScreenGlue.TOP_AND_LEFT).setEnabled(true);
		widgetManager.addWidget(titleMenu);
		Widget tabbedMenu = new TabbedMenuWidget(widgetManager).setGlue(new WidgetGlue(WidgetGlue.BOTTOM_LEFT, titleMenu)).setEnabled(true);
		widgetManager.addWidget(tabbedMenu);
		Widget enabledMods = new EnabledModsWidget(widgetManager).setGlue(new WidgetGlue(WidgetGlue.BOTTOM_LEFT, tabbedMenu)).setEnabled(true);
		widgetManager.addWidget(enabledMods);
		widgetManager.addWidget(new NotificationWidget(widgetManager, notifications).setGlue(ScreenGlue.TOP_AND_RIGHT).setEnabled(true));
		widgetManager.addWidget(new CoordinatesWidget(widgetManager).setGlue(ScreenGlue.BOTTOM_AND_LEFT));
		widgetManager.addWidget(new CompassWidget(widgetManager).setGlue(ScreenGlue.CENTER_TOP));
		widgetManager.addWidget(new FPSWidget(widgetManager));
		widgetManager.addWidget(new BiomeWidget(widgetManager));
		widgetManager.addWidget(new TimeWidget(widgetManager));
		widgetManager.addWidget(new ArmorStatusWidget(widgetManager).setGlue(ScreenGlue.CENTER_RIGHT).setEnabled(true));
		widgetManager.addWidget(new TextRadarWidget(widgetManager));
		widgetManager.addWidget(new FacingWidget(widgetManager));
		widgetManager.addWidget(new FireStatusWidget(widgetManager));
		settingsMenu.init();
	}

	/**
     * Invoked to load the widget manager and settings menu.
     * */
	public void load() {
		widgetManager.setTheme(getTheme());
		settingsMenu.setTheme(getTheme());
		widgetManager.load();
		settingsMenu.load();
	}
	
	public void save() {
		widgetManager.save();
	}
	
	public Theme getTheme() {
		return themes.getSelectedItem();
	}

	public Mode<Theme> getThemes() {
		return themes;
	}
	
	public void addNotification(Notification notification) {
		notifications.add(notification);
	}
}
