package net.irisshaders.iris.vertices;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializerRegistry;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.sodium.IrisEntityToTerrainVertexSerializer;
import net.irisshaders.iris.vertices.sodium.IrisEntityVertex;
import net.irisshaders.iris.vertices.sodium.ModelToEntityVertexSerializer;

public class IrisVertexFormats {
	public static final VertexFormatElement ENTITY_ELEMENT;
	public static final VertexFormatElement ENTITY_ID_ELEMENT;
	public static final VertexFormatElement MID_TEXTURE_ELEMENT;
	public static final VertexFormatElement TANGENT_ELEMENT;
	public static final VertexFormatElement MID_BLOCK_ELEMENT;
	public static final VertexFormatElement VELOCITY_ELEMENT;

	public static final VertexFormat DEFAULT_ENTITY_FORMAT;

	public static final VertexFormat TERRAIN;
	private static final Byte2ObjectOpenHashMap<VertexFormat> ENTITY_CACHE = new Byte2ObjectOpenHashMap<>();
	private static final Byte2ObjectOpenHashMap<IrisEntityVertex> SODIUM_FORMAT_CACHE = new Byte2ObjectOpenHashMap<>();
	public static final VertexFormat PARTICLE;
	public static final VertexFormat GLYPH;
	public static final VertexFormat CLOUDS;

	static {
		ENTITY_ELEMENT = VertexFormatElement.register(10, 10, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.GENERIC, 2);
		ENTITY_ID_ELEMENT = VertexFormatElement.register(11, 11, VertexFormatElement.Type.USHORT, VertexFormatElement.Usage.UV, 3);
		MID_TEXTURE_ELEMENT = VertexFormatElement.register(12, 12, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);
		TANGENT_ELEMENT = VertexFormatElement.register(13, 13, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.GENERIC, 4);
		MID_BLOCK_ELEMENT = VertexFormatElement.register(14, 14, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.GENERIC, 3);
		VELOCITY_ELEMENT = VertexFormatElement.register(15, 15, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 3);

		TERRAIN = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("UV2", VertexFormatElement.UV2)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.add("mc_Entity", ENTITY_ELEMENT)
			.add("mc_midTexCoord", MID_TEXTURE_ELEMENT)
			.add("at_tangent", TANGENT_ELEMENT)
			.add("at_midBlock", MID_BLOCK_ELEMENT)
			.padding(1)
			.build();

		DEFAULT_ENTITY_FORMAT = getOrCreateEntityFormat(Byte.MAX_VALUE);

		PARTICLE = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("UV0", VertexFormatElement.UV0)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV2", VertexFormatElement.UV2)
			.add("at_velocity", VELOCITY_ELEMENT)
			.build();

		GLYPH = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("UV2", VertexFormatElement.UV2)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.add("iris_Entity", ENTITY_ID_ELEMENT)
			.add("mc_midTexCoord", MID_TEXTURE_ELEMENT)
			.add("at_tangent", TANGENT_ELEMENT)
			.padding(1)
			.build();

		CLOUDS = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.build();
	}

	public static VertexFormat getOrCreateEntityFormat(byte key) {
		return ENTITY_CACHE.computeIfAbsent(key, attributeKey -> {
			VertexFormat.Builder format = VertexFormat.builder()
				.add("Position", VertexFormatElement.POSITION)
				.add("Color", VertexFormatElement.COLOR)
				.add("UV0", VertexFormatElement.UV0)
				.add("UV1", VertexFormatElement.UV1)
				.add("UV2", VertexFormatElement.UV2)
				.add("Normal", VertexFormatElement.NORMAL)
				.padding(1)
				.add("iris_Entity", ENTITY_ID_ELEMENT);

			boolean hasTangent = (attributeKey & 1) != 0;
			boolean hasMidTexCoord = (attributeKey & 2) != 0;
			boolean hasVelocity = (attributeKey & 4) != 0;

			if (hasMidTexCoord) {
				System.out.println("Found texCoord");
				format.add("mc_midTexCoord", MID_TEXTURE_ELEMENT);
			}

			if (hasTangent) {
				System.out.println("Found tangent");

				format.add("at_tangent", TANGENT_ELEMENT);
			}

			if (hasVelocity) {
				System.out.println("Found velocity");

				format.add("at_velocity", VELOCITY_ELEMENT);
			}

			VertexFormat format2 = format.build();

			debug(format2);

			VertexSerializerRegistry.instance().registerSerializer(format2, IrisVertexFormats.TERRAIN, new IrisEntityToTerrainVertexSerializer(format2));
			VertexSerializerRegistry.instance().registerSerializer(DefaultVertexFormat.NEW_ENTITY, format2, new ModelToEntityVertexSerializer(format2));


			return format2;
		});
	}

	private static void debug(VertexFormat format) {
		Iris.logger.info("Vertex format: " + format + " with byte size " + format.getVertexSize());
		int byteIndex = 0;
		for (VertexFormatElement element : format.getElements()) {
			Iris.logger.info(element + " @ " + byteIndex + " is " + element.type() + " " + element.usage());
			byteIndex += element.byteSize();
		}
	}

	public static IrisEntityVertex getSodiumVertex(byte key, VertexFormat format) {
		return SODIUM_FORMAT_CACHE.computeIfAbsent(key, v -> new IrisEntityVertex(format));
	}
}
