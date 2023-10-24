package net.coderbot.iris.compat.sodium.mixin.copyEntity;

import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(ModelPart.Cube.class)
public class CuboidMixin implements ModelCuboidAccessor {
	@Shadow
	@Final
	@Mutable
	private ModelPart.Polygon[] polygons;
	@Unique
    private ModelCuboid sodium$cuboid;

    // Inject at the start of the function, so we don't capture modified locals
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Set;size()I"))
    private int onInit(Set<?> instance, int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight, Set<Direction> renderDirections) {
        this.sodium$cuboid = new ModelCuboid(u, v, x, y, z, sizeX, sizeY, sizeZ, extraX, extraY, extraZ, mirror, textureWidth, textureHeight, renderDirections);
		return instance.size();
	}

    @Override
    public ModelCuboid sodium$copy() {
        return this.sodium$cuboid;
    }
}
