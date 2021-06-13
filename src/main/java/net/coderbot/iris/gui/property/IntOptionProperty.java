package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class IntOptionProperty extends OptionProperty<Integer> {
    public IntOptionProperty(List<Integer> values, int defaultIndex, PropertyDocumentWidget document, String key, Text label, boolean isSlider) {
        super(values, defaultIndex, document, key, label, isSlider);
    }

	@Override
	protected Integer fallbackValue() {
		return 0;
	}

	@Override
    public Text createValueText(int width) {
    	String translation = "value." + key + "." + this.getValue();
    	boolean hasTranslation = I18n.hasTranslation(translation);
        return GuiUtil.trimmed(MinecraftClient.getInstance().textRenderer, hasTranslation ? translation : Integer.toString(this.getValue()), width, hasTranslation, true, isDefault() ? Formatting.RESET : Formatting.YELLOW);
    }

    @Override
    public void setValue(String value) {
        this.valueText = null;
        try {
            this.setValue(Integer.parseInt(value));
        } catch (NumberFormatException ignored) { return; }
        this.index = defaultIndex;
    }
}
