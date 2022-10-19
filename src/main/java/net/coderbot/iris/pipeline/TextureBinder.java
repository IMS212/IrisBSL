package net.coderbot.iris.pipeline;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shadows.ShadowRenderTargets;

public class TextureBinder {
	public static void bindTextures(RenderTargets targets, ShadowRenderTargets shadowTargets, int noiseTex, int whitePixel) {
		for (int i = 3; i < targets.getRenderTargetCount(); i++) {
			IrisRenderSystem.bindTextureToUnit(i, targets.get(i).getMainTexture());
			IrisRenderSystem.bindTextureToUnit(i + 16, targets.get(i).getAltTexture());
		}

		IrisRenderSystem.bindTextureToUnit(35, targets.getDepthTexture());
		IrisRenderSystem.bindTextureToUnit(36, targets.getDepthTextureNoTranslucents().getTextureId());
		IrisRenderSystem.bindTextureToUnit(37, targets.getDepthTextureNoHand().getTextureId());

		if (shadowTargets != null) {
			IrisRenderSystem.bindTextureToUnit(38, shadowTargets.getDepthTexture().getTextureId());
			IrisRenderSystem.bindTextureToUnit(39, shadowTargets.getDepthTextureNoTranslucents().getTextureId());
			IrisRenderSystem.bindTextureToUnit(40, shadowTargets.getDepthTexture().getTextureId());
			IrisRenderSystem.bindTextureToUnit(41, shadowTargets.getDepthTextureNoTranslucents().getTextureId());
			IrisRenderSystem.bindTextureToUnit(42, shadowTargets.getColorTextureId(0));
			IrisRenderSystem.bindTextureToUnit(43, shadowTargets.getColorTextureId(1));
		}

		IrisRenderSystem.bindTextureToUnit(44, noiseTex);
		IrisRenderSystem.bindTextureToUnit(45, whitePixel);
	}
}
