package org.hik.services.utils;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

public final class ConfiguratedMapper {

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
}