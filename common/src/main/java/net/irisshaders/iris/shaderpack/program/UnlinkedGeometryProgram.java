package net.irisshaders.iris.shaderpack.program;

import net.irisshaders.iris.shaderpack.ProgramUsage;

public record UnlinkedGeometryProgram(ProgramUsage usage, String vertex, String fragment, String geometry, String tessControl, String tessEval, String[] targets, String depthTexture) {

}
