package net.coderbot.iris.texture.pbr;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;

public class PBRSpriteHolder {
	protected TextureAtlasSprite normalSprite;
	protected TextureAtlasSprite specularSprite;
	protected TextureAtlasSprite metalnessSprite;

	@Nullable
	public TextureAtlasSprite getNormalSprite() {
		return normalSprite;
	}

	@Nullable
	public TextureAtlasSprite getSpecularSprite() {
		return specularSprite;
	}

	@Nullable
	public TextureAtlasSprite getMetalnessSprite() {
		return metalnessSprite;
	}

	public void setNormalSprite(TextureAtlasSprite sprite) {
		normalSprite = sprite;
	}

	public void setSpecularSprite(TextureAtlasSprite sprite) {
		specularSprite = sprite;
	}

	public void setMetalnessSprite(TextureAtlasSprite sprite) {
		this.metalnessSprite = sprite;
	}

	public void close() {
		if (normalSprite != null) {
			normalSprite.close();
		}
		if (specularSprite != null) {
			specularSprite.close();
		}
		if (metalnessSprite != null) {
			metalnessSprite.close();
		}
	}
}
