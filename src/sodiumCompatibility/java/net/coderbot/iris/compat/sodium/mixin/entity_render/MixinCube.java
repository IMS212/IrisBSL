package net.coderbot.iris.compat.sodium.mixin.entity_render;

import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelPart.Cube.class)
public class MixinCube implements ModelCuboidAccessor {
	@Shadow
	@Final
	private ModelPart.Polygon[] polygons;

	@Override
	public ModelPart.Polygon[] getQuads() {
		return this.polygons;
	}
}
