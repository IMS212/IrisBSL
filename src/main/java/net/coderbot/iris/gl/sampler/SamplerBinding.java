package net.coderbot.iris.gl.sampler;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.texture.TextureType;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL45C;

import java.util.function.IntSupplier;

public class SamplerBinding {
	private final int textureUnit;
	private final IntSupplier texture;
	private final TextureType type;
	private final TextureType[] typesMinusCurrent;

	public SamplerBinding(int textureUnit, TextureType type, IntSupplier texture) {
		this.textureUnit = textureUnit;
		this.type = type;
		this.texture = texture;
		TextureType[] typesMinusCurrent = TextureType.values();
		this.typesMinusCurrent = ArrayUtils.removeAllOccurences(typesMinusCurrent, type);
	}

	public void update() {
		RenderSystem.activeTexture(GL20C.GL_TEXTURE0 + textureUnit);
		IrisRenderSystem.bindTexture(type.getGlType(), texture.getAsInt());
		// Unbind that binding for other texture types, to avoid issues.
		for (TextureType otherType : this.typesMinusCurrent) {
			IrisRenderSystem.bindTexture(otherType.getGlType(), 0);
		}
	}
}
