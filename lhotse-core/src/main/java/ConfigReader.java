import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public enum ConfigReader {
    JSON(new JsonFileReader()), EXCEL(new ExcelFileReader());

    private final FileReader fileReader;

    ConfigReader(FileReader fileReader) {
        this.fileReader = fileReader;
    }
}

interface FileReader {

    /**
     * 读取文件内容
     * @return map数组
     */
    List<Map<String, String>> readFile();


    /**
     * 读取单配置内容
     * @return key: map内容
     */
    Map<String, Map<String, String>> readFileForSingle();
}

class JsonFileReader implements FileReader {

    @Override
    public List<Map<String, String>> readFile() {
        return null;
    }

    @Override
    public Map<String, Map<String, String>> readFileForSingle() {
        return null;
    }
}

class ExcelFileReader implements FileReader {

    @Override
    public List<Map<String, String>> readFile() {
        return null;
    }

    @Override
    public Map<String, Map<String, String>> readFileForSingle() {
        return null;
    }
}
