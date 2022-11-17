package net.coderbot.iris.gui.element.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ElementWidgetScreenData {
	public static final ElementWidgetScreenData EMPTY = new ElementWidgetScreenData("", true);

	public final String heading;
	public final boolean backButton;

	public ElementWidgetScreenData(String heading, boolean backButton) {
		this.heading = heading;
		this.backButton = backButton;
	}
}
