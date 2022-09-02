package net.coderbot.iris.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.texture.TextureInfoCache;
import net.coderbot.iris.texture.TextureTracker;
import net.coderbot.iris.vendored.joml.Vector3i;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.EXTShaderImageLoadStore;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL40C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * This class is responsible for abstracting calls to OpenGL and asserting that calls are run on the render thread.
 */
public class IrisRenderSystem {
	private static Matrix4f backupProjection;

	private static boolean lockParameters;

	public static void lockParameters(boolean locked) {
		lockParameters = locked;
	}

	public static boolean areParametersLocked() {
		return lockParameters;
	}

	public static void getIntegerv(int pname, int[] params) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glGetIntegerv(pname, params);
	}

	public static void getFloatv(int pname, float[] params) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glGetFloatv(pname, params);
	}

	public static void generateMipmaps(int mipmapTarget) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glGenerateMipmap(mipmapTarget);
	}

	public static void bindAttributeLocation(int program, int index, CharSequence name) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glBindAttribLocation(program, index, name);
	}

	public static void texImage1D(int id, int target, int level, int internalformat, int width, int border, int format, int type, @Nullable ByteBuffer pixels) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
		TextureInfoCache.INSTANCE.onTexImage(id, TextureType.TEXTURE_1D, level, internalformat, width, -1, -1, border, format, type, pixels);
	}

	public static void texImage2D(int id, int target, int level, int internalformat, int width, int height, int border, int format, int type, @Nullable ByteBuffer pixels) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
		TextureInfoCache.INSTANCE.onTexImage(id, TextureType.TEXTURE_2D, level, internalformat, width, height, -1, border, format, type, pixels);
	}

	public static void texImage3D(int id, int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, @Nullable ByteBuffer pixels) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glBindTexture(GL32C.GL_TEXTURE_3D, id);
		GL32C.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
		TextureInfoCache.INSTANCE.onTexImage(id, TextureType.TEXTURE_3D, level, internalformat, width, height, depth, border, format, type, pixels);
	}

	public static void uniformMatrix4fv(int location, boolean transpose, FloatBuffer matrix) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniformMatrix4fv(location, transpose, matrix);
	}

	public static void copyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
	}

	public static void uniform1f(int location, float v0) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform1f(location, v0);
	}

	public static void uniform2f(int location, float v0, float v1) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform2f(location, v0, v1);
	}

	public static void uniform2i(int location, int v0, int v1) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform2i(location, v0, v1);
	}

	public static void uniform3f(int location, float v0, float v1, float v2) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform3f(location, v0, v1, v2);
	}

	public static void uniform4f(int location, float v0, float v1, float v2, float v3) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform4f(location, v0, v1, v2, v3);
	}

	public static void uniform4i(int location, int v0, int v1, int v2, int v3) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniform4i(location, v0, v1, v2, v3);
	}

	public static void texParameteriv(int target, int pname, int[] params) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glTexParameteriv(target, pname, params);
	}

	public static String getProgramInfoLog(int program) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetProgramInfoLog(program);
	}

	public static String getShaderInfoLog(int shader) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetShaderInfoLog(shader);
	}

	public static void drawBuffers(int[] buffers) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glDrawBuffers(buffers);
	}

	public static void readBuffer(int buffer) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glReadBuffer(buffer);
	}

	public static String getActiveUniform(int program, int index, int size, IntBuffer type, IntBuffer name) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetActiveUniform(program, index, size, type, name);
	}

	public static void readPixels(int x, int y, int width, int height, int format, int type, float[] pixels) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glReadPixels(x, y, width, height, format, type, pixels);
	}

	public static void bufferData(int target, float[] data, int usage) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glBufferData(target, data, usage);
	}

	public static void vertexAttrib4f(int index, float v0, float v1, float v2, float v3) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glVertexAttrib4f(index, v0, v1, v2, v3);
	}

	public static void detachShader(int program, int shader) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glDetachShader(program, shader);
	}

	public static int getTexParameteri(int target, int pname) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetTexParameteri(target, pname);
	}

	public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
		RenderSystem.assertOnRenderThreadOrInit();
		if (GL.getCapabilities().OpenGL42) {
			GL42C.glBindImageTexture(unit, texture, level, layered, layer, access, format);
		} else {
			EXTShaderImageLoadStore.glBindImageTextureEXT(unit, texture, level, layered, layer, access, format);
		}
	}

	public static int getMaxImageUnits() {
		if (GL.getCapabilities().OpenGL42) {
			return GlStateManager._getInteger(GL42C.GL_MAX_IMAGE_UNITS);
		} else if (GL.getCapabilities().GL_EXT_shader_image_load_store) {
			return GlStateManager._getInteger(EXTShaderImageLoadStore.GL_MAX_IMAGE_UNITS_EXT);
		} else {
			return 0;
		}
	}

	public static void bindTexture(int target, int id) {
		RenderSystem.assertOnRenderThreadOrInit();

		GL32C.glBindTexture(target, id);
	}

	public static boolean supportsBufferBlending() {
		return GL.getCapabilities().GL_ARB_draw_buffers_blend || GL.getCapabilities().OpenGL40;
	}

	public static void disableBufferBlend(int buffer) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL30C.glDisablei(GL30C.GL_BLEND, buffer);
	}

	public static void enableBufferBlend(int buffer) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL30C.glEnablei(GL30C.GL_BLEND, buffer);
	}

	public static void blendFuncSeparatei(int buffer, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL40C.glBlendFuncSeparatei(buffer, srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	public static void getProgramiv(int program, int value, int[] storage) {
		GL30C.glGetProgramiv(program, value, storage);
	}

	public static void dispatchCompute(int workX, int workY, int workZ) {
		GL43C.glDispatchCompute(workX, workY, workZ);
	}

	public static void dispatchCompute(Vector3i workGroups) {
		GL43C.glDispatchCompute(workGroups.x, workGroups.y, workGroups.z);
	}

	public static void memoryBarrier(int barriers) {
		GL43C.glMemoryBarrier(barriers);
	}

	// These functions are deprecated and unavailable in the core profile.
	public static String getStringi(int glExtensions, int index) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetStringi(glExtensions, index);
	}

	public static int getUniformBlockIndex(int program, String uniformBlockName) {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL32C.glGetUniformBlockIndex(program, uniformBlockName);
	}

	public static void uniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL32C.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	public static void setShadowProjection(Matrix4f shadowProjection) {
		backupProjection = RenderSystem.getProjectionMatrix();
		RenderSystem.setProjectionMatrix(shadowProjection);
	}

	public static void restorePlayerProjection() {
		RenderSystem.setProjectionMatrix(backupProjection);
		backupProjection = null;
	}
}
