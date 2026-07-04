package org.hik.services.utils;

import org.hik.exceptions.MatrixIOException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

public class ConfigurationMapper {

    private static final ObjectMapper INSTANCE = buildMapper();

    private ConfigurationMapper() {
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
}