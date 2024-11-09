package net.irisshaders.iris.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonHandler {
	public static JsonObject mergeJsonObjects(JsonObject dest, JsonObject addition) {
		JsonObject destination = dest.deepCopy();

		addition.entrySet().forEach(entry -> {
			String key = entry.getKey();

			if (destination.has(key)) {
				JsonElement element1 = addition.get(key);
				JsonElement element2 = entry.getValue();

				if (element1.isJsonObject() && element2.isJsonObject()) {
					destination.add(key, mergeJsonObjects(element1.getAsJsonObject(), element2.getAsJsonObject()));
				} else if (element1.isJsonArray() && element2.isJsonArray()) {
					JsonArray mergedArray = new JsonArray();
					for (JsonElement elem : element1.getAsJsonArray()) {
						mergedArray.add(elem);
					}
					for (JsonElement elem : element2.getAsJsonArray()) {
						if (!mergedArray.contains(elem)) { // add only unique elements
							mergedArray.add(elem);
						}
					}
					destination.add(key, mergedArray);
				} else {
					destination.add(key, element2);
				}
			} else {
				destination.add(key, addition.get(key));
			}
		});

		return destination;
	}
}
