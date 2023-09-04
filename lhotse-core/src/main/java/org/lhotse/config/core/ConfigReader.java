package org.lhotse.config.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.lhotse.config.core.exception.LhotseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;

@Getter
enum ConfigReader {
    JSON(new JsonFileReader()), EXCEL(new ExcelFileReader());

    private final FileReader fileReader;

    ConfigReader(FileReader fileReader) {
        this.fileReader = fileReader;
    }

    /**
     * 获取文件解析器
     */
    static ConfigReader get(String filepath) {
        Objects.requireNonNull(filepath, "文件路径不能为空");
        for (ConfigReader value : values()) {
            if (value.fileReader.match(filepath)) {
                return value;
            }
        }
        throw new UnsupportedOperationException("不支持解析文件格式" + filepath);
    }
}

interface FileReader {

    /**
     * 读取文件内容
     *
     * @return map数组
     */
    List<Map<String, String>> readFile(String path);


    /**
     * 读取单配置内容
     *
     * @return key: map内容
     */
    Map<String, Map<String, String>> readFileForSingle(String path);

    boolean match(String filepath);
}

class JsonFileReader implements FileReader {

    /**
     * 多文件格式
     * [
     * {
     * data1
     * },
     * {
     * data2
     * }
     * ]
     */
    @Override
    public List<Map<String, String>> readFile(String path) {
        var json = readFileContent(path);
        try {
            return JsonUtils.MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {
                    }).stream()
                    .map(this::read)
                    .toList();
        } catch (Exception ex) {
            throw new LhotseException("解析Json失败", ex);
        }
    }

    /**
     * single配置表格式
     * {
     * [key1]: {
     * data1
     * },
     * [key2]: {
     * data2
     * }
     * }
     */
    @Override
    public Map<String, Map<String, String>> readFileForSingle(String path) {
        var json = readFileContent(path);
        try {
            var map = JsonUtils.MAPPER.readValue(json, new TypeReference<Map<String, Map<String, Object>>>() {
            });
            Map<String, Map<String, String>> ret = new HashMap<>();
            map.forEach((k, v) -> ret.put(k, read(v)));
            return ret;
        } catch (Exception ex) {
            throw new LhotseException("解析Json失败", ex);
        }
    }

    Map<String, String> read(Map<String, Object> value) {
        Map<String, String> ret = new HashMap<>();
        value.forEach((k, v) -> {
            try {
                if (v instanceof String str) {
                    ret.put(k, str);
                    return;
                }
                ret.put(k, JsonUtils.MAPPER.writeValueAsString(v));
            } catch (Exception ex) {
                throw new LhotseException("序列化Json失败", ex);
            }
        });
        return ret;
    }

    String readFileContent(String path) {
        try {
            return Files.readString(Path.of(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new LhotseException("文件读取失败", e);
        }
    }

    @Override
    public boolean match(String filepath) {
        return filepath.endsWith(".json");
    }

    static class JsonUtils {

        static final ObjectMapper MAPPER;

        static {
            MAPPER = new ObjectMapper();
        }
    }
}

class ExcelFileReader implements FileReader {

    final DecimalFormat decimalFormat = new DecimalFormat();

    /**
     * 文件格式<br>
     * | enable: true|false | <br>
     * | description_1 | description_2 | ... | <br>
     * | title_1 | title_2 | ... | <br>
     * | data1 | data2 | ... | <br>
     */
    @Override
    public List<Map<String, String>> readFile(String path) {
        var filePath = getPath(path);
        var sheet = getSheet(path).orElse(null);
        return readContent(filePath, sheet);
    }

    /**
     * 文件格式 <br>
     * | enable: true|false | <br>
     * | id | key | value | <br>
     * | id1 | k1 | v1| <br>
     * |     | k2 | v2 | <br>
     * | id2 | k3 | v3 | <br>
     * |     | k4 | v4 | <br>
     */
    @Override
    public Map<String, Map<String, String>> readFileForSingle(String path) {
        var filePath = getPath(path);
        var sheet = getSheet(path).orElse(null);
        var mapList = readContent(filePath, sheet);
        Map<String, Map<String, String>> ret = new HashMap<>();
        String currId = null;
        Map<String, String> currMap = new HashMap<>();
        for (var map : mapList) {
            var id = map.get("id");
            if (id != null && !id.isEmpty()) {
                if (currId != null) {
                    ret.put(currId, currMap);
                    currMap = new HashMap<>();
                }
                currId = id;
            }
            if (currId == null) {
                continue;
            }
            var key = map.get("key");
            var value = map.get("value");
            if (key == null || key.isEmpty()) {
                continue;
            }
            currMap.put(key, value);
        }
        ret.put(currId, currMap);

        return ret;
    }

    /**
     * 获取文件路径
     */
    private String getPath(String path) {
        var i = path.lastIndexOf(":");
        if (i < 6) {
            return path;
        }
        return path.substring(0, i);
    }

    private Optional<String> getSheet(String path) {
        var i = path.lastIndexOf(":");
        if (i < 6) {
            return Optional.empty();
        }
        return Optional.of(path.substring(i + 1));
    }

    List<Map<String, String>> readContent(String path, String sheetName) {
        try (var fileInputStream = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = null;
            if (sheetName != null) {
                sheet = workbook.getSheet(sheetName);
            } else {
                // 取第0个
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    sheet = workbook.getSheetAt(i);
                    if (sheetEnable(sheet)) {
                        break;
                    }
                }
            }
            if (sheet == null || !sheetEnable(sheet)) {
                return Collections.emptyList();
            }
            // 获取title
            var sheetTitles = getSheetTitles(sheet);
            if (sheetTitles.isEmpty()) {
                return Collections.emptyList();
            }
            return getContent(sheet, sheetTitles);
        } catch (Exception ex) {
            throw new LhotseException("读取Excel失败" + path + " : " + sheetName, ex);
        }
    }

    /**
     * sheet 是否有用
     */
    boolean sheetEnable(Sheet sheet) {
        if (sheet == null || sheet.getPhysicalNumberOfRows() < 1) {
            return false;
        }
        var row = sheet.getRow(0);
        if (row == null) {
            return false;
        }
        var cell = row.getCell(0);
        if (cell == null) {
            return false;
        }
        switch (cell.getCellType()) {
            case NUMERIC -> {
                return cell.getNumericCellValue() > 0;
            }
            case STRING, BLANK -> {
                return "true".equalsIgnoreCase(cell.getStringCellValue());
            }
            case BOOLEAN -> {
                return cell.getBooleanCellValue();
            }
        }
        return false;
    }

    List<String> getSheetTitles(Sheet sheet) {
        if (sheet == null || sheet.getPhysicalNumberOfRows() < 3) {
            return Collections.emptyList();
        }
        List<String> ret = new ArrayList<>();
        var row = sheet.getRow(2);
        if (row == null) {
            return Collections.emptyList();
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            var cell = row.getCell(i);
            if (cell == null) {
                ret.add("");
            } else {
                switch (cell.getCellType()) {
                    case NUMERIC -> ret.add(decimalFormat.format(cell.getNumericCellValue()));
                    case STRING -> ret.add(cell.getStringCellValue().trim());
                    case BOOLEAN -> ret.add(String.valueOf(cell.getBooleanCellValue()));
                    default -> ret.add("");
                }
            }
        }
        return ret;
    }

    List<Map<String, String>> getContent(Sheet sheet, List<String> titles) {
        if (sheet == null || sheet.getPhysicalNumberOfRows() < 4) {
            return Collections.emptyList();
        }
        List<Map<String, String>> ret = new ArrayList<>();
        for (int r = 3; r < sheet.getPhysicalNumberOfRows(); r++) {
            var row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            boolean emptyRow = true;
            Map<String, String> map = new HashMap<>();
            for (int i = row.getFirstCellNum(); i < row.getLastCellNum() && i < titles.size(); i++) {
                var cell = row.getCell(i);
                if (cell == null) {
                    continue;
                }
                switch (cell.getCellType()) {
                    case NUMERIC -> {
                        map.put(titles.get(i), decimalFormat.format(cell.getNumericCellValue()));
                        emptyRow = false;
                    }
                    case STRING -> {
                        map.put(titles.get(i), cell.getStringCellValue().trim());
                        emptyRow = false;
                    }
                    case BOOLEAN -> {
                        map.put(titles.get(i), String.valueOf(cell.getBooleanCellValue()));
                        emptyRow = false;
                    }
                }
            }
            map.remove("");
            if (emptyRow || map.isEmpty()) {
                continue;
            }
            ret.add(map);
        }
        return ret;
    }


    @Override
    public boolean match(String filepath) {
        var i = filepath.lastIndexOf(":");
        if (i < 6) {
            return filepath.endsWith(".xlsx");
        }
        filepath = filepath.substring(0, i);
        return filepath.endsWith(".xlsx");
    }
}
