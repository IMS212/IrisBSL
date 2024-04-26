#version 450

layout(local_size_x = 16, local_size_y = 16, local_size_z = 1) in;

layout(binding = 0, rg32f) uniform writeonly image2D OutputMap;
uniform sampler2D DepthMap;

shared vec2 depthSamples[256];

void main()
{
    uvec2 uvCoord = min(((gl_WorkGroupID.xy * uvec2(16u)) + gl_LocalInvocationID.xy), (uvec2(textureSize(DepthMap, 0)) - uvec2(1u)));
    vec4 texColor = texelFetch(DepthMap, ivec2(uvCoord), int(0u));
    float depth = texColor.x;
    depthSamples[gl_LocalInvocationIndex] = vec2((depth == 0.0) ? 1.0 : depth, texelFetch(DepthMap, ivec2(uvCoord), int(0u)).y);
    barrier();
    for (uint i = 128u; i > 0u; i = i >> 1u)
    {
        if (gl_LocalInvocationIndex < i)
        {
            uint index = gl_LocalInvocationIndex + i;
            depthSamples[gl_LocalInvocationIndex].x = isnan(depthSamples[index].x) ? depthSamples[gl_LocalInvocationIndex].x : (isnan(depthSamples[gl_LocalInvocationIndex].x) ? depthSamples[index].x : min(depthSamples[gl_LocalInvocationIndex].x, depthSamples[index].x));
            depthSamples[gl_LocalInvocationIndex].y = isnan(depthSamples[index].y) ? depthSamples[gl_LocalInvocationIndex].y : (isnan(depthSamples[gl_LocalInvocationIndex].y) ? depthSamples[index].y : max(depthSamples[gl_LocalInvocationIndex].y, depthSamples[index].y));
        }
        barrier();
    }
    if (gl_LocalInvocationIndex == 0u)
    {
        imageStore(OutputMap, ivec2(gl_WorkGroupID.xy), vec2(depthSamples[0].x, depthSamples[0].y).xyyy);
    }
}
