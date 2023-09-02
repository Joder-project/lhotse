package org.lhotse.config.core;

import lombok.Getter;

import java.util.List;
import java.util.Map;

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
        return JSON;
    }
}

interface FileReader {

    /**
     * 读取文件内容
     * @return map数组
     */
    List<Map<String, String>> readFile(String path);


    /**
     * 读取单配置内容
     * @return key: map内容
     */
    Map<String, Map<String, String>> readFileForSingle(String path);
}

class JsonFileReader implements FileReader {

    @Override
    public List<Map<String, String>> readFile(String path) {
        return null;
    }

    @Override
    public Map<String, Map<String, String>> readFileForSingle(String path) {
        return null;
    }
}

class ExcelFileReader implements FileReader {

    @Override
    public List<Map<String, String>> readFile(String path) {
        return null;
    }

    @Override
    public Map<String, Map<String, String>> readFileForSingle(String path) {
        return null;
    }
}
