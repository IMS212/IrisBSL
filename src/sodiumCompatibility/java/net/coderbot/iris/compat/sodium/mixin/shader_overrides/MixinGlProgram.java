package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.platform.GlStateManager;
import me.jellysquid.mods.sodium.opengl.ManagedObject;
import me.jellysquid.mods.sodium.opengl.shader.Program;
import me.jellysquid.mods.sodium.opengl.shader.ProgramImpl;
import me.jellysquid.mods.sodium.opengl.shader.ShaderBindingContext;
import me.jellysquid.mods.sodium.opengl.shader.uniform.Uniform;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformBlock;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformFactory;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderBindingContextExt;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.IntFunction;

@Mixin(targets = "me.jellysquid.mods.sodium.opengl.shader.ProgramImpl.BindingContext")
public abstract class MixinGlProgram implements ShaderBindingContextExt, ShaderBindingContext {

	@Shadow(remap = false)
	@Final
	private int handle;

	public UniformBlock bindUniformBlockIfPresent(String name, int bindingPoint) {
		int index = IrisRenderSystem.getUniformBlockIndex(this.handle, name);
		if (index < 0) {
			return null;
		} else {
			IrisRenderSystem.uniformBlockBinding(this.handle, index, bindingPoint);
			return new UniformBlock(bindingPoint);
		}
	}
}
