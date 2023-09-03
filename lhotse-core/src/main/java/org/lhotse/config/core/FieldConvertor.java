package org.lhotse.config.core;

import javax.annotation.Nonnull;

public interface FieldConvertor {

    /**
     * 字段自定义解析
     */
    Object encode(@Nonnull String text);
}
