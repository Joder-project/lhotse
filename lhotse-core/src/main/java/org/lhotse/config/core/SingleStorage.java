package org.lhotse.config.core;

import java.util.Optional;

/**
 * 单配置表查询
 *
 * @param <Config> 对应类
 */
public interface SingleStorage<Config extends ISingleStorage> {

    /**
     * 获取配置表数据
     */
    Optional<Config> tryGet();

    /**
     * 用户认为一定不为空
     */
    default Config value() {
        return tryGet().orElseThrow();
    }
}

class SingleStorageImpl<Config extends ISingleStorage> implements SingleStorage<Config> {
    private final Class<Config> clazz;
    private final GlobalDataStorage globalDataStorage;

    SingleStorageImpl(Class<Config> clazz, GlobalDataStorage globalDataStorage) {
        this.clazz = clazz;
        this.globalDataStorage = globalDataStorage;
    }

    @Override
    public Optional<Config> tryGet() {
        return globalDataStorage.getSingleConfig(clazz);
    }
}
