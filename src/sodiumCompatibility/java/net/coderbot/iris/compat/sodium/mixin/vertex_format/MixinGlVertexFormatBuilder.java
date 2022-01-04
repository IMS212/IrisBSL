package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttribute;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.opengl.attribute.VertexAttribute;
import me.jellysquid.mods.sodium.opengl.attribute.VertexAttributeFormat;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.EnumMap;

@Mixin(VertexFormat.class)
public class MixinGlVertexFormatBuilder {
    private static final VertexAttribute EMPTY
            = new VertexAttribute(VertexAttributeFormat.FLOAT, 0, false, 0, 0);

    @Redirect(method = "build",
            at = @At(value = "INVOKE",
                    target = "g"),
            remap = false)
    private Object iris$suppressMissingAttributes(EnumMap<?, ?> map, Object key) {
        Object value = map.get(key);

        if (value == null) {
            if (key == IrisChunkMeshAttributes.NORMAL || key == IrisChunkMeshAttributes.TANGENT
                    || key == IrisChunkMeshAttributes.MID_TEX_COORD || key == IrisChunkMeshAttributes.BLOCK_ID) {
                // Missing these attributes is acceptable and will be handled properly.
                return EMPTY;
            }
        }

        return value;
    }
}
