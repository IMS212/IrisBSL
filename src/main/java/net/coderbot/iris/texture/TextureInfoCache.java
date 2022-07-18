package net.coderbot.iris.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.nio.IntBuffer;

public class TextureInfoCache {
	public static final TextureInfoCache INSTANCE = new TextureInfoCache();

	private final Int2ObjectMap<TextureInfo> cache = new Int2ObjectOpenHashMap<>();

	private TextureInfoCache() {
	}

	public TextureInfo getInfo(int target, int id) {
		TextureInfo info = cache.get(id);
		if (info == null) {
			info = new TextureInfo(target, id);
			cache.put(id, info);
		}
		return info;
	}

	public void onTexImage2D(int target, int level, int internalformat, int width, int height) {
		if (level == 0) {
			int id = GlStateManager.getActiveTextureName();
			TextureInfo info = getInfo(target, id);
			if (info.target == -1) {
				info.target = target;
			}
			info.internalFormat = internalformat;
			info.width = width;
			info.height = height;
		}
	}

	public void onDeleteTexture(int id) {
		cache.remove(id);
	}

	public static class TextureInfo {
		private final int id;
		private int target;
		private int internalFormat = -1;
		private int width = -1;
		private int height = -1;

		private TextureInfo(int target, int id) {
			this.target = target;
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public int getTarget() {
			return target;
		}

		public int getInternalFormat() {
			if (internalFormat == -1) {
				internalFormat = fetchLevelParameter(GL20C.GL_TEXTURE_INTERNAL_FORMAT);
			}
			return internalFormat;
		}

		public int getWidth() {
			if (width == -1) {
				width = fetchLevelParameter(GL20C.GL_TEXTURE_WIDTH);
			}
			return width;
		}

		public int getHeight() {
			if (height == -1) {
				height = fetchLevelParameter(GL20C.GL_TEXTURE_HEIGHT);
			}
			return height;
		}

		private int fetchLevelParameter(int pname) {
			// Keep track of what texture was bound before
			int previousTextureBinding = getPreviousBinding();

			// Bind this texture and grab the parameter from it.
			GlStateManager._bindTexture(id);
			int parameter = GlStateManager._getTexLevelParameter(target, 0, pname);

			// Make sure to re-bind the previous texture to avoid issues.
			GlStateManager._bindTexture(previousTextureBinding);

			return parameter;
		}

		private int getPreviousBinding() {
			switch (target) {
				case GL20C.GL_TEXTURE_2D:
					return GlStateManager._getInteger(GL20C.GL_TEXTURE_BINDING_2D);
				case GL30C.GL_TEXTURE_2D_ARRAY:
					return GlStateManager._getInteger(GL30C.GL_TEXTURE_BINDING_2D_ARRAY);
				case GL30C.GL_TEXTURE_3D:
					return GlStateManager._getInteger(GL30C.GL_TEXTURE_BINDING_3D);
				default:
					throw new IllegalStateException("Unknown texture target: " + target);
			}
		}
	}
}
