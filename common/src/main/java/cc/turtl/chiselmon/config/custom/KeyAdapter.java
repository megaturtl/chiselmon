package cc.turtl.chiselmon.config.custom;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Type;

public class KeyAdapter implements JsonSerializer<InputConstants.Key>, JsonDeserializer<InputConstants.Key> {

    @Override
    public JsonElement serialize(InputConstants.Key key, Type type, JsonSerializationContext ctx) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", key.getType().name());
        obj.addProperty("value", key.getValue());
        return obj;
    }

    @Override
    public InputConstants.Key deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String typeName = obj.get("type").getAsString();
        int value = obj.get("value").getAsInt();
        InputConstants.Type keyType = InputConstants.Type.valueOf(typeName);
        return keyType.getOrCreate(value);
    }
}