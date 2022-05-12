package net.coderbot.iris.compat.sodium.mixin.entity_render;

import com.mojang.math.Vector3f;
import net.coderbot.iris.compat.sodium.impl.vertex_format.VertexInterface;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelPart.Vertex.class)
public class MixinModelPartVertex implements VertexInterface {

	@Shadow
	@Final
	public Vector3f pos;

	@Unique
	private final Vector3f prevPos = new Vector3f();

	@Override
	public float getPrevX() {
		return this.prevPos.x();
	}

	@Override
	public float getPrevY() {
		return this.prevPos.y();
	}

	@Override
	public float getPrevZ() {
		return this.prevPos.z();
	}

	@Override
	public void updatePrevValues(float x, float y, float z) {
		this.prevPos.set(x, y, z);
	}
}
