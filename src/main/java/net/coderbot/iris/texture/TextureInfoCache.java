package net.coderbot.iris.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.texture.TextureType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

import java.nio.Buffer;

public class TextureInfoCache {
	public static final TextureInfoCache INSTANCE = new TextureInfoCache();

	private final Int2ObjectMap<TextureInfo> cache = new Int2ObjectOpenHashMap<>();

	private TextureInfoCache() {
	}

	public TextureInfo getInfo(int id) {
		TextureInfo info = cache.get(id);
		if (info == null) {
			info = new TextureInfo(TextureType.TEXTURE_2D, id);
			cache.put(id, info);
		}
		return info;
	}

	public void onTexImage(int id, TextureType target, int level, int internalformat, int width, int height, int depth, int border,
						   int format, int type, @Nullable Buffer pixels) {
		if (level == 0) {
			TextureInfo info = getInfo(id);
			info.type = target;
			info.internalFormat = internalformat;
			info.width = width;
			info.height = height;
			info.depth = depth;
		}
	}

	public void onDeleteTexture(int id) {
		cache.remove(id);
	}

	public static class TextureInfo {
		private final int id;
		private TextureType type;
		private int internalFormat = -1;
		private int width = -1;
		private int height = -1;
		private int depth = -1;

		private TextureInfo(TextureType type, int id) {
			this.type = type;
			this.id = id;
		}

		public int getId() {
			return id;
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
			if (height == -1 && type != TextureType.TEXTURE_1D) {
				height = fetchLevelParameter(GL20C.GL_TEXTURE_HEIGHT);
			}
			return height;
		}

		public int getDepth() {
			if (depth == -1 && type == TextureType.TEXTURE_3D) {
				depth = fetchLevelParameter(GL20C.GL_TEXTURE_DEPTH);
			}
			return depth;
		}

		private int fetchLevelParameter(int pname) {
			// Keep track of what texture was bound before
			int previousTextureBinding = GlStateManager._getInteger(getBinding());

			// Bind this texture and grab the parameter from it.
			IrisRenderSystem.bindTexture(getType().getGlType(), id);
			int parameter = GlStateManager._getTexLevelParameter(getType().getGlType(), 0, pname);

			// Make sure to re-bind the previous texture to avoid issues.
			IrisRenderSystem.bindTexture(getType().getGlType(), previousTextureBinding);

			return parameter;
		}

		public int getBinding() {
			switch (getType()) {
				case TEXTURE_1D:
					return GL20C.GL_TEXTURE_BINDING_1D;
				case TEXTURE_2D:
					return GL20C.GL_TEXTURE_BINDING_2D;
				case TEXTURE_3D:
					return GL20C.GL_TEXTURE_BINDING_3D;
				default:
					throw new IllegalStateException("Unsupported texture type: " + getType());
			}
		}

		public TextureType getType() {
			return type;
		}
	}
}
