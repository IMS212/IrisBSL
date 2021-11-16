package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Enables usage of the extended vertex format needed by Iris.
 */
@Mixin(RenderSectionManager.class)
public class MixinSodiumWorldRenderer {
    @ModifyArg(method = "<init>", remap = false,
            at = @At(value = "FIELD", remap = false,
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/format/ChunkModelVertexFormats;DEFAULT:Lme/jellysquid/mods/sodium/client/render/chunk/format/sfp/ModelVertexType;"))
    private ChunkVertexType iris$overrideVertexType(ChunkVertexType vertexType) {
        // TODO: Don't use the extended vertex format when shaders are disabled.
        return IrisModelVertexFormats.MODEL_VERTEX_XHFP;
    }
}
