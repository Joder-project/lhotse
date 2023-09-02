package org.lhotse.config.core;

import org.lhotse.config.core.annotations.SingleConfig;

import java.util.*;

class DataContainer {

    final Map<Class<?>, List<ConfigData>> multiConfigData;
    final Map<Class<?>, ConfigData> singleConfigData;

    DataContainer() {
        this.multiConfigData = Collections.emptyMap();
        this.singleConfigData = Collections.emptyMap();
    }


    DataContainer(Map<Class<?>, List<ConfigData>> multiConfigData, Map<Class<?>, ConfigData> singleConfigData) {
        this.multiConfigData = multiConfigData;
        this.singleConfigData = singleConfigData;
    }

    /**
     * 刷新数据
     *
     * @param configs  类与对应文件关系
     * @param typeInfo 类信息
     */
    DataContainer refresh(Map<String, TypeInfo> configs, Map<Class<?>, ConfigTypeInfo> typeInfo) {
        return new StepWithRead(configs, typeInfo, this)
                .readRaw()
                .parseConfigData()
                .parseNormalField()
                .parseDependence()
                .toObject()
                .refresh();
    }


    /**
     * 读取原始数据
     * @param configs 文件路径对应类
     * @param typeInfo 类的配置信息
     * @param oldContainer 旧数据
     */
    record StepWithRead(Map<String, TypeInfo> configs, Map<Class<?>, ConfigTypeInfo> typeInfo,
                        DataContainer oldContainer) {

        StepWithParseConfig readRaw() {
            Map<String, List<Map<String, String>>> multiConfigData = new HashMap<>();
            Map<String, Map<String, String>> singleConfigData = new HashMap<>();
            Map<String, Class<?>> singleConfigWithKey = new HashMap<>();
            configs.forEach((path, info) -> {
                var reader = ConfigReader.get(path).getFileReader();
                if (info instanceof SingleTypeInfo singleTypeInfo) {
                    var result = reader.readFileForSingle(singleTypeInfo.path());
                    singleConfigData.putAll(result);
                    singleConfigWithKey.putAll(parseSingleClass(singleTypeInfo.classes()));
                } else {
                    var multiTypeInfo = (MultiTypeInfo) info;
                    var result = reader.readFile(multiTypeInfo.path());
                    multiConfigData.put(path, result);
                }
            });
            return new StepWithParseConfig(multiConfigData, singleConfigData, configs, typeInfo, singleConfigWithKey, oldContainer);
        }

        Map<String, Class<?>> parseSingleClass(Set<Class<?>> classes) {
            Map<String, Class<?>> ret = new HashMap<>();
            for (Class<?> clazz : classes) {
                var annotation = clazz.getAnnotation(SingleConfig.class);
                String key = annotation.key();
                ret.put(key, clazz);
            }
            return ret;
        }
    }

    /**
     * 原始数据转为类原始数据
     *
     * @param multiConfigData  path: data
     * @param singleConfigData path: data
     */
    record StepWithParseConfig(Map<String, List<Map<String, String>>> multiConfigData,
                               Map<String, Map<String, String>> singleConfigData,
                               Map<String, TypeInfo> configs,
                               Map<Class<?>, ConfigTypeInfo> typeInfo,
                               Map<String, Class<?>> singleConfigWithKey,
                               DataContainer oldContainer) {

        StepWithParseNormalField parseConfigData() {
            Map<Class<?>, List<Map<String, String>>> multiConfigData = new HashMap<>();

            multiConfigData().forEach((path, data) -> {
                var info = configs.get(path);
                if (info instanceof MultiTypeInfo multiTypeInfo) {
                    multiConfigData.put(multiTypeInfo.clazz(), data);
                }
            });
            Map<Class<?>, Map<String, String>> singleConfigData = new HashMap<>();
            singleConfigData().forEach((path, data) -> {
                Class<?> clazz = singleConfigWithKey.get(path);
                singleConfigData.put(clazz, data);
            });
            return new StepWithParseNormalField(multiConfigData, singleConfigData, typeInfo, oldContainer);
        }
    }

