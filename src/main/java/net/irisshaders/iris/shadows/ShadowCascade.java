package net.irisshaders.iris.shadows;

import org.joml.Matrix4f;

public record ShadowCascade(Matrix4f projection, float splitDistance) {
}
