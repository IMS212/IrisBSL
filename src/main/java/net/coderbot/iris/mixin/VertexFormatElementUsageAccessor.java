package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.IntConsumer;

@Mixin(VertexFormatElement.Usage.class)
public interface VertexFormatElementUsageAccessor {
	@Invoker(value = "<init>")
	static VertexFormatElement.Usage createVertexUsage(String name, int ordinal, String string2, VertexFormatElement.Usage.SetupState arg, IntConsumer intConsumer) {
		throw new AssertionError();
	}
}
