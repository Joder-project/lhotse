package org.lhotse.config.core;

import org.lhotse.config.core.annotations.Custom;
import org.lhotse.config.core.annotations.SingleConfig;
import org.lhotse.config.core.annotations.StorageConfig;

import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 保存全局配置表数据
 */
public class GlobalDataStorage {
    volatile TypeInfoParse typeInfoParse;

    volatile AtomicReference<DataContainer> dataContainer = new AtomicReference<>(new DataContainer());

    static final ReadWriteLock Lock = new ReentrantReadWriteLock();

    /**
     * 注册配置表
     */
    public void init(Set<Class<?>> types) {
        this.typeInfoParse = new TypeInfoParse(types);
        refresh(types);
    }

    /**
     * 需要刷新的配置表
     *
     * @param types 类型
     */
    public void refresh(Set<Class<?>> types) {
        Map<String, TypeInfo> configs = new HashMap<>();
        for (Class<?> type : types) {
            if (!type.isAnnotationPresent(StorageConfig.class)) {
                throw new IllegalStateException("没有@StorageConfig注解, " + type.getName());
            }
            var annotation = type.getAnnotation(StorageConfig.class);
            var path = annotation.path();

            configs.put(path, Objects.requireNonNull(typeInfoParse.typeInfoMap.get(path)));
        }
        Lock.writeLock().lock();
        try {
            this.dataContainer.set(dataContainer.get().refresh(configs, typeInfoParse.configTypeInfo));
        } finally {
            Lock.writeLock().unlock();
        }

    }

    @SuppressWarnings("unchecked")
    <ID extends Comparable<ID>, Config extends IConfig<ID>> List<Config> listConfig(Class<Config> clazz) {
        var container = dataContainer.get();
        if (!container.multiConfigData.containsKey(clazz)) {
            return Collections.emptyList();
        }
        var map = container.multiConfigData.get(clazz);
        return map.values().stream()
                .map(e -> ((Config) e))
                .toList();
    }

    @SuppressWarnings("unchecked")
    <ID extends Comparable<ID>, Config extends IConfig<ID>> Optional<Config> getConfig(Class<Config> clazz, ID id) {
        var container = dataContainer.get();
        if (!container.multiConfigData.containsKey(clazz)) {
            return Optional.empty();
        }
        var map = container.multiConfigData.get(clazz);
        return Optional.ofNullable(((Config) map.get(id)));
    }

    @SuppressWarnings("unchecked")
    <Config extends ISingleStorage> Optional<Config> getSingleConfig(Class<Config> clazz) {
        var container = dataContainer.get();
        if (!container.singleConfigData.containsKey(clazz)) {
            return Optional.empty();
        }
        return Optional.ofNullable(((Config) container.singleConfigData.get(clazz)));
    }

    /**
     * 安全查询
     * 保证查询时数据是安全的
     */
    public static <T1, T2> Tuple2<T1, T2> safeBatchQuery(Callable2<T1, T2> callable) {
        Lock.readLock().lock();
        try {
            return callable.call();
        } finally {
            Lock.readLock().unlock();
        }
    }

    /**
     * 安全查询
     * 保证查询时数据是安全的
     */
    public static <T1, T2, T3> Tuple3<T1, T2, T3> safeBatchQuery(Callable3<T1, T2, T3> callable) {
        Lock.readLock().lock();
        try {
            return callable.call();
        } finally {
            Lock.readLock().unlock();
        }
    }

    /**
     * 安全查询
     * 保证查询时数据是安全的
     */
    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> safeBatchQuery(Callable4<T1, T2, T3, T4> callable) {
        Lock.readLock().lock();
        try {
            return callable.call();
        } finally {
            Lock.readLock().unlock();
        }
    }

    /**
     * 安全查询
     * 保证查询时数据是安全的
     */
    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> safeBatchQuery(Callable5<T1, T2, T3, T4, T5> callable) {
        Lock.readLock().lock();
        try {
            return callable.call();
        } finally {
            Lock.readLock().unlock();
        }
    }

