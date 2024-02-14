package net.irisshaders.iris;

import com.github.zafarkhaja.semver.Version;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;

import java.nio.file.Path;

public class IrisMultiPlat {
	@ExpectPlatform
	public static Path getConfigDirectory() {
		// Just throw an error, the content should get replaced at runtime.
		// Something is terribly wrong if this is not replaced.
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean isModLoadedMixinPlugin(String mod) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static Path getGameDirectory() {
		// Just throw an error, the content should get replaced at runtime.
		// Something is terribly wrong if this is not replaced.
		throw new AssertionError();
	}

	@ExpectPlatform
	public static Version getVersion() {
		throw new AssertionError();
	}

	@ExpectPlatform
    public static void drawParticles(ParticleEngine particleEngine, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LightTexture lightTexture, Camera camera, float f, Frustum cullingFrustum) {
		throw new AssertionError();
    }

	@ExpectPlatform
    public static boolean isDevEnv() {
		throw new AssertionError();
    }
}
