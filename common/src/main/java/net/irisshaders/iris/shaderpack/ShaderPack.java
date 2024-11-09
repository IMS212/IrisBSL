package net.irisshaders.iris.shaderpack;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public class ShaderPack {
	private static final Gson GSON = new Gson();
	private Map<NamespacedId, ShaderLevel> dimensionMap;

	public ShaderPack(Path root, ImmutableList<StringPair> environmentDefines, boolean isZip) throws IOException, IllegalStateException {
		this(root, Collections.emptyMap(), environmentDefines, isZip);
	}

	/**
	 * Reads a shader pack from the disk.
	 *
	 * @param root The path to the "shaders" directory within the shader pack. The created ShaderPack will not retain
	 *             this path in any form; once the constructor exits, all disk I/O needed to load this shader pack will
	 *             have completed, and there is no need to hold on to the path for that reason.
	 * @throws IOException if there are any IO errors during shader pack loading.
	 */
	public ShaderPack(Path root, Map<String, String> changedConfigs, ImmutableList<StringPair> environmentDefines, boolean isZip) throws IOException, IllegalStateException {
		JsonObject properties = JsonParser.parseString(Files.readString(root.resolve("pack.json"))).getAsJsonObject();

		JsonArray dimensions = properties.getAsJsonArray("dimensions");
		dimensionMap = new Object2ObjectOpenHashMap<>();

		if (dimensions != null) {
			for (JsonElement element : dimensions) {
				JsonObject dimension = element.getAsJsonObject();
				dimensionMap.put(new NamespacedId(dimension.get("id").getAsString()), new ShaderLevel(root, dimension.get("folder").getAsString(), properties));
			}
		} else {
			dimensionMap.put(DimensionId.OVERWORLD, new ShaderLevel(root, "overworld", properties));
			dimensionMap.put(DimensionId.NETHER, new ShaderLevel(root, "nether", properties));
			dimensionMap.put(DimensionId.END, new ShaderLevel(root, "end", properties));
		}
	}
}
