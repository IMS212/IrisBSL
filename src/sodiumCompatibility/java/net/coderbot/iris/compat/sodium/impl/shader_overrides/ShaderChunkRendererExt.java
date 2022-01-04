package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.opengl.shader.Program;

public interface ShaderChunkRendererExt {
	Program<IrisChunkShaderInterface> iris$getOverride();
}
