package net.coderbot.iris.samplers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.gl.uniform.DynamicLocationalUniformHolder;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.minecraft.client.renderer.texture.AbstractTexture;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class IrisSamplersReloaded {
	public static final int ALBEDO_TEXTURE_UNIT = 0;
	public static final int OVERLAY_TEXTURE_UNIT = 1;
	public static final int LIGHTMAP_TEXTURE_UNIT = 2;

	public static final int COLOR_TEX_START = 3; // colortex0-15, colortex0-15alt

	public static final int DEPTH_TEX_START = 35; // depthtex0-2

	public static final int SHADOW_TEX_START = 38; //shadowtex0-1, HW0-1, shadowcolor0-1

	public static final int NOISE_TEX = 44; // noisetex

	public static final int WHITE_PIXEL = 45;


	public static final ImmutableSet<Integer> WORLD_RESERVED_TEXTURE_UNITS = ImmutableSet.of(0, 1, 2);

	// TODO: In composite programs, there shouldn't be any reserved textures.
	// We need a way to restore these texture bindings.
	public static final ImmutableSet<Integer> COMPOSITE_RESERVED_TEXTURE_UNITS = ImmutableSet.of(1, 2);

	private IrisSamplersReloaded() {
		// no construction allowed
	}

	public static void addRenderTargetSamplers(LocationalUniformHolder samplers,
											   RenderTargets renderTargets, boolean isFullscreenPass) {
		// colortex0,1,2,3 are only able to be sampled from fullscreen passes.
		// Iris could lift this restriction, though I'm not sure if it could cause issues.
		int startIndex = isFullscreenPass ? 0 : 4;

		for (int i = startIndex; i < renderTargets.getRenderTargetCount(); i++) {
			final String name = "colortex" + i;

			// TODO: How do custom textures interact with aliases?

			if (i == 0) {
				addSampler(samplers, COLOR_TEX_START + i, "texture", "tex", "gtexture", "gcolor");
			}

			if (i < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size()) {
				String legacyName = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(i);

				addSampler(samplers, COLOR_TEX_START + i, name, legacyName);

			} else {
				addSampler(samplers, COLOR_TEX_START + i, name);
			}

			final String nameAlt = "colortex" + i + "Alt";

			if (i < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size()) {
				String legacyName = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(i) + "Alt";

				addSampler(samplers, COLOR_TEX_START + 16 + i, nameAlt, legacyName);

			} else {
				addSampler(samplers, COLOR_TEX_START + 16 + i, nameAlt);
			}
		}
	}

	public static void addNoiseSampler(LocationalUniformHolder samplers, IntSupplier sampler) {
		addSampler(samplers, NOISE_TEX, "noisetex");
	}

	public static boolean hasShadowSamplers(LocationalUniformHolder samplers) {
		// TODO: Keep this up to date with the actual definitions.
		// TODO: Don't query image presence using the sampler interface even though the current underlying implementation
		//       is the same.
		ImmutableList<String> shadowSamplers = ImmutableList.of("shadowtex0", "shadowtex0HW", "shadowtex1", "shadowtex1HW", "shadow", "watershadow",
				"shadowcolor", "shadowcolor0", "shadowcolor1", "shadowcolorimg0", "shadowcolorimg1");

		for (String samplerName : shadowSamplers) {
			if (samplers.hasSampler(samplerName)) {
				return true;
			}
		}

		return false;
	}

	public static boolean addShadowSamplers(LocationalUniformHolder samplers, ShadowRenderTargets shadowRenderTargets) {
		boolean usesShadows;

		// TODO: figure this out from parsing the shader source code to be 100% compatible with the legacy
		// shader packs that rely on this behavior.
		boolean waterShadowEnabled = samplers.hasSampler("watershadow");

		if (waterShadowEnabled) {
			usesShadows = true;
			addSampler(samplers, /*shadowRenderTargets.getDepthTexture()::getTextureId*/ SHADOW_TEX_START, "shadowtex0", "watershadow");
			addSampler(samplers, SHADOW_TEX_START + 1,
					"shadowtex1", "shadow");
		} else {
			usesShadows = addSampler(samplers, SHADOW_TEX_START, "shadowtex0", "shadow");
			usesShadows |= addSampler(samplers, SHADOW_TEX_START + 1, "shadowtex1");
		}

		addSampler(samplers, SHADOW_TEX_START + 4, "shadowcolor", "shadowcolor0");
		addSampler(samplers, SHADOW_TEX_START + 5, "shadowcolor1");

		if (shadowRenderTargets.isHardwareFiltered(0)) {
			addSampler(samplers, SHADOW_TEX_START + 2, "shadowtex0HW");
		}

		if (shadowRenderTargets.isHardwareFiltered(1)) {
			addSampler(samplers, SHADOW_TEX_START + 3, "shadowtex1HW");
		}

		return usesShadows;
	}

	private static boolean addSampler(LocationalUniformHolder samplers, int samplerUnit, String... names) {
		boolean hasSampler = false;
		for (String name : names) {
			samplers.uniform1i(UniformUpdateFrequency.ONCE, name, () -> samplerUnit);
			hasSampler |= samplers.hasSampler(name);
		}

		return hasSampler;
	}

	private static void addSampler(DynamicLocationalUniformHolder samplers, IntSupplier supplier, ValueUpdateNotifier notifier, String... names) {
		for (String name : names) {
			samplers.uniform1i(name, supplier, notifier);
		}
	}

	public static boolean hasPBRSamplers(LocationalUniformHolder samplers) {
		return samplers.hasSampler("normals") || samplers.hasSampler("specular");
	}

	public static void addLevelSamplers(DynamicLocationalUniformHolder samplers, WorldRenderingPipeline pipeline, AbstractTexture whitePixel, InputAvailability availability) {
		if (availability.texture) {
			addSampler(samplers, ALBEDO_TEXTURE_UNIT, "tex", "texture", "gtexture");
		} else {
			// TODO: Rebind unbound sampler IDs instead of hardcoding a list...
			addSampler(samplers, WHITE_PIXEL, "tex", "texture", "gtexture",
					"gcolor", "colortex0");
		}

		if (availability.lightmap) {
			addSampler(samplers, LIGHTMAP_TEXTURE_UNIT, "lightmap");
		} else {
			addSampler(samplers, WHITE_PIXEL, "lightmap");
		}

		if (availability.overlay) {
			addSampler(samplers, OVERLAY_TEXTURE_UNIT, "iris_overlay");
		} else {
			addSampler(samplers, WHITE_PIXEL, "iris_overlay");
		}

		addSampler(samplers, pipeline::getCurrentNormalTexture, StateUpdateNotifiers.normalTextureChangeNotifier, "normals");
		addSampler(samplers, pipeline::getCurrentSpecularTexture, StateUpdateNotifiers.specularTextureChangeNotifier, "specular");
	}

	public static void addWorldDepthSamplers(LocationalUniformHolder samplers, RenderTargets renderTargets) {
		addSampler(samplers, DEPTH_TEX_START, "depthtex0");
		// TODO: Should depthtex2 be made available to gbuffer / shadow programs?
		addSampler(samplers, DEPTH_TEX_START + 1, "depthtex1");
	}

	public static void addCompositeSamplers(LocationalUniformHolder samplers, RenderTargets renderTargets) {
		addSampler(samplers, DEPTH_TEX_START,
				"gdepthtex", "depthtex0");
		addSampler(samplers, DEPTH_TEX_START + 1,
				"depthtex1");
		addSampler(samplers, DEPTH_TEX_START + 2,
				"depthtex2");
	}
}
