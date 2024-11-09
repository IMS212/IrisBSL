package net.irisshaders.iris.shaderpack;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.Serializable;
import java.lang.reflect.Type;

public interface TextureOptions extends Serializable {
    String getName();
    @Nullable String getImageName();
    InternalTextureFormat getFormat();
    Vector2f getSize();
    boolean shouldClear();
    Vector4f getClearColor();

    public class OptionsDeserializer implements JsonDeserializer<TextureOptions> {
        @Override
        public TextureOptions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return null;
        }
    }
}
