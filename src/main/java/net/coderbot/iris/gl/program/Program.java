package net.coderbot.iris.gl.program;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.ProgramManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.uniform.UBOCreator;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL43C;

public final class Program extends GlResource {
	private final ProgramUniforms uniforms;
	private final ProgramSamplers samplers;
	private final ProgramImages images;

	Program(int handle, ProgramUniforms uniforms, ProgramSamplers samplers, ProgramImages images) {
		super(handle);

		int index = GL43C.glGetUniformBlockIndex(handle, "CommonUniforms");
		if (index != GL43C.GL_INVALID_INDEX) {
			GL43C.glUniformBlockBinding(handle, index, 1);
		}

		this.uniforms = uniforms;
		this.samplers = samplers;
		this.images = images;
	}

	public void use() {
		ProgramManager.glUseProgram(getGlId());

		uniforms.update();
		samplers.update();
		images.update();
	}

	public static void unbind() {
		ProgramUniforms.clearActiveUniforms();
		ProgramManager.glUseProgram(0);
	}

	public void destroyInternal() {
		GlStateManager.glDeleteProgram(getGlId());
	}

	/**
	 * @return the OpenGL ID of this program.
	 * @deprecated this should be encapsulated eventually
	 */
	@Deprecated
	public int getProgramId() {
		return getGlId();
	}

	public int getActiveImages() {
		return images.getActiveImages();
	}
}
