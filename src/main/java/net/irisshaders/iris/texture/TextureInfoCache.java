package net.irisshaders.iris.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;
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

	public void onTexImage2D(int target, int level, int internalformat, int width, int height, int border,
							 int format, int type, @Nullable IntBuffer pixels) {
		if (level == 0) {
			int id = GlStateManagerAccessor.getTEXTURES()[GlStateManagerAccessor.getActiveTexture()].binding;
			TextureInfo info = getInfo(target, id);
			info.internalFormat = internalformat;
			info.width = width;
			info.height = height;
		}
	}

	public void onTexImage3D(int target, int id, int level, int internalformat, int width, int height, int depth, int border,
							 int format, int type) {
		if (level == 0) {
			TextureInfo info = getInfo(target, id);
			info.internalFormat = internalformat;
			info.width = width;
			info.height = height;
		}
	}

	public void onDeleteTexture(int id) {
		cache.remove(id);
	}

	public static class TextureInfo {
		private final int target;
		private final int id;
		private int internalFormat = -1;
		private int width = -1;
		private int height = -1;
		private int depth = -1;

		private TextureInfo(int target, int id) {
			this.target = target;
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
			if (height == -1) {
				height = fetchLevelParameter(GL20C.GL_TEXTURE_HEIGHT);
			}
			return height;
		}

		public int getDepth() {
			if (depth == -1) {
				depth = fetchLevelParameter(GL20C.GL_TEXTURE_DEPTH);
			}
			return depth;
		}

		private int fetchLevelParameter(int pname) {
			// Keep track of what texture was bound before
			int previousTextureBinding = GlStateManager._getInteger(getBindingTarget(target));

			// Bind this texture and grab the parameter from it.
			GL20C.glBindTexture(target, id);
			int parameter = GlStateManager._getTexLevelParameter(target, 0, pname);

			// Make sure to re-bind the previous texture to avoid issues.
			GL20C.glBindTexture(target, previousTextureBinding);

			return parameter;
		}

		private int getBindingTarget(int target) {
			if (target == GL20C.GL_TEXTURE_2D) {
				return GL30C.GL_TEXTURE_BINDING_2D;
			} else if (target == GL30C.GL_TEXTURE_2D_ARRAY) {
				return GL30C.GL_TEXTURE_BINDING_2D_ARRAY;
			} else {
				throw new IllegalStateException("idk " + target);
			}
		}
	}
}
