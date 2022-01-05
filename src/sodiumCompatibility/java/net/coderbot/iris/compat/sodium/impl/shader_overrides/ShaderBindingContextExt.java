package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.opengl.shader.uniform.Uniform;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformBlock;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformFactory;

import java.util.function.IntFunction;

public interface ShaderBindingContextExt {
	UniformBlock bindUniformBlockIfPresent(String var1, int var2);
}
