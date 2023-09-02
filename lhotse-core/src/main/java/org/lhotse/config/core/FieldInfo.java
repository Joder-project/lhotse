package org.lhotse.config.core;

public interface FieldInfo {

    Object getRealValue(DataContainer container, Object rawValue);
}

class NormalFieldInfo implements FieldInfo{

    @Override
    public Object getRealValue(DataContainer container, Object rawValue) {
        return null;
    }
}

class DependenceFieldInfo implements FieldInfo{

    @Override
    public Object getRealValue(DataContainer container, Object rawValue) {
        return null;
    }
}

class CustomFieldInfo implements FieldInfo{

    @Override
    public Object getRealValue(DataContainer container, Object rawValue) {
        return null;
    }
}
