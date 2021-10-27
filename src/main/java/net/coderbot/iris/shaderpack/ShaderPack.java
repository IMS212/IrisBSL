package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;
import net.coderbot.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.coderbot.iris.shaderpack.texture.TextureFilteringData;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

public class ShaderPack {
	private final Path root;
	private final ProgramSet base;
	@Nullable
	private final ProgramSet overworld;
	private final ProgramSet nether;
	private final ProgramSet end;

	private final IdMap idMap;
	private final LanguageMap languageMap;
	private final CustomTextureData customNoiseTexture;

	public final CustomUniforms.Builder customUniforms;

	/**
	 * Reads a shader pack from the disk.
	 *
	 * @param root The path to the "shaders" directory within the shader pack
	 * @throws IOException
	 */
	public ShaderPack(Path root) throws IOException {
		// A null path is not allowed.
		Objects.requireNonNull(root);

		this.root = root;

		ShaderProperties shaderProperties = loadProperties(root, "shaders.properties")
				.map(ShaderProperties::new)
				.orElseGet(ShaderProperties::empty);

		this.base = new ProgramSet(root, root, shaderProperties, this);
		this.overworld = loadOverrides(root, "world0", shaderProperties, this);
		this.nether = loadOverrides(root, "world-1", shaderProperties, this);
		this.end = loadOverrides(root, "world1", shaderProperties, this);

		this.idMap = new IdMap(root);
		this.languageMap = new LanguageMap(root.resolve("lang"));

		customNoiseTexture = shaderProperties.getNoiseTexturePath().map(path -> {
			try {
				return readTexture(path);
			} catch (IOException | UnsupportedOperationException e) {
				Iris.logger.error("Unable to read the custom noise texture at " + path, e);

				return null;
			}
		}).orElse(null);
		this.customUniforms = shaderProperties.customUniforms;
	}

	@Nullable
	private static ProgramSet loadOverrides(Path root, String subfolder, ShaderProperties shaderProperties, ShaderPack pack) throws IOException {
		Path sub = root.resolve(subfolder);

		if (Files.exists(sub)) {
			return new ProgramSet(sub, root, shaderProperties, pack);
		}

		return null;
	}

	// TODO: Copy-paste from IdMap, find a way to deduplicate this
	private static Optional<Properties> loadProperties(Path shaderPath, String name) {
		Properties properties = new Properties();

		try {
			// NB: shaders.properties is specified to be encoded with ISO-8859-1 by OptiFine,
			//     so we don't need to do the UTF-8 workaround here.
			properties.load(Files.newInputStream(shaderPath.resolve(name)));
		} catch (IOException e) {
			Iris.logger.debug("An " + name + " file was not found in the current shaderpack");

			return Optional.empty();
		}

		return Optional.of(properties);
	}

	public CustomTextureData readTexture(String path) throws IOException, UnsupportedOperationException {
		CustomTextureData customTextureData;
		if (path.contains(":") && ResourceLocation.isValidResourceLocation(path)) {
			ResourceLocation textureIdentifier = new ResourceLocation(path);
			byte[] content = IOUtils.toByteArray(Minecraft.getInstance().getResourceManager().getResource(textureIdentifier).getInputStream());
			customTextureData = new CustomTextureData.PngData(new TextureFilteringData(true, false), content);
			//throw new UnsupportedOperationException("Identifier-based custom textures are not yet supported");
		} else {
			// TODO: Make sure the resulting path is within the shaderpack?
			if (path.startsWith("/")) {
				// NB: This does not guarantee the resulting path is in the shaderpack as a double slash could be used,
				// this just fixes shaderpacks like Continuum 2.0.4 that use a leading slash in texture paths
				path = path.substring(1);
			}
			byte[] content = Files.readAllBytes(root.resolve(path));
			// TODO: Read the blur / clamp data from the shaderpack...
			customTextureData = new CustomTextureData.PngData(new TextureFilteringData(true, false), content);
		}
		return customTextureData;
	}

	public ProgramSet getProgramSet(DimensionId dimension) {
		ProgramSet overrides;

		switch (dimension) {
			case OVERWORLD:
				overrides = overworld;
				break;
			case NETHER:
				overrides = nether;
				break;
			case END:
				overrides = end;
				break;
			default:
				throw new IllegalArgumentException("Unknown dimension " + dimension);
		}

		return ProgramSet.merged(base, overrides);
	}

	public IdMap getIdMap() {
		return idMap;
	}

	public Optional<CustomTextureData> getCustomNoiseTexture() {
		return Optional.ofNullable(customNoiseTexture);
	}

	public LanguageMap getLanguageMap() {
		return languageMap;
	}
}
