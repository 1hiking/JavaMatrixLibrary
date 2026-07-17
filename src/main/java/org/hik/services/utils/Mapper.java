package org.hik.services.utils;

import org.hik.exceptions.MatrixIOException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

/// The [Mapper] class handles the global configuration of an [ObjectMapper] instance and also exposes additional
/// methods to parse JSON [String] responses safely.
public class Mapper {

    private static final ObjectMapper INSTANCE = buildMapper();

    private Mapper() {
    }

    /// Returns the shared [ObjectMapper] instance used for all
    /// usages in the library.
    ///
    /// The instance is configured with snake\_case property naming to
    /// match the Matrix spec's conventions, and auto-discovers
    /// any `Jackson` modules present on the classpath.
    ///
    /// @return the shared, pre-configured [ObjectMapper] instance
    public static ObjectMapper getInstance() {
        return INSTANCE;
    }

    private static ObjectMapper buildMapper() {
        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .findAndAddModules()
                .build();
    }

    /// Extracts a single key value from a deserialized JSON Object. Useful for dealing with simple response types.
    ///
    /// @param json a JSON [String].
    /// @param key  the key of the JSON Object.
    /// @return the corresponding value.
    public static String getStringFromSingleObject(String json, String key) {
        JsonNode tree = INSTANCE.readTree(json);
        if (tree.isMissingNode()) {
            throw new MatrixIOException("Missing '%s' in server response ".formatted(key));
        }
        return tree.get(key).stringValue();
    }

    /// Produces a JSON [String] from a map of key values, used for input bodies that don't have a configured record class.
    ///
    /// @param map the key-values for the JSON Object
    /// @return a serialized [String].
    public static String createObjectFromMap(Map<String, Object> map) {
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
    /// @param <T>          the class type to deserialize into
    /// @return the deserialized object
    /// @throws MatrixIOException if the JSON cannot be parsed into the target type
    public static <T> T getObjectFromString(String responseBody, Class<T> type) {
        if (responseBody == null || type == null) {
            throw new IllegalArgumentException("responseBody and type must not be null");
        }
        try {
            return INSTANCE.readValue(responseBody, type);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON", e);
        }
    }
}