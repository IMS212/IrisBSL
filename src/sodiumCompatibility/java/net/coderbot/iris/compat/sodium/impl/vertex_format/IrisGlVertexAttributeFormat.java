package net.coderbot.iris.compat.sodium.impl.vertex_format;

import me.jellysquid.mods.sodium.opengl.attribute.VertexAttributeFormat;
import net.coderbot.iris.compat.sodium.mixin.vertex_format.GlVertexAttributeFormatAccessor;
import org.lwjgl.opengl.GL20C;

public class IrisGlVertexAttributeFormat {
    public static final VertexAttributeFormat BYTE =
            GlVertexAttributeFormatAccessor.createGlVertexAttributeFormat(GL20C.GL_BYTE, 1);

	public static final VertexAttributeFormat SHORT
			= new VertexAttributeFormat(GL20C.GL_SHORT, 2);
}
