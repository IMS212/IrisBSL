package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.coderbot.iris.texunits.TextureUnit;
import org.lwjgl.opengl.GL20C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.FloatBuffer;

/**
 * Modifies {@link ChunkShaderInterface} to handle cases where uniforms might not be present in the target program, and to use
 * the correct texture units for terrain and lightmap textures.
 */
@Mixin(ChunkShaderInterface.class)
public class MixinChunkProgram {
    @ModifyConstant(method = "setup", remap = false, constant = @Constant(intValue = 0))
    private int iris$replaceTerrainTextureUnit(int unit) {
        return TextureUnit.TERRAIN.getSamplerId();
    }

    @ModifyConstant(method = "setup", remap = false, constant = @Constant(intValue = 2))
    private int iris$replaceLightmapTextureUnit(int unit) {
        return TextureUnit.LIGHTMAP.getSamplerId();
    }

}
