package net.coderbot.iris.pipeline;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.GlTexture;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.gl.texture.TextureUploadHelper;
import net.coderbot.iris.mixin.LightTextureAccessor;
import net.coderbot.iris.rendertarget.NativeImageBackedCustomTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedNoiseTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedSingleColorTexture;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;

public class CustomTextureManager {
	private final EnumMap<TextureStage, Object2ObjectMap<String, IntSupplier>> customTextureIdMap = new EnumMap<>(TextureStage.class);
	private final IntSupplier noise;
	private final NativeImageBackedSingleColorTexture normals;
	private final NativeImageBackedSingleColorTexture specular;

	/**
	 * List of all OpenGL texture objects owned by this CustomTextureManager that need to be deleted in order to avoid
	 * leaks.
	 * Make sure any textures added to this list call releaseId from the close method.
	 */
	private final List<AbstractTexture> ownedTextures = new ArrayList<>();
	private final List<GlTexture> ownedRawTextures = new ArrayList<>();

	public CustomTextureManager(PackDirectives packDirectives,
								EnumMap<TextureStage, Object2ObjectMap<String, CustomTextureData>> customTextureDataMap,
								Optional<CustomTextureData> customNoiseTextureData) {
		TextureUploadHelper.resetTextureUploadState();

		customTextureDataMap.forEach((textureStage, customTextureStageDataMap) -> {
			Object2ObjectMap<String, IntSupplier> customTextureIds = new Object2ObjectOpenHashMap<>();

			customTextureStageDataMap.forEach((samplerName, textureData) -> {
				try {
					customTextureIds.put(samplerName, createCustomTexture(textureData));
				} catch (IOException | ResourceLocationException e) {
					Iris.logger.error("Unable to parse the image data for the custom texture on stage "
							+ textureStage + ", sampler " + samplerName, e);
				}
			});

			customTextureIdMap.put(textureStage, customTextureIds);
		});

		noise = customNoiseTextureData.flatMap(textureData -> {
			try {
				return Optional.of(createCustomTexture(textureData));
			} catch (IOException | ResourceLocationException e) {
				Iris.logger.error("Unable to parse the image data for the custom noise texture", e);

				return Optional.empty();
			}
		}).orElseGet(() -> {
			final int noiseTextureResolution = packDirectives.getNoiseTextureResolution();

			AbstractTexture texture = new NativeImageBackedNoiseTexture(noiseTextureResolution);
			ownedTextures.add(texture);

			return texture::getId;
		});

		// Create some placeholder PBR textures for now
		normals = new NativeImageBackedSingleColorTexture(127, 127, 255, 255);
		specular = new NativeImageBackedSingleColorTexture(0, 0, 0, 0);

		ownedTextures.add(normals);
		ownedTextures.add(specular);
	}

	private IntSupplier createCustomTexture(CustomTextureData textureData) throws IOException, ResourceLocationException {
		if (textureData instanceof CustomTextureData.PngData) {
			AbstractTexture texture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);
			ownedTextures.add(texture);

			return texture::getId;
		} else if (textureData instanceof CustomTextureData.LightmapMarker) {
			// Special code path for the light texture. While shader packs hardcode the primary light texture, it's
			// possible that a mod will create a different light texture, so this code path is robust to that.
			return () ->
				((LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture())
					.getLightTexture().getId();
		} else if (textureData instanceof CustomTextureData.RawData1D) {
			CustomTextureData.RawData1D rawData1D = (CustomTextureData.RawData1D) textureData;
			GlTexture texture = new GlTexture(TextureType.TEXTURE_1D, rawData1D.getFilteringData().shouldBlur(), rawData1D.getFilteringData().shouldClamp(), rawData1D.getSizeX(), 0, 0, rawData1D.getInternalFormat(), rawData1D.getPixelFormat(), rawData1D.getPixelType(), rawData1D.getContent());
			ownedRawTextures.add(texture);

			return texture::getId;
		} else if (textureData instanceof CustomTextureData.RawData2D) {
			CustomTextureData.RawData2D rawData2D = (CustomTextureData.RawData2D) textureData;
			GlTexture texture = new GlTexture(TextureType.TEXTURE_2D, rawData2D.getFilteringData().shouldBlur(), rawData2D.getFilteringData().shouldClamp(), rawData2D.getSizeX(), rawData2D.getSizeY(), 0, rawData2D.getInternalFormat(), rawData2D.getPixelFormat(), rawData2D.getPixelType(), rawData2D.getContent());
			ownedRawTextures.add(texture);

			return texture::getId;
		} else if (textureData instanceof CustomTextureData.RawData3D) {
			CustomTextureData.RawData3D rawData3D = (CustomTextureData.RawData3D) textureData;
			GlTexture texture = new GlTexture(TextureType.TEXTURE_3D, rawData3D.getFilteringData().shouldBlur(), rawData3D.getFilteringData().shouldClamp(), rawData3D.getSizeX(), rawData3D.getSizeY(), rawData3D.getSizeZ(), rawData3D.getInternalFormat(), rawData3D.getPixelFormat(), rawData3D.getPixelType(), rawData3D.getContent());
			ownedRawTextures.add(texture);

			return texture::getId;
		} else if (textureData instanceof CustomTextureData.ResourceData) {
			CustomTextureData.ResourceData resourceData = (CustomTextureData.ResourceData) textureData;
			String namespace = resourceData.getNamespace();
			String location = resourceData.getLocation();

			ResourceLocation textureLocation = new ResourceLocation(namespace, location);
			TextureManager textureManager = Minecraft.getInstance().getTextureManager();

			// NB: We have to re-query the TextureManager for the texture object every time. This is because the
			//     AbstractTexture object could be removed / deleted from the TextureManager on resource reloads,
			//     and we could end up holding on to a deleted texture unless we added special code to handle resource
			//     reloads. Re-fetching the texture from the TextureManager every time is the most robust approach for
			//     now.
			return () -> {
				AbstractTexture texture = textureManager.getTexture(textureLocation);

				// TODO: Should we give something else if the texture isn't there? This will need some thought
				return texture != null ? texture.getId() : MissingTextureAtlasSprite.getTexture().getId();
			};
		} else {
			throw new IllegalArgumentException("Unable to handle custom texture data " + textureData);
		}
	}

	public EnumMap<TextureStage, Object2ObjectMap<String, IntSupplier>> getCustomTextureIdMap() {
		return customTextureIdMap;
	}

	public Object2ObjectMap<String, IntSupplier> getCustomTextureIdMap(TextureStage stage) {
		return customTextureIdMap.getOrDefault(stage, Object2ObjectMaps.emptyMap());
	}

	public IntSupplier getNoiseTexture() {
		return noise;
	}

	public NativeImageBackedSingleColorTexture getNormals() {
		return normals;
	}

	public NativeImageBackedSingleColorTexture getSpecular() {
		return specular;
	}

	public void destroy() {
		ownedTextures.forEach(AbstractTexture::close);
		ownedRawTextures.forEach(GlTexture::destroy);
	}
}
