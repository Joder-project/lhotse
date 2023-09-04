package org.lhotse.config.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelFileReaderTest {

    @Test
    void readFile() {
        var path = this.getClass().getClassLoader().getResource("excel/multi-config.xlsx").getFile();
        if (path.charAt(2) == ':') {
            path = path.substring(1);
        }
        path += ":item";
        var excelFileReader = new ExcelFileReader();
        var mapList = excelFileReader.readFile(path);
        assertEquals(5, mapList.size());
        assertEquals("A1", mapList.get(0).get("name"));
        assertEquals("1", mapList.get(0).get("id"));
        assertEquals("B1", mapList.get(0).get("value"));
        assertEquals("A5", mapList.get(4).get("name"));
        assertEquals("B5", mapList.get(4).get("value"));
    }

    @Test
    void readFileForSingle() {
        var path = this.getClass().getClassLoader().getResource("excel/single-config.xlsx").getFile();
        if (path.charAt(2) == ':') {
            path = path.substring(1);
        }
        var excelFileReader = new ExcelFileReader();
        var map = excelFileReader.readFileForSingle(path);
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals("1", map.get("a").get("name"));
        assertEquals("2", map.get("a").get("age"));
        assertEquals("3", map.get("a").get("num"));

        assertEquals("1", map.get("b").get("id"));
        assertEquals("A", map.get("b").get("name"));
    }
}