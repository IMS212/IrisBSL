package net.irisshaders.iris.fabric;

import com.github.zafarkhaja.semver.Version;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;

import java.nio.file.Path;

public class IrisMultiPlatImpl {
	public static Path getConfigDirectory() {
		return FabricLoader.getInstance().getConfigDir();
	}

	public static boolean isModLoadedMixinPlugin(String mod) {
		return FabricLoader.getInstance().isModLoaded(mod);
	}

	public static Path getGameDirectory() {
		return FabricLoader.getInstance().getGameDir();
	}

	public static Version getVersion() {
		return Version.parse(FabricLoader.getInstance().getModContainer(Iris.MODID).orElseThrow(IllegalStateException::new).getMetadata().getVersion().getFriendlyString(), false);
	}

    public static void drawParticles(ParticleEngine particleEngine, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LightTexture lightTexture, Camera camera, float f, Frustum cullingFrustum) {
		particleEngine.render(poseStack, bufferSource, lightTexture, camera, f);
    }

	public static boolean isDevEnv() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
}
