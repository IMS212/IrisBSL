package net.coderbot.iris.compat.sodium.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer$BufferTarget")
public interface BufferTargetInterface {
}
