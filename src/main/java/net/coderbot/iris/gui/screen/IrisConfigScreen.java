package net.coderbot.iris.gui.screen;

import net.coderbot.iris.Iris;
import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.ScreenStack;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class IrisConfigScreen extends Screen implements HudHideable {
    protected final IrisConfig config = Iris.getIrisConfig();
    protected PropertyDocumentWidget configProperties;

    private final Screen parent;

	public IrisConfigScreen(Screen parent) {
        super(new LiteralText(""));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int bottomCenter = this.width / 2 - 50;
        boolean inWorld = this.client.world != null;

        float scrollAmount = 0.0f;
        String page = "main";

        if (this.configProperties != null) {
            scrollAmount = (float)this.configProperties.getScrollAmount() / this.configProperties.getMaxScroll();
            page = this.configProperties.getCurrentPage();
        }

        this.configProperties  = new PropertyDocumentWidget(client, width, height, 20, this.height - 34, 0, this.width, 26, width - 39);
        if (inWorld) this.configProperties.setRenderBackground(false);
        this.configProperties.setDocument(this.config.createDocument(this.client.textRenderer, this, this.configProperties, 320), "main");

        this.configProperties.setScrollAmount(this.configProperties.getMaxScroll() * scrollAmount);
        this.configProperties.goTo(page);

        this.addDrawableChild(configProperties);

        this.addDrawableChild(new ButtonWidget(bottomCenter + 104, this.height - 27, 100, 20, ScreenTexts.DONE, button -> { this.saveConfig(); onClose(); }));
        this.addDrawableChild(new ButtonWidget(bottomCenter, this.height - 27, 100, 20, new TranslatableText("options.iris.apply"), button -> this.saveConfig()));
        this.addDrawableChild(new ButtonWidget(bottomCenter - 104, this.height - 27, 100, 20, new TranslatableText("options.iris.refresh"), button -> this.loadConfig()));

        loadConfig();

		if (parent != null) {
			ScreenStack.push(parent);
		}
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) this.renderBackground(matrices);
        else this.fillGradient(matrices, 0, 0, width, height, 0x4F232323, 0x4F232323);
        this.configProperties.render(matrices, mouseX, mouseY, delta);

        GuiUtil.drawDirtTexture(client, 0, 0, -100, width, 20);
        GuiUtil.drawDirtTexture(client, 0, this.height - 34, -100, width, 34);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
		ScreenStack.pull(this.getClass());
		client.openScreen(ScreenStack.pop());
	}

    private void loadConfig() {
        this.configProperties.loadProperties();
    }

    private void saveConfig() {
        this.configProperties.saveProperties();
    }
}
