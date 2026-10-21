package org.unibl.etf.mdp.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import org.unibl.etf.mdp.model.Message;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The one JSON format of the system, used on sockets, RabbitMQ and multicast instead of Java serialization:
 * the receiver names the type it expects, so a message can never instantiate an unexpected class.
 */
public final class Json {
    public static final Type STRING_MAP = new TypeToken<Map<String, String>>() {
    }.getType();

    // Dates travel as epoch milliseconds so they compare equal on the other side
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (d, t, c) -> new JsonPrimitive(d.getTime()))
            .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (j, t, c) -> new Date(j.getAsLong()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (d, t, c) -> new JsonPrimitive(d.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (j, t, c) -> LocalDate.parse(j.getAsString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (d, t, c) -> new JsonPrimitive(d.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (j, t, c) -> LocalDateTime.parse(j.getAsString()))
            .create();

    private Json() {
    }

    public static Gson gson() {
        return GSON;
    }

    public static String toJson(Object value) {
        return GSON.toJson(value);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    public static JsonElement toTree(Object value) {
        return value == null ? JsonNull.INSTANCE : GSON.toJsonTree(value);
    }

    public static <T> T fromTree(JsonElement tree, Type type) {
        return tree == null || tree.isJsonNull() ? null : GSON.fromJson(tree, type);
    }

    public static Type listOf(Class<?> element) {
        return TypeToken.getParameterized(List.class, element).getType();
    }

    public static byte[] toBytes(Message message) {
        return toJson(message).getBytes(StandardCharsets.UTF_8);
    }

    public static Message messageFromBytes(byte[] bytes, int length) {
        return fromJson(new String(bytes, 0, length, StandardCharsets.UTF_8), Message.class);
    }
}
