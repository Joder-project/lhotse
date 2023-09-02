package org.lhotse.config.core;

import java.util.List;
import java.util.Map;

public class DataContainer {

    /**
     * 刷新数据
     * @param configs 类与对应文件关系
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
     */
    static class StepWithRead {

        final Map<String, TypeInfo> configs;
        final Map<Class<?>, ConfigTypeInfo> typeInfo;

        final DataContainer oldContainer;

        StepWithRead(Map<String, TypeInfo> configs, Map<Class<?>, ConfigTypeInfo> typeInfo, DataContainer oldContainer) {
            this.configs = configs;
            this.typeInfo = typeInfo;
            this.oldContainer = oldContainer;
        }

        StepWithParseConfig readRaw() {

        }
    }

    /**
     * 原始数据转为类原始数据
     */
    static class StepWithParseConfig {

        /**
         * path: data
         */
        private final Map<String, List<Map<String, String>>> multiConfigData;
        /**
         * path: data
         */
        private final Map<String, Map<String, String>> singleConfigData;

        StepWithParseNormalField parseConfigData() {

        }
    }

    /**
     * 初始化普通字段
     */
    static class StepWithParseNormalField {

        /**
         * path: data
         */
        private final Map<Class<?>, List<Map<String, String>>> multiConfigData;
        /**
         * path: data
         */
        private final Map<Class<?>, Map<String, String>> singleConfigData;

        StepWithDependence parseNormalField() {

        }
    }

    /**
     * 解析依赖字段
     */
    static class StepWithDependence {
        StepWithToObject parseDependence() {

        }
    }

    /**
     * 转换为数据
     */
    static class StepWithToObject {
        StepWithReplaceData toObject() {

        }
    }

    /**
     * 替换内存数据
     */
    static class StepWithReplaceData {
        DataContainer refresh() {

        }
    }
}
