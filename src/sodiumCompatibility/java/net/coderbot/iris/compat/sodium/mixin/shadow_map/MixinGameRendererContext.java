package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows the Iris shadow map projection matrix to be used during shadow rendering instead of the player view's
 * projection matrix.
 */
