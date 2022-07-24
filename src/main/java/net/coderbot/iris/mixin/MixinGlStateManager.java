package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.sampler.SamplerLimits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 12), require = 1)
	private static int iris$increaseMaximumAllowedTextureUnits(int existingValue) {
		return SamplerLimits.get().getMaxTextureUnits();
	}

}
