package net.irisshaders.iris.platform;

import net.irisshaders.iris.Iris;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class IrisForgeHelpers implements IrisPlatformHelpers {
	@Override
	public boolean isModLoaded(String modId) {
		return LoadingModList.get().getModFileById(modId) != null;
	}

	@Override
	public String getVersion() {
		return LoadingModList.get().getModFileById(Iris.MODID).versionString();
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	public Path getGameDir() {
		return FMLPaths.GAMEDIR.get();
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public int compareVersions(String currentVersion, String semanticVersion) throws Exception {
		return new DefaultArtifactVersion(currentVersion).compareTo(new DefaultArtifactVersion(semanticVersion));
	}

	@Override
	public KeyMapping registerKeyBinding(KeyMapping keyMapping) {
		IrisForgeMod.KEYLIST.add(keyMapping);
		return keyMapping;
	}

	@Override
	public void onMixinConfigLoad() {
		if (LoadingModList.get().getModFileById("embeddium") != null) {
			AtomicReference<String> modCheck = new AtomicReference<>();

			for (ModFileInfo modFile : LoadingModList.get().getModFiles()) {
				modFile.getConfig().getConfigElement("compatibleIrisVersion").ifPresent(override -> {
					modCheck.set((String) override);
				});

				if (modCheck.get() != null) {
					System.out.println("Mod " + modFile.getFile().getFileName() + " provided an override for Iris of " + modCheck.get());
					break;
				}
			}

			boolean compatibleEmbeddium = modCheck.get() != null && IrisForgeHelpers.INSTANCE.getVersion().contains(modCheck.get());

			if (!compatibleEmbeddium) {
				if (modCheck.get() == null) {
					ModLoader.addLoadingIssue(ModLoadingIssue.error("Iris is not compatible with this version of Embeddium.\nPlease use Sodium."));
				} else {
					ModLoader.addLoadingIssue(ModLoadingIssue.error("Iris is not officially compatible with Embeddium, and this version of Embeddium has only added support for Iris " + modCheck.get() + "." +
						"\nPlease use Sodium or an alternate version of Embeddium."));
				}
			}
		}
	}

	@Override
	public boolean shouldShowTainted() {
		return LoadingModList.get().getModFileById("embeddium") != null;
	}
}
