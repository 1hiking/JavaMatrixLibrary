package io.github.hikingc.matrixsdk.services.utils;

import io.github.hikingc.matrixsdk.exceptions.MatrixIOException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

/// [Mapper] handles the global configuration of an [ObjectMapper] instance and also exposes additional
/// methods to parse JSON [String] responses safely.
@NullMarked
public class Mapper {

    private static final ObjectMapper INSTANCE = buildMapper();

    private Mapper() {
    }

    /// Returns the shared [ObjectMapper] instance used for all
    /// usages in the library.
    ///
    /// The instance only modified configuration is [PropertyNamingStrategies#SNAKE_CASE] to
    /// match the Matrix spec's conventions.
    ///
    /// @return the shared, pre-configured [ObjectMapper] instance
    public static ObjectMapper getInstance() {
        return INSTANCE;
    }

    private static ObjectMapper buildMapper() {
        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }

    /// Attempts to extract a key value from a deserialized JSON Object as a [String].
    /// Useful for dealing with simple [String] responses.
    ///
    /// @param json a JSON [String].
    /// @param key  the key of the JSON Object.
    /// @return the corresponding value.
    public static String getStringValueOfAJsonKey(String json, String key) {
        JsonNode tree = INSTANCE.readTree(json);
        if (tree == null || tree.isMissingNode()) {
            throw new MatrixIOException("Missing '%s' in server response".formatted(key));
        }

        return tree.get(key).stringValue();
    }

    /// Attempts to extract a key value from a deserialized JSON Object to a [List].
    /// Useful for dealing with iteration of [Object]s.
    ///
    /// @param json        a JSON [String].
    /// @param key         the key of the JSON Object.
    /// @param elementType the [Class] to deserialize each element into.
    /// @param <T>         the type to deserialize each element into
    /// @return the deserialized [List] of values for the given key
    public static <T> List<T> getListFromAJsonKey(String json, String key, Class<T> elementType) {
        JsonNode tree = INSTANCE.readTree(json);
        JsonNode value = tree.get(key);
        if (value == null || value.isMissingNode()) {
            throw new MatrixIOException("Missing '%s' in server response".formatted(key));
        }
        if (!value.isArray()) {
            throw new MatrixIOException("Expected '%s' to be an Array, was %s".formatted(key, value.getNodeType()));
        }
        JavaType listType = INSTANCE.getTypeFactory().constructCollectionType(List.class, elementType);
        return INSTANCE.convertValue(value, listType);
    }

    /// Produces a JSON [String] from a map of key values, used for input bodies that don't have a configured record class.
    ///
    /// @param map the key-values for the JSON Object
    /// @return a serialized [String].
    @Nullable
    public static String createObjectFromMap(@Nullable Map<String, @Nullable Object> map) {
        if (map == null) {
            return null;
        }
        ObjectNode node = INSTANCE.createObjectNode();
        map.forEach((key, value) -> {
            switch (value) {
                case null -> node.putNull(key);
                case String s -> node.put(key, s);
                case Boolean b -> node.put(key, b);
                case Integer i -> node.put(key, i);
                case Long l -> node.put(key, l);
                case Double d -> node.put(key, d);
                case List<?> list -> {
                    var array = node.putArray(key);
                    list.forEach(item -> array.add(item.toString()));
                }
                default -> node.put(key, value.toString());
            }
        });
        return node.toString();
    }

    /// Deserializes a JSON response body into an instance of the given type.
    ///
    /// @param responseBody the raw JSON string returned by the Matrix API
    /// @param type         the target class to deserialize into
    /// @param <T>          the [Class] type to deserialize into
    /// @return the deserialized [Object]
    /// @throws MatrixIOException if the JSON cannot be parsed into the target type
    public static <T> T getObjectFromString(@Nullable String responseBody, @Nullable Class<T> type) {
        if (responseBody == null || type == null) {
            throw new IllegalArgumentException("responseBody and type must not be null");
        }
        try {
            return INSTANCE.readValue(responseBody, type);
        } catch (DatabindException e) {
            throw new MatrixIOException("Server didn't return required fields specified by the protocol", e);
        } catch (StreamReadException e) {
            throw new MatrixIOException("Server responded with invalid response", e);
        }
    }

    /// Deserializes a JSON response body into an instance of a class based on a [TypeReference].
    ///
    /// @param responseBody the raw JSON string returned by the Matrix API
    /// @param type         a [TypeReference]
    /// @param <T>          the [Class] type to deserialize into
    /// @return the deserialized [Object]
    /// @throws MatrixIOException if the JSON cannot be parsed into the target type
    public static <T> T getObjectFromString(@Nullable String responseBody, @Nullable TypeReference<T> type) {
        if (responseBody == null || type == null) {
            throw new IllegalArgumentException("responseBody and type must not be null");
        }
        try {
            return INSTANCE.readValue(responseBody, type);
        } catch (DatabindException e) {
            throw new MatrixIOException("Server didn't return required fields specified by the protocol", e);
        } catch (StreamReadException e) {
            throw new MatrixIOException("Server responded with invalid response", e);
        }
    }
}