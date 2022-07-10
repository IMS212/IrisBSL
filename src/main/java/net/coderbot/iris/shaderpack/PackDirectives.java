package net.coderbot.iris.shaderpack;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.buffer.BufferMapping;
import net.coderbot.iris.gl.buffer.BufferObjectInformation;

import java.util.List;
import java.util.Set;

public class PackDirectives {
	private int noiseTextureResolution;
	private float sunPathRotation;
	private float ambientOcclusionLevel;
	private float wetnessHalfLife;
	private float drynessHalfLife;
	private float eyeBrightnessHalfLife;
	private float centerDepthHalfLife;
	private boolean areCloudsEnabled;
	private boolean underwaterOverlay;
	private boolean vignette;
	private boolean rainDepth;
	private boolean separateAo;
	private boolean oldLighting;
	private boolean particlesBeforeDeferred;
	private Object2ObjectMap<String, Object2BooleanMap<String>> explicitFlips = new Object2ObjectOpenHashMap<>();
	private Object2ObjectMap<String, Set<BufferMapping>> bufferMappings = new Object2ObjectOpenHashMap<>();
	private List<BufferObjectInformation> bufferObjects;

	private final PackRenderTargetDirectives renderTargetDirectives;
	private final PackShadowDirectives shadowDirectives;

	private PackDirectives(Set<Integer> supportedRenderTargets, PackShadowDirectives packShadowDirectives) {
		noiseTextureResolution = 256;
		sunPathRotation = 0.0F;
		ambientOcclusionLevel = 1.0F;
		wetnessHalfLife = 600.0f;
		drynessHalfLife = 200.0f;
		eyeBrightnessHalfLife = 10.0f;
		centerDepthHalfLife = 1.0F;
		bufferMappings = new Object2ObjectOpenHashMap<>();
		bufferObjects = new ReferenceArrayList<>();
		renderTargetDirectives = new PackRenderTargetDirectives(supportedRenderTargets);
		shadowDirectives = packShadowDirectives;
	}

	PackDirectives(Set<Integer> supportedRenderTargets, ShaderProperties properties) {
		this(supportedRenderTargets, new PackShadowDirectives(properties));
		areCloudsEnabled = properties.areCloudsEnabled();
		underwaterOverlay = properties.getUnderwaterOverlay().orElse(false);
		vignette = properties.getVignette().orElse(false);
		rainDepth = properties.getRainDepth().orElse(false);
		separateAo = properties.getSeparateAo().orElse(false);
		oldLighting = properties.getOldLighting().orElse(false);
		explicitFlips = properties.getExplicitFlips();
		bufferMappings = properties.getBufferMappings();
		bufferObjects = properties.getBufferObjects();
		particlesBeforeDeferred = properties.getParticlesBeforeDeferred().orElse(false);
	}

	PackDirectives(Set<Integer> supportedRenderTargets, PackDirectives directives) {
		this(supportedRenderTargets, new PackShadowDirectives(directives.getShadowDirectives()));
		areCloudsEnabled = directives.areCloudsEnabled();
		separateAo = directives.separateAo;
		oldLighting = directives.oldLighting;
		explicitFlips = directives.explicitFlips;
		bufferMappings = directives.bufferMappings;
		bufferObjects = directives.bufferObjects;
		particlesBeforeDeferred = directives.particlesBeforeDeferred;
	}

	public int getNoiseTextureResolution() {
		return noiseTextureResolution;
	}

	public float getSunPathRotation() {
		return sunPathRotation;
	}

	public float getAmbientOcclusionLevel() {
		return ambientOcclusionLevel;
	}

	public float getWetnessHalfLife() {
		return wetnessHalfLife;
	}

	public float getDrynessHalfLife() {
		return drynessHalfLife;
	}

	public float getEyeBrightnessHalfLife() {
		return eyeBrightnessHalfLife;
	}

	public float getCenterDepthHalfLife() {
		return centerDepthHalfLife;
	}

	public boolean areCloudsEnabled() {
		return areCloudsEnabled;
	}

	public boolean underwaterOverlay() {
		return underwaterOverlay;
	}

	public boolean vignette() {
		return vignette;
	}

	public boolean rainDepth() {
		return rainDepth;
	}

	public boolean shouldUseSeparateAo() {
		return separateAo;
	}

	public boolean isOldLighting() {
		return oldLighting;
	}

	public boolean areParticlesBeforeDeferred() {
		return particlesBeforeDeferred;
	}

	public PackRenderTargetDirectives getRenderTargetDirectives() {
		return renderTargetDirectives;
	}

	public PackShadowDirectives getShadowDirectives() {
		return shadowDirectives;
	}

	public List<BufferObjectInformation> getBufferObjects() {
		return bufferObjects;
	}

	private static float clamp(float val, float lo, float hi) {
		return Math.max(lo, Math.min(hi, val));
	}

	public void acceptDirectivesFrom(DirectiveHolder directives) {
		renderTargetDirectives.acceptDirectives(directives);
		shadowDirectives.acceptDirectives(directives);

		directives.acceptConstIntDirective("noiseTextureResolution",
				noiseTextureResolution -> this.noiseTextureResolution = noiseTextureResolution);

		directives.acceptConstFloatDirective("sunPathRotation",
				sunPathRotation -> this.sunPathRotation = sunPathRotation);

		directives.acceptConstFloatDirective("ambientOcclusionLevel",
				ambientOcclusionLevel -> this.ambientOcclusionLevel = clamp(ambientOcclusionLevel, 0.0f, 1.0f));

		directives.acceptConstFloatDirective("wetnessHalflife",
			wetnessHalfLife -> this.wetnessHalfLife = wetnessHalfLife);

		directives.acceptConstFloatDirective("drynessHalflife",
			wetnessHalfLife -> this.wetnessHalfLife = wetnessHalfLife);

		directives.acceptConstFloatDirective("eyeBrightnessHalflife",
			eyeBrightnessHalfLife -> this.eyeBrightnessHalfLife = eyeBrightnessHalfLife);

		directives.acceptConstFloatDirective("centerDepthHalflife",
			centerDepthHalfLife -> this.centerDepthHalfLife = centerDepthHalfLife);
	}

	public ImmutableMap<Integer, Boolean> getExplicitFlips(String pass) {
		ImmutableMap.Builder<Integer, Boolean> explicitFlips = ImmutableMap.builder();

		Object2BooleanMap<String> explicitFlipsStr = this.explicitFlips.get(pass);

		if (explicitFlipsStr == null) {
			explicitFlipsStr = Object2BooleanMaps.emptyMap();
		}

		explicitFlipsStr.forEach((buffer, shouldFlip) -> {
			int index = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.indexOf(buffer);

			if (index == -1 && buffer.startsWith("colortex")) {
				String id = buffer.substring("colortex".length());

				try {
					index = Integer.parseInt(id);
				} catch (NumberFormatException e) {
					// fall through to index == null check for unknown buffer.
				}
			}

			if (index != -1) {
				explicitFlips.put(index, shouldFlip);
			} else {
				Iris.logger.warn("Unknown buffer with ID " + buffer + " specified in flip directive for pass "
						+ pass);
			}
		});

		return explicitFlips.build();
	}

	public Set<BufferMapping> getBufferMappings(String pass) {
		return this.bufferMappings.get(pass);
	}
}
