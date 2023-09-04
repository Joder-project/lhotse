package org.lhotse.config.spring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(LhotseProperties.PATH)
public class LhotseProperties {
    static final String PATH = "lhotse";

    /**
     * 配置表路径
     */
    private String configRoot;
    /**
     * 文件监听间隔（ms）
     */
    private long watchUpdateFileIntervalMs = 5000L;
}
