package org.hik.services.utils;

import org.hik.exceptions.MatrixIOException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;

public class Mapper {

    private static final ObjectMapper INSTANCE = buildMapper();

    private Mapper() {
    }

    public static ObjectMapper getInstance() {
        return INSTANCE;
    }

    private static ObjectMapper buildMapper() {
        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .findAndAddModules()
                .build();
    }

    /// Extracts a single key value from a deserialized JSON Object. Useful when dealing with simple response types
    ///
    /// @param json a deserialized JSON [String].
    /// @param key  the key of the JSON Object.
    /// @return the corresponding value.
    public static String getStringFromSingleObject(String json, String key) {
        JsonNode tree = INSTANCE.readTree(json);
        if (tree.isMissingNode()) {
            throw new MatrixIOException("Missing '%s' in server response ".formatted(key));
        }
        return tree.get(key).stringValue();
    }

    /// Creates an object from a map of key values, convenient when the input is simple and doesn't demand a record.
    ///
    /// @param map the key-values for the JSON Object
    /// @return a serialized [String].
    public static String createObjectFromMap(Map<String, String> map) {
        if (map == null) {
            return null;
        }

        ObjectNode x = INSTANCE.createObjectNode();
        map.forEach(x::put);
        return x.toString();
    }
}