package net.irisshaders.iris.shaderpack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.irisshaders.iris.helpers.JsonHandler;
import net.irisshaders.iris.shaderpack.program.UnlinkedGeometryProgram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public class ShaderLevel {
	private static final Gson GSON = new Gson();
	private final ShaderProperties properties;

	private final Map<ProgramUsage, UnlinkedGeometryProgram> programs = new Object2ObjectArrayMap<>();
	private final CombinationProgram combination;

	public ShaderLevel(Path root, String folder, JsonObject properties) throws IOException {
		JsonObject levelProperties;

		if (!Files.exists(root.resolve(folder).resolve("pack.json"))) {
			levelProperties = properties.deepCopy();
		} else {
			levelProperties = JsonParser.parseString(Files.readString(root.resolve(folder).resolve("pack.json"))).getAsJsonObject();

			levelProperties = JsonHandler.mergeJsonObjects(levelProperties, properties);
		}

		this.properties = GSON.fromJson(levelProperties, ShaderProperties.class);

		// Load program sources
		for (ProgramOptions program : this.properties.programOptions) {
			if (program.type.equals("geometry")) {
				String vertex = readOrNull(root, folder, program.source + ".vsh");
				String fragment = readOrNull(root, folder, program.source + ".fsh");
				String geometry = readOrNull(root, folder, program.source + ".gsh");
				String tessControl = readOrNull(root, folder, program.source + ".tcs");
				String tessEval = readOrNull(root, folder, program.source + ".tes");

				if (vertex == null || fragment == null) {
					throw new RuntimeException("Source " + program.source + " is missing a vertex or fragment shader");
				}

				for (String usage : program.usages) {
					ProgramUsage programUsage = ProgramUsage.valueOf(usage.trim().toUpperCase(Locale.ROOT));

					programs.put(programUsage, new UnlinkedGeometryProgram(programUsage, vertex, fragment, geometry, tessControl, tessEval, program.targets, program.depthTexture));
				}
			} else if (program.type.equals("composite")) {
				String vertex = readOrNull(root, folder, program.source + ".vsh");
				String fragment = readOrNull(root, folder, program.source + ".fsh");
			} else if (program.type.equals("combination")) {
				String vertex = readOrNull(root, folder, program.source + ".vsh");
				String fragment = readOrNull(root, folder, program.source + ".fsh");

				this.combination = new CombinationProgram(vertex, fragment);
			} else {
				throw new RuntimeException("Got a program (" + program.source + ") with an unknown type: " + program.type + " (it should be either a geometry, composite or combination program!)");
			}
		}
	}

	private String readOrNull(Path root, String folder, String resolve) throws IOException {
		if (resolve.startsWith("/")) {
			resolve = resolve.substring(1);
		}

		if (Files.exists(root.resolve(folder).resolve(resolve))) {
			return Files.readString(root.resolve(folder).resolve(resolve));
		}

		if (Files.exists(root.resolve(resolve))) {
			return Files.readString(root.resolve(resolve));
		}

		return null;
	}
}
