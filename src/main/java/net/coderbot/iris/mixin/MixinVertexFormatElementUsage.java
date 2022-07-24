package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisShaderTypes;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vertices.IrisVertexUsages;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VertexFormatElement.Usage.class)
public class MixinVertexFormatElementUsage {
	@SuppressWarnings("target")
	@Shadow(remap = false)
	@Final
	@Mutable
	private static VertexFormatElement.Usage[] $VALUES;

	static {
		int baseOrdinal = $VALUES.length;

		IrisVertexUsages.INTEGER
				= VertexFormatElementUsageAccessor.createVertexUsage("INTEGER", baseOrdinal, "Integer", (i, j, k, l, m) -> {
			GlStateManager._enableVertexAttribArray(m);
			IrisRenderSystem.vertexAttribIPointer(m, i, j, k, l);
		}, GlStateManager::_disableVertexAttribArray);

		$VALUES = ArrayUtils.addAll($VALUES, IrisVertexUsages.INTEGER);
	}
}
