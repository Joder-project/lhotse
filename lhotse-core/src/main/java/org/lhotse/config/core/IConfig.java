package org.lhotse.config.core;

public interface IConfig<ID extends Comparable<ID>> {

    /**
     * 配置表ID
     */
    ID id();
}