    /**
     * 安全查询
     * 保证查询时数据是安全的
     */
    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> safeBatchQuery(Callable6<T1, T2, T3, T4, T5, T6> callable) {
        Lock.readLock().lock();
        try {
            return callable.call();
        } finally {
            Lock.readLock().unlock();
        }
    }


    static class TypeInfoParse {
        /**
         * 多数据配置
         */
        final Map<String, TypeInfo> typeInfoMap;

        final Map<Class<?>, ConfigTypeInfo> configTypeInfo;


        TypeInfoParse(Set<Class<?>> types) {
            // 解析配置表类型
            this.typeInfoMap = parsePathType(types);
            this.configTypeInfo = parseConfig(types);
        }

        Map<String, TypeInfo> parsePathType(Set<Class<?>> types) {
            Map<String, TypeInfo> typeInfoMap = new HashMap<>();
            Map<String, Set<Class<?>>> singleTypeMap = new HashMap<>();
            for (Class<?> type : types) {
                if (!type.isAnnotationPresent(StorageConfig.class)) {
                    throw new IllegalStateException("没有@StorageConfig注解, " + type.getName());
                }
                var annotation = type.getAnnotation(StorageConfig.class);
                var path = annotation.path();
                var singleConfig = type.getAnnotation(SingleConfig.class);
                if (singleConfig != null) {
                    if (!singleTypeMap.containsKey(path)) {
                        singleTypeMap.put(path, new HashSet<>());
                    }
                    singleTypeMap.get(path).add(type);
                } else {
                    typeInfoMap.put(path, new MultiTypeInfo(type, path));
                }
            }
            singleTypeMap.forEach((k, v) -> typeInfoMap.put(k, new SingleTypeInfo(v, k)));
            return typeInfoMap;
        }

        Map<Class<?>, ConfigTypeInfo> parseConfig(Set<Class<?>> types) {
            Map<Class<?>, ConfigTypeInfo> map = new HashMap<>();
            for (Class<?> type : types) {
                map.put(type, parseConfig(type));
            }
            return map;
        }

        ConfigTypeInfo parseConfig(Class<?> clazz) {
            Set<FieldInfo> fieldInfos = new HashSet<>();
            for (RecordComponent recordComponent : clazz.getRecordComponents()) {
                var annotation = recordComponent.getAnnotation(Custom.class);
                if (annotation == null) {
                    fieldInfos.add(new NormalFieldInfo(recordComponent.getName(), recordComponent.getType()));
                } else {
                    fieldInfos.add(new CustomFieldInfo(recordComponent.getName(), recordComponent.getType(), annotation.convertor()));
                }
            }
            return new ConfigTypeInfo(clazz, fieldInfos);
        }
    }

    public record Tuple2<T1, T2>(T1 t1, T2 t2) {

    }

    public record Tuple3<T1, T2, T3>(T1 t1, T2 t2, T3 t3) {

    }

    public record Tuple4<T1, T2, T3, T4>(T1 t1, T2 t2, T3 t3, T4 t4) {

    }

    public record Tuple5<T1, T2, T3, T4, T5>(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {

    }

    public record Tuple6<T1, T2, T3, T4, T5, T6>(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {

    }

    @FunctionalInterface
    public interface Callable2<T1, T2> {
        Tuple2<T1, T2> call();
    }

    @FunctionalInterface
    public interface Callable3<T1, T2, T3> {
        Tuple3<T1, T2, T3> call();
    }

    @FunctionalInterface
    public interface Callable4<T1, T2, T3, T4> {
        Tuple4<T1, T2, T3, T4> call();
    }

    @FunctionalInterface
    public interface Callable5<T1, T2, T3, T4, T5> {
        Tuple5<T1, T2, T3, T4, T5> call();
    }

    @FunctionalInterface
    public interface Callable6<T1, T2, T3, T4, T5, T6> {
        Tuple6<T1, T2, T3, T4, T5, T6> call();
    }
}
