package net.coderbot.iris.gui.element.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ElementWidgetScreenData {
	public static final ElementWidgetScreenData EMPTY = new ElementWidgetScreenData(new TextComponent(""), true);

	public final Component heading;
	public final boolean backButton;

	public ElementWidgetScreenData(Component heading, boolean backButton) {
		this.heading = heading;
		this.backButton = backButton;
	}
}
