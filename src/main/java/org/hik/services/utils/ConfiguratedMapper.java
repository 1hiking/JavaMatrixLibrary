package org.hik.services.utils;

import org.hik.exceptions.MatrixIOException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

public class ConfiguratedMapper {

    private static final ObjectMapper INSTANCE = buildMapper();

    private ConfiguratedMapper() {
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
            throw new MatrixIOException("Missing 'room_id' in server response ");
        }
        return tree.get(key).stringValue();
    }
}