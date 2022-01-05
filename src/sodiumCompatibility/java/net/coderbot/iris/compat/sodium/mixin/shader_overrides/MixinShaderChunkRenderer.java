package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.opengl.device.RenderDevice;
import me.jellysquid.mods.sodium.opengl.shader.Program;
import me.jellysquid.mods.sodium.render.chunk.draw.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.render.chunk.passes.ChunkRenderPass;
import me.jellysquid.mods.sodium.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.render.terrain.format.TerrainVertexType;
import me.jellysquid.mods.sodium.render.vertex.type.VertexType;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overrides shaders in {@link ShaderChunkRenderer} with our own as needed.
 */
@Mixin(ShaderChunkRenderer.class)
public class MixinShaderChunkRenderer {







}
