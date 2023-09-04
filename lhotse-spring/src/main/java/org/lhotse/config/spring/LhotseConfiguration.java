package org.lhotse.config.spring;

import org.lhotse.config.core.GlobalDataStorage;
import org.lhotse.config.core.StorageFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LhotseProperties.class)
public class LhotseConfiguration {

    @Bean
    StorageFactory storageFactory(GlobalDataStorage dataStorage) {
        return new StorageFactory(dataStorage);
    }

    @Bean
    GlobalDataStorage globalDataStorage(LhotseProperties properties) {
        return new GlobalDataStorage(properties.getConfigRoot());
    }
}