    /**
     * 初始化普通字段
     *
     * @param multiConfigData  path: data
     * @param singleConfigData path: data
     */
    record StepWithParseNormalField(Map<Class<?>, List<Map<String, String>>> multiConfigData,
                                    Map<Class<?>, Map<String, String>> singleConfigData,
                                    Map<Class<?>, ConfigTypeInfo> typeInfo,
                                    DataContainer oldContainer) {

        StepWithDependence parseNormalField() {
            Map<Class<?>, List<ConfigData>> multiConfigData = new HashMap<>();
            Map<Class<?>, ConfigData> singleConfigData = new HashMap<>();
            multiConfigData().forEach((cls, data) -> {
                multiConfigData.put(cls, data.stream().map(e -> new ConfigData(cls, e, typeInfo.get(cls).fieldInfos())).toList());
            });
            singleConfigData().forEach((cls, data) -> singleConfigData.put(cls, new ConfigData(cls, data, typeInfo.get(cls).fieldInfos())));
            return new StepWithDependence(multiConfigData, singleConfigData, oldContainer);
        }
    }

    /**
     * 解析依赖字段
     */
    record StepWithDependence(Map<Class<?>, List<ConfigData>> multiConfigData,
                              Map<Class<?>, ConfigData> singleConfigData,
                              DataContainer oldContainer) {
        StepWithToObject parseDependence() {
            multiConfigData().values().stream().flatMap(Collection::stream)
                    .forEach(ConfigData::initDependence);
            singleConfigData.values().forEach(ConfigData::initDependence);
            return new StepWithToObject(multiConfigData, singleConfigData);
        }
    }

    /**
     * 转换为数据
     */
    record StepWithToObject(
            Map<Class<?>, List<ConfigData>> multiConfigData,
            Map<Class<?>, ConfigData> singleConfigData
    ) {
        StepWithReplaceData toObject() {
            multiConfigData().forEach((k, v) -> v.forEach(ConfigData::toObject));
            singleConfigData().forEach((k, v) -> v.toObject());
            return new StepWithReplaceData(multiConfigData(), singleConfigData());
        }
    }

    /**
     * 替换内存数据
     */
    record StepWithReplaceData(
            Map<Class<?>, List<ConfigData>> multiConfigData,
            Map<Class<?>, ConfigData> singleConfigData
    ) {
        DataContainer refresh() {
            return new DataContainer(multiConfigData, singleConfigData);
        }
    }

    static class ConfigData {
        /**
         * 对应类
         */
        final Class<?> clazz;

        final Set<FieldInfo> fieldInfos;

        final Map<String, String> rawData;
        final Map<String, Object> properties;

        final DataContainer dataContainer;
        /**
         * 是否完成数据加载
         */
        boolean finishLoad;
        /**
         * 对应数据
         */
        Object data;

        ConfigData(Class<?> clazz, Map<String, String> raw, Set<FieldInfo> fieldInfos, DataContainer container) {
            this.clazz = clazz;
            this.fieldInfos = fieldInfos;
            this.rawData = raw;
            this.dataContainer = container;
            this.properties = initNormalField(raw);
        }

        Map<String, Object> initNormalField(Map<String, String> raw) {
            Map<String, Object> properties = new HashMap<>();
            fieldInfos.forEach(fieldInfo -> {
                if (fieldInfo instanceof NormalFieldInfo || fieldInfo instanceof CustomFieldInfo) {
                    var value = raw.get(fieldInfo.name());
                    properties.put(fieldInfo.name(), fieldInfo.getRealValue(null, value));
                }
            });
            return properties;
        }

        void initDependence() {
            if (finishLoad) {
                return;
            }
            finishLoad = true;
        }

        @SuppressWarnings("unchecked")
        <T> T toObject() {
            if (!finishLoad) {
                initDependence();
            }
            if (data != null) {
                return (T) data;
            }
        }
    }
}
