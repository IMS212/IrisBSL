package net.coderbot.iris.sodiumglue.impl;

public interface IrisFrustumExtended {
    default boolean preAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return true;
    }
}
