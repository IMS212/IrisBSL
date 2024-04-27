#version 450

layout(local_size_x = 16, local_size_y = 16, local_size_z = 1) in;

layout(location = 1) uniform mat4 Projection;
layout(location = 2) uniform float NearClip;
layout(location = 3) uniform float FarClip;

layout(binding = 0, rg32f) uniform writeonly image2D OutputMap;
uniform sampler2D DepthMap;

shared vec2 depthSamples[256];
float linearize_depth(float d,float zNear,float zFar)
{
    float z_n = 2.0 * d - 1.0;
    return 2.0 * zNear * zFar / (zFar + zNear - z_n * (zFar - zNear));
}
void main()
{
    vec4 texValue = texelFetch(DepthMap, ivec2(min(((gl_WorkGroupID.xy * uvec2(16u)) + gl_LocalInvocationID.xy), (uvec2(textureSize(DepthMap, 0)) - uvec2(1u)))), int(0u));
    float depth = texValue.x;
    float maxDepth;
    float minDepth;
    if (depth < 1.0)
    {
        depth = linearize_depth(depth, NearClip, FarClip);
        minDepth = min(minDepth, depth);
        maxDepth = max(maxDepth, depth);
    }
    else
    {
        maxDepth = 0.0;
        minDepth = 1.0;
    }
    depthSamples[gl_LocalInvocationIndex] = vec2(minDepth, maxDepth);
    groupMemoryBarrier();
    barrier();
    for (uint i = 128u; i > 0u; i = i >> 1u)
    {
        if (gl_LocalInvocationIndex < i)
        {
            uint index = gl_LocalInvocationIndex + i;
            depthSamples[gl_LocalInvocationIndex].x = min(depthSamples[gl_LocalInvocationIndex].x, depthSamples[gl_LocalInvocationIndex + i].x);
            depthSamples[gl_LocalInvocationIndex].y = max(depthSamples[gl_LocalInvocationIndex].y, depthSamples[gl_LocalInvocationIndex + i].y);
        }
        groupMemoryBarrier();
        barrier();
    }
    if (gl_LocalInvocationIndex == 0u)
    {
        imageStore(OutputMap, ivec2(gl_WorkGroupID.xy), vec2(depthSamples[0].x, depthSamples[0].y).xyyy);
    }
}

