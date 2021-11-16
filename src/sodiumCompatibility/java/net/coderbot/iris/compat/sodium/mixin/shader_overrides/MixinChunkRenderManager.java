package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ChunkRenderBackendExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RegionChunkRenderer.class)
public class MixinChunkRenderManager {
}
