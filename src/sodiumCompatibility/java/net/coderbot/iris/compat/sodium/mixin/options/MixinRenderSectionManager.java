package net.coderbot.iris.compat.sodium.mixin.options;

import me.jellysquid.mods.sodium.config.user.UserConfig;
import me.jellysquid.mods.sodium.render.chunk.RenderSectionManager;
import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Disables fog occlusion when a shader pack is enabled, since shaders are not guaranteed to actually implement fog.
 */
@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {

}
