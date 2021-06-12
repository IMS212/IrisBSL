package net.coderbot.iris.mixin.fabulous;

import net.minecraft.client.resource.VideoWarningManager;
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.Option;

@Environment(EnvType.CLIENT)
@Mixin(Option.class)
public class MixinOption {
	@Redirect(method = "method_32563", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;supportsGl30()Z"))
	private static boolean iris$onAttemptedToSelectFabulousGraphics() {
		// Returning false here will cause Minecraft to cycle between Fancy and Fast, disabling Fabulous graphics
		if(!Iris.getIrisConfig().areShadersEnabled()) {
			return !GlStateManager.supportsGl30();
		}
		return true;
	}
}
