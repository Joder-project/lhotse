package test;

import org.lhotse.config.core.ISingleStorage;
import org.lhotse.config.core.annotations.SingleConfig;
import org.lhotse.config.core.annotations.StorageConfig;
import org.lhotse.config.spring.GenerateStorage;

@GenerateStorage
@StorageConfig(path = "system-config.xlsx")
@SingleConfig(key = "system")
public record SystemConfig(int threadNum, String systemName)
        implements ISingleStorage {

}
