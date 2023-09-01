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

    static class TypeInfo {
        final Class<?> clazz;
        /**
         * 文件路径
         */
        final String path;

        TypeInfo(Class<?> clazz) {
            this.clazz = clazz;
            var annotation = clazz.getAnnotation(StorageConfig.class);
            if (annotation == null) {

            }
            this.path = annotation.path();
        }
    }

    static class SingleTypeInfo {
        final Set<Class<?>> classes;
        /**
         * 文件路径
         */
        final String path;

        SingleTypeInfo(Set<Class<?>> classes, String path) {
            this.classes = classes;
            this.path = path;
        }
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
            for (Class<?> type : types) {
                if (!type.isAnnotationPresent(StorageConfig.class)) {
                    throw new IllegalStateException("没有@StorageConfig注解, " + type.getName());
                }
            }
            types.stream()
        }
    }
}
