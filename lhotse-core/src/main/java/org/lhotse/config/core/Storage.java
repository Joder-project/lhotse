package org.lhotse.config.core;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * 配置表查询
 *
 * @param <ID>     主键
 * @param <Config> 对应类
 */
public interface Storage<ID extends Comparable<ID>, Config extends IConfig<ID>> {

    /**
     * 获取所有数据
     */
    @Nonnull
    List<Config> listConfig();

    /**
     * 获取配置表数据
     */
    Optional<Config> get(@Nonnull ID id);
}

class StorageImpl<ID extends Comparable<ID>, Config extends IConfig<ID>> implements Storage<ID, Config> {

    private final Class<Config> clazz;
    private final GlobalDataStorage globalDataStorage;

    StorageImpl(Class<Config> clazz, GlobalDataStorage globalDataStorage) {
        this.clazz = clazz;
        this.globalDataStorage = globalDataStorage;
    }

    @Nonnull
    @Override
    public List<Config> listConfig() {
        return globalDataStorage.listConfig(clazz);
    }

    @Override
    public Optional<Config> get(@Nonnull ID id) {
        return globalDataStorage.getConfig(clazz, id);
    }
}
