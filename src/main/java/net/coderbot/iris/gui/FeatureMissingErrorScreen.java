package net.coderbot.iris.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FeatureMissingErrorScreen extends Screen {
	private final Screen parent;

	public FeatureMissingErrorScreen(Screen parent, Component title, Component message) {
		super(title);
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
	}
}
