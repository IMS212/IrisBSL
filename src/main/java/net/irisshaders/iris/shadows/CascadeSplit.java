package net.irisshaders.iris.shadows;

import org.joml.Matrix4f;

public record CascadeSplit(Matrix4f modelView, Matrix4f projection, float splitDistance) {
}
