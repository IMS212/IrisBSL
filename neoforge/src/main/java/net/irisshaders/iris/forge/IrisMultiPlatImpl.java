package net.irisshaders.iris.forge;

import com.github.zafarkhaja.semver.Version;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class IrisMultiPlatImpl {
	public static Path getConfigDirectory() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static boolean isModLoadedMixinPlugin(String mod) {
		return FMLLoader.getLoadingModList().getModFileById(mod) != null;
	}

	public static Path getGameDirectory() {
		return FMLPaths.GAMEDIR.get();
	}

	public static Version getVersion() {
		return Version.valueOf(FMLLoader.getLoadingModList().getModFileById(Iris.MODID).getMods().get(0).getVersion().toString());
	}

	public static void drawParticles(ParticleEngine particleEngine, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LightTexture lightTexture, Camera camera, float f, Frustum cullingFrustum) {
		particleEngine.render(poseStack, bufferSource, lightTexture, camera, f, cullingFrustum);
	}

	public static boolean isDevEnv() {
		return !FMLLoader.isProduction();
	}
}
