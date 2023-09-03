package org.lhotse.config.core;

import org.lhotse.config.core.exception.LhotseException;

public interface FieldInfo {

    String name();

    Object getRealValue(String rawValue);
}

class NormalFieldInfo implements FieldInfo {

    final String name;
    final Class<?> type;

    NormalFieldInfo(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object getRealValue(String rawValue) {
        if (rawValue == null) {
            if (type.equals(boolean.class)) {
                return false;
            } else if (type.equals(byte.class)) {
                return 0;
            } else if (type.equals(short.class)) {
                return 0;
            } else if (type.equals(int.class)) {
                return 0;
            } else if (type.equals(long.class)) {
                return 0L;
            } else if (type.equals(float.class)) {
                return 0F;
            } else if (type.equals(double.class)) {
                return 0.;
            }
            return null;
        }
        if (type.equals(String.class)) {
            return rawValue;
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(rawValue);
        } else if (type.equals(Byte.class) || type.equals(byte.class)) {
            return Byte.parseByte(rawValue);
        } else if (type.equals(Short.class) || type.equals(short.class)) {
            return Short.parseShort(rawValue);
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(rawValue);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(rawValue);
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return Float.parseFloat(rawValue);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(rawValue);
        }
        throw new UnsupportedOperationException("不支持其它类型操作");
    }
}

class CustomFieldInfo extends NormalFieldInfo {

    final FieldConvertor convertor;

    CustomFieldInfo(String name, Class<?> type, Class<? extends FieldConvertor> convertor) {
        super(name, type);
        try {
            this.convertor = convertor.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new LhotseException("初始化自定义解析器错误", ex);
        }
    }

    @Override
    public Object getRealValue(String rawValue) {
        return convertor.encode(rawValue);
    }
}
