package org.lhotse.config.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileReaderTest {

    @Test
    void readFile() {
        var reader = new JsonFileReader();
        var path = this.getClass().getClassLoader().getResource("./json/multi.json").getFile();
        if (path.charAt(2) == ':') {
            path = path.substring(1);
        }
        var list = reader.readFile(path);
        assertFalse(list.isEmpty());
        var map = list.get(0);
        assertEquals(map.get("a"), "1");
        assertEquals(map.get("b"), "true");
        assertEquals(map.get("c"), "1.1");
        assertEquals(map.get("d"), "A");
        assertEquals(map.get("e"), "[1,2]");
        assertEquals(map.get("f"), "{\"a\":1}");
    }

    @Test
    void readFileForSingle() throws Exception {
        var reader = new JsonFileReader();
        var path = this.getClass().getClassLoader().getResource("./json/single.json").getFile();
        if (path.charAt(2) == ':') {
            path = path.substring(1);
        }
        var map = reader.readFileForSingle(path);
        assertTrue(map.containsKey("key"));
        assertTrue(map.get("key").containsKey("a"));
        assertEquals(map.get("key").get("a"), "1");
        assertEquals(map.get("key").get("b"), "true");
        assertEquals(map.get("key").get("c"), "1.1");
        assertEquals(map.get("key").get("d"), "A");
        assertEquals(map.get("key").get("e"), "[1,2]");
        assertEquals(map.get("key").get("f"), "{\"a\":1}");
    }

    @Test
    void readSingle() throws JsonProcessingException {
        var reader = new JsonFileReader();
        String json = """
                {
                    "key": {
                        "a": 1,
                        "b": true,
                        "c": 1.1,
                        "d": "A",
                        "e": [1, 2],
                        "f": {
                            "a": 1
                        }
                    }
                }
                """;
        var temp = JsonFileReader.JsonUtils.MAPPER.readValue(json, new TypeReference<Map<String, Map<String, Object>>>() {
        });
        Map<String, Map<String, String>> map = new HashMap<>();
        temp.forEach((k, v) -> map.put(k, reader.read(v)));

        assertTrue(map.containsKey("key"));
        assertTrue(map.get("key").containsKey("a"));
        assertEquals(map.get("key").get("a"), "1");
        assertEquals(map.get("key").get("b"), "true");
        assertEquals(map.get("key").get("c"), "1.1");
        assertEquals(map.get("key").get("d"), "A");
        assertEquals(map.get("key").get("e"), "[1,2]");
        assertEquals(map.get("key").get("f"), "{\"a\":1}");
    }
}