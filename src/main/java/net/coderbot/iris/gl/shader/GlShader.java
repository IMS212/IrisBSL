// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.util.shaderc.Shaderc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * A compiled OpenGL shader object.
 */
public class GlShader extends GlResource {
    private static final Logger LOGGER = LogManager.getLogger(GlShader.class);

    private final String name;

    public GlShader(ShaderType type, String name, String src, ShaderConstants constants) {
    	super(createShader(type, name, src, constants));

        this.name = name;
    }

    private static int createShader(ShaderType type, String name, String src, ShaderConstants constants) {
		src = processShader(src, constants);

		int handle = GlStateManager.glCreateShader(type.id);
		ShaderWorkarounds.safeShaderSource(handle, src);
		long options     = Shaderc.shaderc_compile_options_initialize();
		long compiler = Shaderc.shaderc_compiler_initialize();
		Shaderc.shaderc_compile_options_set_target_env(options, Shaderc.shaderc_target_env_opengl, 0);

		int trueType = 0;
		switch (type.id) {
			case GL32C.GL_FRAGMENT_SHADER:
				trueType = Shaderc.shaderc_glsl_fragment_shader;
			case GL32C.GL_GEOMETRY_SHADER:
				trueType = Shaderc.shaderc_glsl_geometry_shader;
			case GL32C.GL_VERTEX_SHADER:
				trueType = Shaderc.shaderc_glsl_vertex_shader;
		}
		long output = Shaderc.shaderc_compile_into_spv(compiler, src, trueType, name, "main", options);
		Shaderc.shaderc_compile_options_release(options);
		Shaderc.shaderc_compiler_release(compiler);
		if (Shaderc.shaderc_result_get_compilation_status(output) != Shaderc.shaderc_compilation_status_success) {
			Iris.logger.error(Shaderc.shaderc_result_get_error_message(output));
		}
		GlStateManager.glCompileShader(handle);

		String log = IrisRenderSystem.getShaderInfoLog(handle);

		if (!log.isEmpty()) {
			LOGGER.warn("Shader compilation log for " + name + ": " + log);
		}

		int result = GlStateManager.glGetShaderi(handle, GL20C.GL_COMPILE_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new RuntimeException("Shader compilation failed, see log for details");
		}

		return handle;
	}

    /**
     * Adds an additional list of defines to the top of a GLSL shader file just after the version declaration. This
     * allows for ghetto shader specialization.
     */
    public static String processShader(String src, ShaderConstants constants) {
        StringBuilder builder = new StringBuilder(src.length());
        boolean patched = false;

        try (BufferedReader reader = new BufferedReader(new StringReader(src))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // Write the line out to the patched GLSL code string
                builder.append(line).append("\n");

                // Now, see if the line we just wrote declares the version
                // If we haven't already added our define declarations, add them just after the version declaration
                if (!patched && line.startsWith("#version")) {
                    for (String macro : constants.getDefineStrings()) {
                        builder.append(macro).append('\n');
                    }

                    // We did our work, don't add them again
                    patched = true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not process shader source", e);
        }

        return builder.toString();
    }

    public String getName() {
        return this.name;
    }

    public int getHandle() {
    	return this.getGlId();
	}

    @Override
	protected void destroyInternal() {
		GlStateManager.glDeleteShader(this.getGlId());
	}
}
