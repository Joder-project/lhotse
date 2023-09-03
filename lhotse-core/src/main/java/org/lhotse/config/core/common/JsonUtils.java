package org.lhotse.config.core.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public class JsonUtils {

    static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    @SneakyThrows
    public <T> T parseJson(String json, Class<T> type) {
        return objectMapper.readValue(json, type);
    }
}
