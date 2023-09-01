package org.lhotse.config.core;

public class StorageFactory {

    private final GlobalDataStorage globalDataStorage;

    public StorageFactory(GlobalDataStorage globalDataStorage) {
        this.globalDataStorage = globalDataStorage;
    }

    public <ID extends Comparable<ID>, Config extends IConfig<ID>> Storage<ID, Config> create(Class<Config> clazz) {
        return new StorageImpl<>(clazz, globalDataStorage);
    }

    public <Config extends ISingleStorage> SingleStorage<Config> createSingle(Class<Config> clazz) {
        return new SingleStorageImpl<>(clazz, globalDataStorage);
    }
}
