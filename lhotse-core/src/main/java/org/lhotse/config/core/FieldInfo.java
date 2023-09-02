package org.lhotse.config.core;

public interface FieldInfo {

    String name();

    Object getRealValue(DataContainer container, String rawValue);
}

class NormalFieldInfo implements FieldInfo{

    @Override
    public String name() {
        return null;
    }

    @Override
    public Object getRealValue(DataContainer container, String rawValue) {
        return null;
    }
}

class DependenceFieldInfo implements FieldInfo{

    @Override
    public String name() {
        return null;
    }

    @Override
    public Object getRealValue(DataContainer container, String rawValue) {
        return null;
    }
}

class CustomFieldInfo implements FieldInfo{

    @Override
    public String name() {
        return null;
    }

    @Override
    public Object getRealValue(DataContainer container, String rawValue) {
        return null;
    }
}
