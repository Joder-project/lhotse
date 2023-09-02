package org.lhotse.config.core;

import org.lhotse.config.core.annotations.StorageConfig;

import java.util.*;

/**
 * 保存全局配置表数据
 */
public class GlobalDataStorage {

    /**
     * 注册配置表
     */
    public void init(Set<Class<?>> types) {

    }

    /**
     * 需要刷新的配置表
     *
     * @param types 类型
     */
    public void refresh(Set<Class<?>> types) {

    }

    <ID extends Comparable<ID>, Config extends IConfig<ID>> List<Config> listConfig(Class<Config> clazz) {
        return Collections.emptyList();
    }

    <ID extends Comparable<ID>, Config extends IConfig<ID>> Optional<Config> getConfig(Class<Config> clazz, ID id) {
        return Optional.empty();
    }

    <Config extends ISingleStorage> Optional<Config> getSingleConfig(Class<Config> clazz) {
        return Optional.empty();
    }





    static class TypeInfoParse {
        /**
         * 多数据配置
         */
        private final Map<Class<?>, TypeInfo> typeInfoMap;
        /**
         * 单数据配置
         */
        private final Map<String, SingleTypeInfo> singleTypeInfoMap;

        TypeInfoParse(Set<Class<?>> types) {
            // 解析配置表类型
            Map<Class<?>, TypeInfo> typeInfoMap = new HashMap<>();
            Map<String, Set<Class<?>>> singleTypeMap = new HashMap<>();
            for (Class<?> type : types) {
                if (!type.isAnnotationPresent(StorageConfig.class)) {
                    throw new IllegalStateException("没有@StorageConfig注解, " + type.getName());
                }
                var annotation = type.getAnnotation(StorageConfig.class);
                var path = annotation.path();
                if (ISingleStorage.class.isAssignableFrom(type)) {
                    if (!singleTypeMap.containsKey(path)) {
                        singleTypeMap.put(path, new HashSet<>());
                    }
                    singleTypeMap.get(path).add(type);
                } else {
                    typeInfoMap.put(type, new MultiTypeInfo(type, path));
                }
            }
            Map<String, SingleTypeInfo> singleTypeInfoMap = new HashMap<>();
            singleTypeMap.forEach((k, v) -> singleTypeInfoMap.put(k, new SingleTypeInfo(v, k)));
            this.singleTypeInfoMap = singleTypeInfoMap;
            this.typeInfoMap = typeInfoMap;
            // 解析依赖关系
        }
    }
}
