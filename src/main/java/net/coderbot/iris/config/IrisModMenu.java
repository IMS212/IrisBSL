package net.coderbot.iris.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.coderbot.iris.gui.screen.IrisConfigScreen;

public class IrisModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return IrisConfigScreen::new;
    }
}
