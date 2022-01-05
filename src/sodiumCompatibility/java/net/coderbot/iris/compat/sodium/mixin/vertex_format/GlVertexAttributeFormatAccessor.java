package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.opengl.attribute.VertexAttributeFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VertexAttributeFormat.class)
public interface GlVertexAttributeFormatAccessor {
    @Invoker(value = "<init>")
    static VertexAttributeFormat createGlVertexAttributeFormat(int glId, int size) {
        throw new AssertionError("accessor failure");
    }
}
