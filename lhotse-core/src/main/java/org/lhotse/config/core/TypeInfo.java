package org.lhotse.config.core;

import java.util.Set;

public interface TypeInfo {
}

/**
 * 配置类字段信息
 * @param clazz
 */
record ConfigTypeInfo(Class<?> clazz, Set<FieldInfo> fieldInfos) {

}

/**
 * @param path 文件路径
 */
record MultiTypeInfo(Class<?> clazz, String path)  implements TypeInfo {
}

/**
 * @param path 文件路径
 */
record SingleTypeInfo(Set<Class<?>> classes, String path) implements TypeInfo {
}

